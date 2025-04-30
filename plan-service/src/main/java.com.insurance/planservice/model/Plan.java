package planservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "plans")
@Data
@NoArgsConstructor
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "plan_id")
    private UUID planId;

    @NotBlank
    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @PositiveOrZero
    @Column(name = "base_premium", nullable = false)
    private BigDecimal basePremium;

    @PositiveOrZero
    private BigDecimal deductible;

    @PositiveOrZero
    @Column(name = "max_out_of_pocket")
    private BigDecimal maxOutOfPocket;

    @Column(name = "eligibility_criteria", columnDefinition = "TEXT")
    private String eligibilityCriteria;

    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT true")
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PlanBenefit> planBenefits = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "plan_networks",
            joinColumns = @JoinColumn(name = "plan_id"),
            inverseJoinColumns = @JoinColumn(name = "network_id")
    )
    private Set<ProviderNetwork> providerNetworks = new HashSet<>();

    // Convenience methods to manage relationships if needed
    public void addPlanBenefit(PlanBenefit planBenefit) {
        planBenefits.add(planBenefit);
        planBenefit.setPlan(this);
    }

    public void removePlanBenefit(PlanBenefit planBenefit) {
        planBenefits.remove(planBenefit);
        planBenefit.setPlan(null);
    }

    public void addProviderNetwork(ProviderNetwork network) {
        providerNetworks.add(network);
        network.getPlans().add(this);
    }

    public void removeProviderNetwork(ProviderNetwork network) {
        providerNetworks.remove(network);
        network.getPlans().remove(this);
    }
}

