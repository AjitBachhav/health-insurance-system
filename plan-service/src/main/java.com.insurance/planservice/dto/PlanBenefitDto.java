package planservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class PlanBenefitDto {
    private UUID planBenefitId;
    // private UUID planId; // Often omitted to avoid redundancy if nested within PlanDto
    private BenefitDto benefit; // Embed the Benefit details
    private String coverageDetails;
    private String limitations;
}

