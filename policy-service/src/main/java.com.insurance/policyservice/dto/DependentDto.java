package policyservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
public class DependentDto {
    private UUID dependentId;
    // private UUID policyId; // Omit if nested
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String relationshipToPolicyHolder;
}

