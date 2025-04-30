package paymentservice.controller;

import paymentservice.dto.PaymentRequestDto;
import paymentservice.dto.TransactionDto;
import paymentservice.service.PaymentService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments") // Base path for payment endpoints
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Endpoint to initiate a payment
    @PostMapping
    public ResponseEntity<TransactionDto> processPayment(@Valid @RequestBody PaymentRequestDto paymentRequest) {
        // TODO: Add security check (e.g., user can only pay for their own policy)
        log.info("Received payment request for policyId: {}", paymentRequest.getPolicyId());
        try {
            TransactionDto transaction = paymentService.processPayment(paymentRequest);
            // Depending on the PaymentIntent status, the response might indicate success,
            // failure, or require further action (client secret needed on frontend).
            // For simplicity, we return the transaction DTO. Frontend needs to check status.
            return ResponseEntity.ok(transaction);
        } catch (StripeException e) {
            log.error("Stripe error during payment processing: {}", e.getMessage());
            // Return a more specific error based on StripeException type if needed
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Avoid exposing raw error
        } catch (Exception e) {
            log.error("Unexpected error during payment processing: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Endpoint to handle Stripe webhooks
    @PostMapping("/webhook/stripe")
    public ResponseEntity<Void> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        log.info("Received Stripe webhook");
        try {
            paymentService.handleWebhook(payload, sigHeader);
            return ResponseEntity.ok().build();
        } catch (StripeException e) {
            log.error("Error processing Stripe webhook: {}", e.getMessage());
            // Return appropriate status code based on error (e.g., 400 for bad request/signature)
            if (e instanceof com.stripe.exception.SignatureVerificationException) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (NoSuchElementException e) {
            log.error("Error processing webhook - transaction not found: {}", e.getMessage());
            // Return 404 or 400 - debatable, 400 might be better as the webhook data is invalid in context
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Unexpected error processing Stripe webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint to get a transaction by ID
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<TransactionDto> getTransactionById(@PathVariable UUID transactionId) {
        // TODO: Add security check
        return paymentService.getTransactionById(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint to get transactions for a specific policy
    @GetMapping("/policy/{policyId}/transactions")
    public ResponseEntity<List<TransactionDto>> getTransactionsByPolicyId(@PathVariable UUID policyId) {
        // TODO: Add security check
        List<TransactionDto> transactions = paymentService.getTransactionsByPolicyId(policyId);
        return ResponseEntity.ok(transactions);
    }

    // Endpoint to get transactions for a specific user
    @GetMapping("/user/{userId}/transactions")
    public ResponseEntity<List<TransactionDto>> getTransactionsByUserId(@PathVariable UUID userId) {
        // TODO: Add security check
        List<TransactionDto> transactions = paymentService.getTransactionsByUserId(userId);
        return ResponseEntity.ok(transactions);
    }
}

