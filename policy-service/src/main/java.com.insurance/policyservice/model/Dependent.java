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
@Table(name = "dependents")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "policy") // Avoid recursion
public class Dependent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "dependent_id")
    private UUID dependentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @NotBlank
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotNull
    @Past
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank
    @Column(name = "relationship_to_policy_holder", nullable = false)
    private String relationshipToPolicyHolder; // e.g., 'SPOUSE', 'CHILD'
}

