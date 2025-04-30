package planservice.service;

import planservice.dto.BenefitDto;
import planservice.dto.PlanBenefitDto;
import planservice.dto.PlanDto;
import planservice.dto.ProviderNetworkDto;
import planservice.model.Benefit;
import planservice.model.Plan;
import planservice.model.PlanBenefit;
import planservice.model.ProviderNetwork;
import planservice.repository.BenefitRepository;
import planservice.repository.PlanRepository;
import planservice.repository.ProviderNetworkRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final BenefitRepository benefitRepository; // Needed if creating/linking benefits
    private final ProviderNetworkRepository networkRepository; // Needed if linking networks

    @Autowired
    public PlanServiceImpl(PlanRepository planRepository, BenefitRepository benefitRepository, ProviderNetworkRepository networkRepository) {
        this.planRepository = planRepository;
        this.benefitRepository = benefitRepository;
        this.networkRepository = networkRepository;
    }

    @Override
    @Transactional
    public PlanDto createPlan(PlanDto planDto) {
        Plan plan = mapToEntity(planDto);
        plan.setActive(true); // Default to active on creation, or based on DTO

        // Handle benefits and networks if provided in DTO
        // This assumes benefits/networks already exist. More complex logic needed for creation.
        if (planDto.getPlanBenefits() != null) {
            // Logic to find existing benefits and create PlanBenefit entities
        }
        if (planDto.getProviderNetworks() != null) {
            // Logic to find existing networks and link them
        }

        Plan savedPlan = planRepository.save(plan);
        return mapToDto(savedPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlanDto> getPlanById(UUID planId) {
        // Consider fetching associated entities eagerly if always needed
        return planRepository.findById(planId).map(this::mapToDtoWithDetails); // Use detailed mapping
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanDto> getAllPlans(boolean includeInactive) {
        List<Plan> plans = includeInactive ? planRepository.findAll() : planRepository.findByIsActiveTrue();
        return plans.stream()
                .map(this::mapToDto) // Use basic mapping for lists
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanDto> getActivePlans() {
        return planRepository.findByIsActiveTrue().stream()
                .map(this::mapToDto) // Use basic mapping for lists
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PlanDto updatePlan(UUID planId, PlanDto planDto) {
        Plan existingPlan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found with id: " + planId));

        // Update basic fields
        existingPlan.setPlanName(planDto.getPlanName());
        existingPlan.setDescription(planDto.getDescription());
        existingPlan.setBasePremium(planDto.getBasePremium());
        existingPlan.setDeductible(planDto.getDeductible());
        existingPlan.setMaxOutOfPocket(planDto.getMaxOutOfPocket());
        existingPlan.setEligibilityCriteria(planDto.getEligibilityCriteria());
        // existingPlan.setActive(planDto.isActive()); // Use activate/deactivate methods

        // TODO: Add logic to update benefits and networks (add/remove based on DTO)

        Plan updatedPlan = planRepository.save(existingPlan);
        return mapToDtoWithDetails(updatedPlan);
    }

    @Override
    @Transactional
    public void deletePlan(UUID planId) {
        // Consider soft delete (deactivation) instead of hard delete
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found with id: " + planId));
        planRepository.delete(plan);
        // Or: plan.setActive(false); planRepository.save(plan);
    }

    @Override
    @Transactional
    public PlanDto activatePlan(UUID planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found with id: " + planId));
        plan.setActive(true);
        return mapToDto(planRepository.save(plan));
    }

    @Override
    @Transactional
    public PlanDto deactivatePlan(UUID planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found with id: " + planId));
        plan.setActive(false);
        return mapToDto(planRepository.save(plan));
    }

    // --- Helper Methods for Mapping --- 

    // Basic mapping (e.g., for lists)
    private PlanDto mapToDto(Plan plan) {
        PlanDto dto = new PlanDto();
        dto.setPlanId(plan.getPlanId());
        dto.setPlanName(plan.getPlanName());
        dto.setDescription(plan.getDescription());
        dto.setBasePremium(plan.getBasePremium());
        dto.setDeductible(plan.getDeductible());
        dto.setMaxOutOfPocket(plan.getMaxOutOfPocket());
        dto.setEligibilityCriteria(plan.getEligibilityCriteria());
        dto.setActive(plan.isActive());
        dto.setCreatedAt(plan.getCreatedAt());
        dto.setUpdatedAt(plan.getUpdatedAt());
        // Intentionally omit collections for list views for performance
        return dto;
    }

    // Detailed mapping (e.g., for single plan view)
    private PlanDto mapToDtoWithDetails(Plan plan) {
        PlanDto dto = mapToDto(plan); // Start with basic mapping
        if (plan.getPlanBenefits() != null) {
            dto.setPlanBenefits(plan.getPlanBenefits().stream()
                    .map(this::mapPlanBenefitToDto)
                    .collect(Collectors.toSet()));
        }
        if (plan.getProviderNetworks() != null) {
            dto.setProviderNetworks(plan.getProviderNetworks().stream()
                    .map(this::mapProviderNetworkToDto)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }

    private Plan mapToEntity(PlanDto dto) {
        Plan plan = new Plan();
        // planId is generated
        plan.setPlanName(dto.getPlanName());
        plan.setDescription(dto.getDescription());
        plan.setBasePremium(dto.getBasePremium());
        plan.setDeductible(dto.getDeductible());
        plan.setMaxOutOfPocket(dto.getMaxOutOfPocket());
        plan.setEligibilityCriteria(dto.getEligibilityCriteria());
        plan.setActive(dto.isActive()); // Or set default
        // Collections (benefits, networks) need separate handling during creation/update
        return plan;
    }

    private PlanBenefitDto mapPlanBenefitToDto(PlanBenefit planBenefit) {
        PlanBenefitDto dto = new PlanBenefitDto();
        dto.setPlanBenefitId(planBenefit.getPlanBenefitId());
        dto.setCoverageDetails(planBenefit.getCoverageDetails());
        dto.setLimitations(planBenefit.getLimitations());
        if (planBenefit.getBenefit() != null) {
            dto.setBenefit(mapBenefitToDto(planBenefit.getBenefit()));
        }
        return dto;
    }

    private BenefitDto mapBenefitToDto(Benefit benefit) {
        BenefitDto dto = new BenefitDto();
        dto.setBenefitId(benefit.getBenefitId());
        dto.setBenefitName(benefit.getBenefitName());
        dto.setDescription(benefit.getDescription());
        return dto;
    }

    private ProviderNetworkDto mapProviderNetworkToDto(ProviderNetwork network) {
        ProviderNetworkDto dto = new ProviderNetworkDto();
        dto.setNetworkId(network.getNetworkId());
        dto.setNetworkName(network.getNetworkName());
        dto.setDescription(network.getDescription());
        return dto;
    }

    // mapBenefitToEntity, mapProviderNetworkToEntity might be needed if creating via Plan endpoint
}

