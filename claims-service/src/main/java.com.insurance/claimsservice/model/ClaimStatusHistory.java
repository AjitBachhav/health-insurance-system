package claimsservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "claim_status_history")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "claim") // Avoid recursion
public class ClaimStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "history_id")
    private UUID historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @NotBlank
    @Column(nullable = false)
    private String status; // The status being recorded (e.g., "SUBMITTED", "PROCESSING")

    @Column(columnDefinition = "TEXT")
    private String notes; // Optional notes related to the status change

    @CreationTimestamp
    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;

    // Optional: Add user ID who made the change if tracking is needed
    // private UUID changedByUserId;

    public ClaimStatusHistory(Claim claim, String status, String notes) {
        this.claim = claim;
        this.status = status;
        this.notes = notes;
    }
}

