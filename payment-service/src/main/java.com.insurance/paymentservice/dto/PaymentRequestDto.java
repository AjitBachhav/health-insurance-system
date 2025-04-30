package paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
public class PaymentRequestDto {

    @NotNull
    private UUID policyId;

    @NotNull
    private UUID userId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String currency; // e.g., "usd"

    @NotBlank
    private String paymentMethodId; // e.g., Stripe PaymentMethod ID (pm_...)

    private String description; // Optional description for the payment
}

