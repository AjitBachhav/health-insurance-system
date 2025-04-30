package policyservice.service;

import policyservice.dto.PolicyDto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyService {

    // Consider a CreatePolicyRequest DTO with userId, planId, dependent info etc.
    PolicyDto createPolicy(PolicyDto policyDto);

    Optional<PolicyDto> getPolicyById(UUID policyId);

    Optional<PolicyDto> getPolicyByNumber(String policyNumber);

    List<PolicyDto> getPoliciesByUserId(UUID userId);

    List<PolicyDto> getPoliciesByUserIdAndStatus(UUID userId, String status);

    PolicyDto updatePolicyStatus(UUID policyId, String newStatus);

    // Add methods for managing dependents, documents if needed
    // PolicyDto addDependentToPolicy(UUID policyId, DependentDto dependentDto);
    // PolicyDto addDocumentToPolicy(UUID policyId, PolicyDocumentDto documentDto);

    // Method to generate policy document (e.g., PDF)
    byte[] generatePolicyDocument(UUID policyId, String documentType);
}

