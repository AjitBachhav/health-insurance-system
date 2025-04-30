package claimsservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ClaimDto {
    private UUID claimId;
    private String claimNumber;
    private UUID policyId;
    private UUID userId;
    private UUID dependentId; // Nullable
    private LocalDate dateOfService;
    private String providerName;
    private String diagnosisCode;
    private String procedureCode;
    private BigDecimal amountClaimed;
    private BigDecimal amountPaid;
    private String claimStatus;
    private LocalDateTime submissionDate;
    private LocalDateTime lastUpdatedDate;
    private List<ClaimDocumentDto> documents;
    private List<ClaimStatusHistoryDto> statusHistory;
}

