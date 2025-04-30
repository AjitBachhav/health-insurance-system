package planservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "plan_benefits")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"plan", "benefit"}) // Avoid recursion in equals/hashCode
public class PlanBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "plan_benefit_id")
    private UUID planBenefitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benefit_id", nullable = false)
    private Benefit benefit;

    @Column(name = "coverage_details", columnDefinition = "TEXT")
    private String coverageDetails; // e.g., '80% coinsurance after deductible'

    @Column(columnDefinition = "TEXT")
    private String limitations;
}

