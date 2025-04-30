package paymentservice.service;

import paymentservice.dto.PaymentRequestDto;
import paymentservice.dto.TransactionDto;
import paymentservice.model.Transaction;
import paymentservice.repository.TransactionRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final TransactionRepository transactionRepository;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    // TODO: Inject Kafka/RabbitMQ template if publishing events
    // TODO: Inject Policy Service client if needed to update policy status directly

    @Autowired
    public PaymentServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @PostConstruct
    public void checkWebhookSecret() {
        if (webhookSecret == null || webhookSecret.isBlank() || webhookSecret.equals("whsec_YOUR_WEBHOOK_SECRET")) {
            log.warn("Stripe webhook secret is not configured. Webhook verification will fail.");
        }
    }

    @Override
    @Transactional
    public TransactionDto processPayment(PaymentRequestDto paymentRequest) throws StripeException {
        log.info("Processing payment request for policyId: {}", paymentRequest.getPolicyId());

        // 1. Create initial transaction record in PENDING state
        Transaction transaction = new Transaction();
        transaction.setPolicyId(paymentRequest.getPolicyId());
        transaction.setUserId(paymentRequest.getUserId());
        transaction.setAmount(paymentRequest.getAmount());
        transaction.setCurrency(paymentRequest.getCurrency().toLowerCase());
        transaction.setTransactionStatus("PENDING");
        transaction.setPaymentGateway("STRIPE");
        transaction.setTransactionType("PREMIUM_PAYMENT");
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Created initial transaction record with ID: {}", savedTransaction.getTransactionId());

        // 2. Create PaymentIntent with Stripe
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(convertToStripeAmount(paymentRequest.getAmount(), paymentRequest.getCurrency())) // Amount in cents/smallest unit
                .setCurrency(paymentRequest.getCurrency().toLowerCase())
                .setPaymentMethod(paymentRequest.getPaymentMethodId())
                .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
                .setConfirm(true)
                // Add customer ID if available
                // .setCustomer("cus_xyz") 
                .putMetadata("transaction_id", savedTransaction.getTransactionId().toString()) // Link Stripe PI to our transaction
                .putMetadata("policy_id", paymentRequest.getPolicyId().toString())
                .setDescription(paymentRequest.getDescription() != null ? paymentRequest.getDescription() : "Policy Premium Payment")
                // Use off_session for recurring payments if applicable
                // .setOffSession(true)
                .build();

        PaymentIntent paymentIntent;
        try {
            paymentIntent = PaymentIntent.create(params);
            log.info("Created Stripe PaymentIntent with ID: {}", paymentIntent.getId());
        } catch (StripeException e) {
            log.error("Stripe API error during PaymentIntent creation: {}", e.getMessage());
            // Update transaction status to FAILED
            savedTransaction.setTransactionStatus("FAILED");
            transactionRepository.save(savedTransaction);
            throw e; // Re-throw exception
        }

        // 3. Update transaction record with gateway ID
        savedTransaction.setGatewayTransactionId(paymentIntent.getId());
        savedTransaction.setPaymentMethodDetails(getPaymentMethodDetails(paymentIntent)); // Extract minimal details

        // 4. Handle initial PaymentIntent status (requires_action or succeeded)
        // Stripe webhooks are preferred for final status updates, but handle immediate success/failure.
        if ("succeeded".equals(paymentIntent.getStatus())) {
            log.info("PaymentIntent {} succeeded immediately.", paymentIntent.getId());
            savedTransaction.setTransactionStatus("SUCCESS");
            // TODO: Publish PaymentSuccessEvent or call Policy Service
        } else if ("requires_action".equals(paymentIntent.getStatus())) {
            log.info("PaymentIntent {} requires further action (e.g., 3D Secure).", paymentIntent.getId());
            // Status remains PENDING, frontend needs to handle the action using paymentIntent.getClientSecret()
        } else {
            log.warn("PaymentIntent {} has status: {}. Treating as PENDING.", paymentIntent.getId(), paymentIntent.getStatus());
            // Keep status PENDING, rely on webhook for final status
        }

        Transaction finalTransaction = transactionRepository.save(savedTransaction);
        return mapToDto(finalTransaction);
    }

    @Override
    @Transactional
    public TransactionDto handleWebhook(String payload, String signatureHeader) throws StripeException {
        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed.");
            throw e;
        }

        log.info("Received Stripe webhook event: type={}, id={}", event.getType(), event.getId());

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            log.warn("Webhook event data object is empty for event id: {}", event.getId());
            // Cannot process further without data
            return null;
        }

        Transaction updatedTransaction = null;

        // Handle specific event types
        switch (event.getType()) {
            case "payment_intent.succeeded":
                PaymentIntent paymentIntentSucceeded = (PaymentIntent) stripeObject;
                log.info("Webhook: PaymentIntent {} succeeded.", paymentIntentSucceeded.getId());
                updatedTransaction = updateTransactionStatus(paymentIntentSucceeded.getId(), "SUCCESS", paymentIntentSucceeded);
                // TODO: Publish PaymentSuccessEvent or call Policy Service to activate policy
                break;
            case "payment_intent.payment_failed":
                PaymentIntent paymentIntentFailed = (PaymentIntent) stripeObject;
                log.warn("Webhook: PaymentIntent {} failed.", paymentIntentFailed.getId());
                updatedTransaction = updateTransactionStatus(paymentIntentFailed.getId(), "FAILED", paymentIntentFailed);
                // TODO: Publish PaymentFailedEvent or notify user/admin
                break;
            // Add other relevant events like payment_intent.canceled, charge.refunded etc.
            default:
                log.info("Webhook: Unhandled event type: {}", event.getType());
                return null; // Or return DTO of existing transaction if found by metadata
        }

        return updatedTransaction != null ? mapToDto(updatedTransaction) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransactionDto> getTransactionById(UUID transactionId) {
        return transactionRepository.findById(transactionId).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionsByPolicyId(UUID policyId) {
        return transactionRepository.findByPolicyId(policyId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionsByUserId(UUID userId) {
        return transactionRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // --- Helper Methods --- 

    private Transaction updateTransactionStatus(String gatewayTransactionId, String newStatus, PaymentIntent paymentIntent) {
        Transaction transaction = transactionRepository.findByGatewayTransactionId(gatewayTransactionId)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found for gateway ID: " + gatewayTransactionId));

        // Avoid updating if status is already final (SUCCESS, FAILED, REFUNDED)
        if (isFinalStatus(transaction.getTransactionStatus())) {
            log.warn("Transaction {} already has final status {}. Ignoring webhook update to {}.",
                    transaction.getTransactionId(), transaction.getTransactionStatus(), newStatus);
            return transaction;
        }

        transaction.setTransactionStatus(newStatus);
        // Update payment method details if not already set or if changed
        if (transaction.getPaymentMethodDetails() == null && paymentIntent != null) {
            transaction.setPaymentMethodDetails(getPaymentMethodDetails(paymentIntent));
        }
        // Potentially update amount if partial captures/refunds occur, though less common for simple payments

        return transactionRepository.save(transaction);
    }

    private boolean isFinalStatus(String status) {
        return "SUCCESS".equals(status) || "FAILED".equals(status) || "REFUNDED".equals(status);
    }

    private long convertToStripeAmount(BigDecimal amount, String currency) {
        // Stripe expects amount in the smallest currency unit (e.g., cents for USD)
        // TODO: Handle different currency decimal places properly
        int decimalPlaces = 2; // Default for USD, EUR etc.
        if ("jpy".equalsIgnoreCase(currency)) { // Example: Japanese Yen has 0 decimal places
            decimalPlaces = 0;
        }
        BigDecimal multiplier = BigDecimal.TEN.pow(decimalPlaces);
        return amount.multiply(multiplier).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private String getPaymentMethodDetails(PaymentIntent paymentIntent) {
        if (paymentIntent.getPaymentMethod() != null) {
            // Fetch PaymentMethod object to get details (requires extra API call or expansion)
            // For simplicity, often just the ID is stored, or details from the charge if available.
            // Example: return "pm_id:" + paymentIntent.getPaymentMethod();
        }
        if (paymentIntent.getLatestChargeObject() != null && paymentIntent.getLatestChargeObject().getPaymentMethodDetails() != null) {
            // Extract basic details from charge object if available
            var details = paymentIntent.getLatestChargeObject().getPaymentMethodDetails();
            if (details.getCard() != null) {
                return details.getCard().getBrand() + " ****" + details.getCard().getLast4();
            }
            // Add other payment method types if needed (e.g., SEPA, bank transfer)
        }
        return "Details unavailable";
    }

    private TransactionDto mapToDto(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setPolicyId(transaction.getPolicyId());
        dto.setUserId(transaction.getUserId());
        dto.setAmount(transaction.getAmount());
        dto.setCurrency(transaction.getCurrency());
        dto.setTransactionStatus(transaction.getTransactionStatus());
        dto.setPaymentGateway(transaction.getPaymentGateway());
        dto.setGatewayTransactionId(transaction.getGatewayTransactionId());
        dto.setPaymentMethodDetails(transaction.getPaymentMethodDetails());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setUpdatedAt(transaction.getUpdatedAt());
        return dto;
    }

    // mapToEntity is less common here as transactions are usually created internally
}

