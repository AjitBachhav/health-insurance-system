package planservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class PlanDto {
    private UUID planId;
    private String planName;
    private String description;
    private BigDecimal basePremium;
    private BigDecimal deductible;
    private BigDecimal maxOutOfPocket;
    private String eligibilityCriteria;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<PlanBenefitDto> planBenefits;
    private Set<ProviderNetworkDto> providerNetworks;
}

