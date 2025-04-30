package paymentservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transaction_id")
    private UUID transactionId;

    @NotNull
    @Column(name = "policy_id") // Logical FK to Policy Service
    private UUID policyId;

    @NotNull
    @Column(name = "user_id") // Logical FK to User Service
    private UUID userId;

    @NotNull
    @Positive
    @Column(nullable = false)
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3)
    @Column(length = 3, nullable = false)
    private String currency; // e.g., "USD"

    @NotBlank
    @Column(name = "transaction_status", nullable = false)
    private String transactionStatus; // e.g., "PENDING", "SUCCESS", "FAILED", "REFUNDED"

    @NotBlank
    @Column(name = "payment_gateway", nullable = false)
    private String paymentGateway; // e.g., "STRIPE", "PAYPAL"

    @Column(name = "gateway_transaction_id", unique = true)
    private String gatewayTransactionId; // ID from the payment gateway

    @Column(name = "payment_method_details", columnDefinition = "TEXT")
    private String paymentMethodDetails; // e.g., last 4 digits, card type (store minimal info)

    @NotBlank
    @Column(name = "transaction_type", nullable = false)
    private String transactionType; // e.g., "PREMIUM_PAYMENT", "REFUND"

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

