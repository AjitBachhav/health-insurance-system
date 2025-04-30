package policyservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class PolicyDocumentDto {
    private UUID documentId;
    // private UUID policyId; // Omit if nested
    private String documentType;
    private String documentUrl;
    private LocalDateTime generatedAt;
}

