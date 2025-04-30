package policyservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
public class PolicyHolderDto {
    private UUID policyHolderId;
    // private UUID policyId; // Omit if nested
    private UUID userId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String relationshipToSubscriber;
}

