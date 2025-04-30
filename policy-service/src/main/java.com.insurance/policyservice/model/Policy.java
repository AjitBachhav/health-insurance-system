package policyservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Table(name = "policies")
@Data
@NoArgsConstructor
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "policy_id")
    private UUID policyId;

    @NotBlank
    @Column(name = "policy_number", unique = true, nullable = false)
    private String policyNumber;

    @NotNull
    @Column(name = "user_id", nullable = false) // Logical FK to User Service
    private UUID userId;

    @NotNull
    @Column(name = "plan_id", nullable = false) // Logical FK to Plan Service
    private UUID planId;

    @NotNull
    @FutureOrPresent
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @NotNull
    @Future
    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @NotBlank
    @Column(name = "policy_status", nullable = false)
    private String policyStatus; // e.g., 'ACTIVE', 'PENDING', 'EXPIRED', 'CANCELLED'

    @NotNull
    @Positive
    @Column(name = "premium_amount", nullable = false)
    private BigDecimal premiumAmount;

    @NotBlank
    @Column(name = "payment_frequency", nullable = false)
    private String paymentFrequency; // e.g., 'MONTHLY', 'ANNUALLY'

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // One policy holder (the primary subscriber)
    @OneToOne(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PolicyHolder policyHolder;

    // Multiple dependents
    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Dependent> dependents = new ArrayList<>();

    // Policy documents
    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PolicyDocument> documents = new ArrayList<>();

    // Convenience methods
    public void addDependent(Dependent dependent) {
        dependents.add(dependent);
        dependent.setPolicy(this);
    }

    public void removeDependent(Dependent dependent) {
        dependents.remove(dependent);
        dependent.setPolicy(null);
    }

    public void addDocument(PolicyDocument document) {
        documents.add(document);
        document.setPolicy(this);
    }

    public void removeDocument(PolicyDocument document) {
        documents.remove(document);
        document.setPolicy(null);
    }
}

