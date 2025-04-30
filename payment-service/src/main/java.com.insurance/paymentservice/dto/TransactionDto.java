package paymentservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class TransactionDto {
    private UUID transactionId;
    private UUID policyId;
    private UUID userId;
    private BigDecimal amount;
    private String currency;
    private String transactionStatus;
    private String paymentGateway;
    private String gatewayTransactionId;
    private String paymentMethodDetails;
    private String transactionType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

