package planservice.service;

import planservice.dto.PlanDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanService {

    PlanDto createPlan(PlanDto planDto); // Consider CreatePlanRequest DTO

    Optional<PlanDto> getPlanById(UUID planId);

    List<PlanDto> getAllPlans(boolean includeInactive);

    List<PlanDto> getActivePlans();

    PlanDto updatePlan(UUID planId, PlanDto planDto); // Consider UpdatePlanRequest DTO

    void deletePlan(UUID planId); // Or deactivate plan

    PlanDto activatePlan(UUID planId);

    PlanDto deactivatePlan(UUID planId);

    // Methods for managing benefits and networks associated with a plan might be added here
    // e.g., addBenefitToPlan, removeBenefitFromPlan, addNetworkToPlan, removeNetworkFromPlan
}

