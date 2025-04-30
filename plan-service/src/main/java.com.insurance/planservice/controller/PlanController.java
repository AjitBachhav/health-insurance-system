package planservice.controller;

import planservice.dto.PlanDto;
import planservice.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plans") // Base path for plan endpoints
public class PlanController {

    private final PlanService planService;

    @Autowired
    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    // Endpoint to create a new plan
    @PostMapping
    public ResponseEntity<PlanDto> createPlan(@Valid @RequestBody PlanDto planDto) {
        // TODO: Use a dedicated CreatePlanRequest DTO
        // TODO: Add validation
        // TODO: Add security check (e.g., only ADMIN role)
        PlanDto createdPlan = planService.createPlan(planDto);
        return new ResponseEntity<>(createdPlan, HttpStatus.CREATED);
    }

    // Endpoint to get all plans (optionally include inactive)
    @GetMapping
    public ResponseEntity<List<PlanDto>> getAllPlans(@RequestParam(defaultValue = "false") boolean includeInactive) {
        // Public endpoint, or add security if needed
        List<PlanDto> plans = planService.getAllPlans(includeInactive);
        return ResponseEntity.ok(plans);
    }

    // Endpoint to get active plans only
    @GetMapping("/active")
    public ResponseEntity<List<PlanDto>> getActivePlans() {
        // Public endpoint
        List<PlanDto> plans = planService.getActivePlans();
        return ResponseEntity.ok(plans);
    }

    // Endpoint to get a plan by ID
    @GetMapping("/{planId}")
    public ResponseEntity<PlanDto> getPlanById(@PathVariable UUID planId) {
        // Public endpoint, or add security if needed
        return planService.getPlanById(planId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint to update a plan
    @PutMapping("/{planId}")
    public ResponseEntity<PlanDto> updatePlan(@PathVariable UUID planId, @Valid @RequestBody PlanDto planDto) {
        // TODO: Use a dedicated UpdatePlanRequest DTO
        // TODO: Add security check (e.g., only ADMIN role)
        try {
            PlanDto updatedPlan = planService.updatePlan(planId, planDto);
            return ResponseEntity.ok(updatedPlan);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
        // Add handling for other potential exceptions
    }

    // Endpoint to deactivate a plan
    @PatchMapping("/{planId}/deactivate")
    public ResponseEntity<PlanDto> deactivatePlan(@PathVariable UUID planId) {
        // TODO: Add security check (e.g., only ADMIN role)
        try {
            PlanDto deactivatedPlan = planService.deactivatePlan(planId);
            return ResponseEntity.ok(deactivatedPlan);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint to activate a plan
    @PatchMapping("/{planId}/activate")
    public ResponseEntity<PlanDto> activatePlan(@PathVariable UUID planId) {
        // TODO: Add security check (e.g., only ADMIN role)
        try {
            PlanDto activatedPlan = planService.activatePlan(planId);
            return ResponseEntity.ok(activatedPlan);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint to delete a plan (use with caution, prefer deactivation)
    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> deletePlan(@PathVariable UUID planId) {
        // TODO: Add security check (e.g., only ADMIN role)
        try {
            planService.deletePlan(planId);
            return ResponseEntity.noContent().build();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // TODO: Add endpoints for managing plan benefits and networks if needed
}

