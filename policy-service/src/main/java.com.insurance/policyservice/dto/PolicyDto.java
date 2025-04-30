package policyservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class PolicyDto {
    private UUID policyId;
    private String policyNumber;
    private UUID userId;
    private UUID planId;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private String policyStatus;
    private BigDecimal premiumAmount;
    private String paymentFrequency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PolicyHolderDto policyHolder;
    private List<DependentDto> dependents;
    private List<PolicyDocumentDto> documents;
}

