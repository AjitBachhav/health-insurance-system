package claimsservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ClaimStatusHistoryDto {
    private UUID historyId;
    // private UUID claimId; // Omit if nested
    private String status;
    private String notes;
    private LocalDateTime changedAt;
}

