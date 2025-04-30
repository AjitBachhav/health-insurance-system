package policyservice.controller;

import policyservice.dto.PolicyDto;
import policyservice.service.PolicyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/policies") // Base path for policy endpoints
public class PolicyController {

    private final PolicyService policyService;

    @Autowired
    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    // Endpoint to create a new policy
    @PostMapping
    public ResponseEntity<PolicyDto> createPolicy(@Valid @RequestBody PolicyDto policyDto) {
        // TODO: Use a dedicated CreatePolicyRequest DTO
        // TODO: Add validation (e.g., ensure userId and planId are provided)
        // TODO: Add security check (e.g., user can create policy for themselves)
        PolicyDto createdPolicy = policyService.createPolicy(policyDto);
        return new ResponseEntity<>(createdPolicy, HttpStatus.CREATED);
    }

    // Endpoint to get a policy by ID
    @GetMapping("/{policyId}")
    public ResponseEntity<PolicyDto> getPolicyById(@PathVariable UUID policyId) {
        // TODO: Add security check (user can view own policy, admin can view any)
        return policyService.getPolicyById(policyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint to get a policy by policy number
    @GetMapping("/number/{policyNumber}")
    public ResponseEntity<PolicyDto> getPolicyByNumber(@PathVariable String policyNumber) {
        // TODO: Add security check
        return policyService.getPolicyByNumber(policyNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint to get policies for a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PolicyDto>> getPoliciesByUserId(@PathVariable UUID userId, @RequestParam(required = false) String status) {
        // TODO: Add security check (user can view own policies, admin can view any)
        List<PolicyDto> policies;
        if (status != null && !status.isBlank()) {
            policies = policyService.getPoliciesByUserIdAndStatus(userId, status);
        } else {
            policies = policyService.getPoliciesByUserId(userId);
        }
        return ResponseEntity.ok(policies);
    }

    // Endpoint to update policy status
    @PatchMapping("/{policyId}/status")
    public ResponseEntity<PolicyDto> updatePolicyStatus(@PathVariable UUID policyId, @RequestBody String newStatus) {
        // TODO: Use a dedicated DTO for status update
        // TODO: Add validation for status value
        // TODO: Add security check (e.g., internal service or admin)
        try {
            // Basic validation: ensure status is not null or empty
            if (newStatus == null || newStatus.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            // Remove potential quotes if status is sent as JSON string
            String cleanStatus = newStatus.replace("\"", "").trim();
            PolicyDto updatedPolicy = policyService.updatePolicyStatus(policyId, cleanStatus);
            return ResponseEntity.ok(updatedPolicy);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
        // Add handling for other potential exceptions (e.g., invalid status transition)
    }

    // Endpoint to generate a policy document (e.g., PDF)
    @GetMapping("/{policyId}/documents/{documentType}")
    public ResponseEntity<byte[]> generatePolicyDocument(@PathVariable UUID policyId, @PathVariable String documentType) {
        // TODO: Add security check
        // TODO: Validate documentType
        try {
            byte[] pdfBytes = policyService.generatePolicyDocument(policyId, documentType);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // Suggest filename for download
            String filename = "policy-" + policyId + "-" + documentType.toLowerCase() + ".pdf";
            headers.setContentDispositionFormData(filename, filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Log the error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // TODO: Add endpoints for managing dependents and documents if needed

}

