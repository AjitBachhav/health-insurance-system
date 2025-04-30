package policyservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "policy_holders")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "policy") // Avoid recursion
public class PolicyHolder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "policy_holder_id")
    private UUID policyHolderId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @NotNull
    @Column(name = "user_id") // Logical FK to User Service
    private UUID userId;

    // Denormalized/Snapshot data from User Service at time of policy creation
    @NotBlank
    @Column(name = "first_name")
    private String firstName;

    @NotBlank
    @Column(name = "last_name")
    private String lastName;

    @NotNull
    @Past
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @NotBlank
    @Column(name = "relationship_to_subscriber", nullable = false)
    private String relationshipToSubscriber = "SELF"; // Usually the user themselves
}

