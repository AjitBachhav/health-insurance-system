package claimsservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ClaimDocumentDto {
    private UUID documentId;
    // private UUID claimId; // Omit if nested
    private String documentName;
    private String documentType;
    private String documentUrl;
    private LocalDateTime uploadedAt;
}

