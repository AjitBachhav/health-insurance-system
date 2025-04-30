package policyservice.service;

import policyservice.dto.*;
import policyservice.model.Dependent;
import policyservice.model.Policy;
import policyservice.model.PolicyDocument;
import policyservice.model.PolicyHolder;
import policyservice.repository.PolicyRepository;
import policyservice.util.PdfGeneratorUtil; // To be created
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final PdfGeneratorUtil pdfGeneratorUtil; // Inject PDF utility

    // TODO: Inject Kafka/RabbitMQ template if publishing events

    @Autowired
    public PolicyServiceImpl(PolicyRepository policyRepository, PdfGeneratorUtil pdfGeneratorUtil) {
        this.policyRepository = policyRepository;
        this.pdfGeneratorUtil = pdfGeneratorUtil;
    }

    @Override
    @Transactional
    public PolicyDto createPolicy(PolicyDto policyDto) {
        // TODO: Add validation - check if user and plan exist (via API call or event sourcing)
        Policy policy = mapToEntity(policyDto);

        // Generate unique policy number
        policy.setPolicyNumber(generatePolicyNumber());
        policy.setPolicyStatus("PENDING"); // Initial status, update after payment

        // Ensure bidirectional relationships are set correctly
        if (policy.getPolicyHolder() != null) {
            policy.getPolicyHolder().setPolicy(policy);
        }
        if (policy.getDependents() != null) {
            policy.getDependents().forEach(dep -> dep.setPolicy(policy));
        }
        // Documents are usually added later (e.g., after generation)
        policy.setDocuments(null); // Clear any documents from input DTO

        Policy savedPolicy = policyRepository.save(policy);

        // TODO: Publish PolicyCreatedEvent if using event-driven architecture

        return mapToDtoWithDetails(savedPolicy);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PolicyDto> getPolicyById(UUID policyId) {
        return policyRepository.findById(policyId).map(this::mapToDtoWithDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PolicyDto> getPolicyByNumber(String policyNumber) {
        return policyRepository.findByPolicyNumber(policyNumber).map(this::mapToDtoWithDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolicyDto> getPoliciesByUserId(UUID userId) {
        return policyRepository.findByUserId(userId).stream()
                .map(this::mapToDto) // Use basic mapping for lists
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolicyDto> getPoliciesByUserIdAndStatus(UUID userId, String status) {
        return policyRepository.findByUserIdAndPolicyStatus(userId, status).stream()
                .map(this::mapToDto) // Use basic mapping for lists
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PolicyDto updatePolicyStatus(UUID policyId, String newStatus) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new EntityNotFoundException("Policy not found with id: " + policyId));
        policy.setPolicyStatus(newStatus);
        // TODO: Add validation for status transitions
        // TODO: Publish PolicyStatusUpdatedEvent
        Policy updatedPolicy = policyRepository.save(policy);
        return mapToDto(updatedPolicy);
    }

    @Override
    @Transactional(readOnly = true) // Read-only as it doesn't modify policy state directly
    public byte[] generatePolicyDocument(UUID policyId, String documentType) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new EntityNotFoundException("Policy not found with id: " + policyId));

        // Fetch necessary data eagerly if needed or handle lazy loading
        PolicyDto policyDto = mapToDtoWithDetails(policy); // Get full details for PDF

        // TODO: Fetch Plan details and User details via API calls or cached data if needed for the document
        // PlanDto planDto = ... // Call Plan Service
        // UserDto userDto = ... // Call User Service

        try {
            // Pass necessary data to the PDF generator
            return pdfGeneratorUtil.generatePolicyPdf(policyDto /*, planDto, userDto */);
        } catch (Exception e) {
            // Log error
            throw new RuntimeException("Error generating policy document for policyId: " + policyId, e);
        }
        // Note: Saving the document URL to PolicyDocument entity should likely happen
        // in a separate step, perhaps after successful generation and storage (e.g., S3 upload).
    }

    // --- Helper Methods --- 

    private String generatePolicyNumber() {
        // Simple example: Prefix + timestamp + random digits
        // Replace with a more robust generation strategy if needed
        return "POL-" + System.currentTimeMillis() + "-" + String.format("%04d", (int) (Math.random() * 10000));
    }

    // Basic mapping (for lists)
    private PolicyDto mapToDto(Policy policy) {
        PolicyDto dto = new PolicyDto();
        dto.setPolicyId(policy.getPolicyId());
        dto.setPolicyNumber(policy.getPolicyNumber());
        dto.setUserId(policy.getUserId());
        dto.setPlanId(policy.getPlanId());
        dto.setEffectiveDate(policy.getEffectiveDate());
        dto.setExpirationDate(policy.getExpirationDate());
        dto.setPolicyStatus(policy.getPolicyStatus());
        dto.setPremiumAmount(policy.getPremiumAmount());
        dto.setPaymentFrequency(policy.getPaymentFrequency());
        dto.setCreatedAt(policy.getCreatedAt());
        dto.setUpdatedAt(policy.getUpdatedAt());
        // Omit collections for list view
        return dto;
    }

    // Detailed mapping (for single view)
    private PolicyDto mapToDtoWithDetails(Policy policy) {
        PolicyDto dto = mapToDto(policy);
        if (policy.getPolicyHolder() != null) {
            dto.setPolicyHolder(mapPolicyHolderToDto(policy.getPolicyHolder()));
        }
        if (policy.getDependents() != null) {
            dto.setDependents(policy.getDependents().stream().map(this::mapDependentToDto).collect(Collectors.toList()));
        }
        if (policy.getDocuments() != null) {
            dto.setDocuments(policy.getDocuments().stream().map(this::mapPolicyDocumentToDto).collect(Collectors.toList()));
        }
        return dto;
    }

    private Policy mapToEntity(PolicyDto dto) {
        Policy policy = new Policy();
        // policyId is generated
        policy.setUserId(dto.getUserId());
        policy.setPlanId(dto.getPlanId());
        policy.setEffectiveDate(dto.getEffectiveDate());
        policy.setExpirationDate(dto.getExpirationDate());
        policy.setPremiumAmount(dto.getPremiumAmount());
        policy.setPaymentFrequency(dto.getPaymentFrequency());
        // PolicyNumber, PolicyStatus, CreatedAt, UpdatedAt are set internally

        if (dto.getPolicyHolder() != null) {
            policy.setPolicyHolder(mapPolicyHolderToEntity(dto.getPolicyHolder()));
        }
        if (dto.getDependents() != null) {
            policy.setDependents(dto.getDependents().stream().map(this::mapDependentToEntity).collect(Collectors.toList()));
        }
        // Documents are typically managed separately

        return policy;
    }

    private PolicyHolderDto mapPolicyHolderToDto(PolicyHolder entity) {
        PolicyHolderDto dto = new PolicyHolderDto();
        dto.setPolicyHolderId(entity.getPolicyHolderId());
        dto.setUserId(entity.getUserId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setRelationshipToSubscriber(entity.getRelationshipToSubscriber());
        return dto;
    }

    private PolicyHolder mapPolicyHolderToEntity(PolicyHolderDto dto) {
        PolicyHolder entity = new PolicyHolder();
        // policyHolderId is generated
        entity.setUserId(dto.getUserId());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setRelationshipToSubscriber(dto.getRelationshipToSubscriber());
        // Policy link is set in createPolicy
        return entity;
    }

    private DependentDto mapDependentToDto(Dependent entity) {
        DependentDto dto = new DependentDto();
        dto.setDependentId(entity.getDependentId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setRelationshipToPolicyHolder(entity.getRelationshipToPolicyHolder());
        return dto;
    }

    private Dependent mapDependentToEntity(DependentDto dto) {
        Dependent entity = new Dependent();
        // dependentId is generated
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setRelationshipToPolicyHolder(dto.getRelationshipToPolicyHolder());
        // Policy link is set in createPolicy
        return entity;
    }

    private PolicyDocumentDto mapPolicyDocumentToDto(PolicyDocument entity) {
        PolicyDocumentDto dto = new PolicyDocumentDto();
        dto.setDocumentId(entity.getDocumentId());
        dto.setDocumentType(entity.getDocumentType());
        dto.setDocumentUrl(entity.getDocumentUrl());
        dto.setGeneratedAt(entity.getGeneratedAt());
        return dto;
    }

    // mapPolicyDocumentToEntity might be needed if creating documents via API
}

