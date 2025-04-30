package claimsservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "claims")
@Data
@NoArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "claim_id")
    private UUID claimId;

    @NotBlank
    @Column(name = "claim_number", unique = true, nullable = false)
    private String claimNumber;

    @NotNull
    @Column(name = "policy_id") // Logical FK to Policy Service
    private UUID policyId;

    @NotNull
    @Column(name = "user_id") // Logical FK to User Service (Claimant/Subscriber)
    private UUID userId;

    @Column(name = "dependent_id") // Logical FK to Policy Service (Dependent, if applicable)
    private UUID dependentId;

    @NotNull
    @PastOrPresent
    @Column(name = "date_of_service", nullable = false)
    private LocalDate dateOfService;

    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "diagnosis_code") // e.g., ICD-10 code
    private String diagnosisCode;

    @Column(name = "procedure_code") // e.g., CPT code
    private String procedureCode;

    @NotNull
    @PositiveOrZero
    @Column(name = "amount_claimed", nullable = false)
    private BigDecimal amountClaimed;

    @PositiveOrZero
    @Column(name = "amount_paid", columnDefinition = "DECIMAL DEFAULT 0.00")
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @NotBlank
    @Column(name = "claim_status", nullable = false)
    private String claimStatus; // e.g., "SUBMITTED", "PROCESSING", "APPROVED", "DENIED", "PAID"

    @CreationTimestamp
    @Column(name = "submission_date", updatable = false)
    private LocalDateTime submissionDate;

    @UpdateTimestamp
    @Column(name = "last_updated_date")
    private LocalDateTime lastUpdatedDate;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClaimDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("changedAt DESC") // Show latest status first
    private List<ClaimStatusHistory> statusHistory = new ArrayList<>();

    // Convenience methods
    public void addDocument(ClaimDocument document) {
        documents.add(document);
        document.setClaim(this);
    }

    public void removeDocument(ClaimDocument document) {
        documents.remove(document);
        document.setClaim(null);
    }

    public void addStatusHistory(ClaimStatusHistory historyEntry) {
        statusHistory.add(historyEntry);
        historyEntry.setClaim(this);
    }
}

