package paymentservice.service;

import paymentservice.dto.PaymentRequestDto;
import paymentservice.dto.TransactionDto;
import com.stripe.exception.StripeException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentService {

    /**
     * Processes a payment using the configured payment gateway (e.g., Stripe).
     *
     * @param paymentRequest Details of the payment to be processed.
     * @return TransactionDto representing the initial state of the transaction (likely PENDING).
     * @throws StripeException if there is an error communicating with the payment gateway.
     */
    TransactionDto processPayment(PaymentRequestDto paymentRequest) throws StripeException;

    /**
     * Handles webhook events from the payment gateway (e.g., payment success, failure).
     *
     * @param payload The raw payload received from the webhook.
     * @param signatureHeader The signature header (e.g., Stripe-Signature) for verification.
     * @return TransactionDto representing the updated transaction state.
     * @throws StripeException if there is an error processing the webhook or verifying the signature.
     */
    TransactionDto handleWebhook(String payload, String signatureHeader) throws StripeException;

    /**
     * Retrieves a transaction by its unique ID.
     *
     * @param transactionId The ID of the transaction.
     * @return An Optional containing the TransactionDto if found, otherwise empty.
     */
    Optional<TransactionDto> getTransactionById(UUID transactionId);

    /**
     * Retrieves all transactions associated with a specific policy ID.
     *
     * @param policyId The ID of the policy.
     * @return A list of TransactionDto objects.
     */
    List<TransactionDto> getTransactionsByPolicyId(UUID policyId);

    /**
     * Retrieves all transactions associated with a specific user ID.
     *
     * @param userId The ID of the user.
     * @return A list of TransactionDto objects.
     */
    List<TransactionDto> getTransactionsByUserId(UUID userId);

    // Optional: Add methods for refunds if needed
    // TransactionDto processRefund(UUID originalTransactionId, BigDecimal amount) throws StripeException;
}


