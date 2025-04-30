package planservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class BenefitDto {
    private UUID benefitId;
    private String benefitName;
    private String description;
}

