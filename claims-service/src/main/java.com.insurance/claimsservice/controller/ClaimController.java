package claimsservice.controller;

import claimsservice.dto.ClaimDto;
import claimsservice.service.ClaimService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/claims") // Base path for claims endpoints
public class ClaimController {

    private static final Logger log = LoggerFactory.getLogger(ClaimController.class);

    private final ClaimService claimService;

    @Autowired
    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    // Endpoint to submit a new claim (potentially with documents)
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ClaimDto> submitClaim(@RequestPart("claim") @Valid ClaimDto claimDto,
                                                @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        // TODO: Add security check (user can submit claim for own policy/dependents)
        log.info("Received claim submission request for policyId: {}", claimDto.getPolicyId());
        try {
            ClaimDto submittedClaim = claimService.submitClaim(claimDto, files);
            return new ResponseEntity<>(submittedClaim, HttpStatus.CREATED);
        } catch (IOException e) {
            log.error("Error processing submitted claim documents: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Avoid exposing raw error
        } catch (Exception e) {
            log.error("Unexpected error during claim submission: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Endpoint to get a claim by ID
    @GetMapping("/{claimId}")
    public ResponseEntity<ClaimDto> getClaimById(@PathVariable UUID claimId) {
        // TODO: Add security check
        return claimService.getClaimById(claimId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint to get a claim by claim number
    @GetMapping("/number/{claimNumber}")
    public ResponseEntity<ClaimDto> getClaimByNumber(@PathVariable String claimNumber) {
        // TODO: Add security check
        return claimService.getClaimByNumber(claimNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint to get claims for a specific policy
    @GetMapping("/policy/{policyId}")
    public ResponseEntity<List<ClaimDto>> getClaimsByPolicyId(@PathVariable UUID policyId) {
        // TODO: Add security check
        List<ClaimDto> claims = claimService.getClaimsByPolicyId(policyId);
        return ResponseEntity.ok(claims);
    }

    // Endpoint to get claims for a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ClaimDto>> getClaimsByUserId(@PathVariable UUID userId) {
        // TODO: Add security check
        List<ClaimDto> claims = claimService.getClaimsByUserId(userId);
        return ResponseEntity.ok(claims);
    }

    // Endpoint to update claim status
    @PatchMapping("/{claimId}/status")
    public ResponseEntity<ClaimDto> updateClaimStatus(@PathVariable UUID claimId, @RequestBody Map<String, String> statusUpdate) {
        // TODO: Add security check (e.g., only internal service or admin)
        String newStatus = statusUpdate.get("status");
        String notes = statusUpdate.get("notes");

        if (newStatus == null || newStatus.isBlank()) {
            log.warn("Claim status update request for {} missing status.", claimId);
            return ResponseEntity.badRequest().build();
        }

        try {
            ClaimDto updatedClaim = claimService.updateClaimStatus(claimId, newStatus, notes);
            return ResponseEntity.ok(updatedClaim);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) { // Catch potential validation errors for status transitions
            log.error("Error updating claim status for {}: {}", claimId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Endpoint to add a document to an existing claim
    @PostMapping(value = "/{claimId}/documents", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ClaimDto> addDocumentToClaim(@PathVariable UUID claimId,
                                                       @RequestParam("file") MultipartFile file,
                                                       @RequestParam("documentType") String documentType) {
        // TODO: Add security check
        log.info("Received request to add document to claimId: {}", claimId);
        try {
            ClaimDto updatedClaim = claimService.addDocumentToClaim(claimId, file, documentType);
            return ResponseEntity.ok(updatedClaim);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Error uploading document for claim {}: {}", claimId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            log.error("Unexpected error adding document to claim {}: {}", claimId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Endpoint to retrieve a specific claim document
    @GetMapping("/{claimId}/documents/{documentId}")
    public ResponseEntity<byte[]> getClaimDocument(@PathVariable UUID claimId, @PathVariable UUID documentId) {
        // TODO: Add security check
        try {
            byte[] documentBytes = claimService.getClaimDocument(claimId, documentId);

            // Try to determine content type (basic example)
            // A more robust solution would store content type or use Tika
            MediaType contentType = MediaType.APPLICATION_OCTET_STREAM; // Default
            // Simple check based on common extensions (needs improvement)
            // String filename = ... // Need filename from service/DB if possible
            // if (filename != null) { ... check extension ... }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            // Optional: Suggest filename for download
            // headers.setContentDispositionFormData("attachment", "document-" + documentId);
            headers.setContentLength(documentBytes.length);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(documentBytes, headers, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Error retrieving document {} for claim {}: {}", documentId, claimId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Unexpected error retrieving document {} for claim {}: {}", documentId, claimId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

