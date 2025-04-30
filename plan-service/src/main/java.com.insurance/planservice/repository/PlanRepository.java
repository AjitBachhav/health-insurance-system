package planservice.repository;

import planservice.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {
    // Find active plans
    List<Plan> findByIsActiveTrue();

    // Custom query example (if needed later)
    // @Query("SELECT p FROM Plan p JOIN FETCH p.planBenefits pb JOIN FETCH pb.benefit WHERE p.isActive = true")
    // List<Plan> findAllActivePlansWithBenefits();
}

