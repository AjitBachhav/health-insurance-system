package claimsservice.service;

import claimsservice.dto.ClaimDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClaimService {

    /**
     * Submits a new claim.
     *
     * @param claimDto DTO containing claim details (policyId, userId, dateOfService, amount, etc.).
     * @param files Optional list of supporting documents (e.g., receipts, reports).
     * @return The created ClaimDto with initial status (e.g., SUBMITTED).
     * @throws IOException If there is an error handling file uploads.
     */
    ClaimDto submitClaim(ClaimDto claimDto, List<MultipartFile> files) throws IOException;

    /**
     * Retrieves a claim by its unique ID.
     *
     * @param claimId The ID of the claim.
     * @return An Optional containing the ClaimDto if found, otherwise empty.
     */
    Optional<ClaimDto> getClaimById(UUID claimId);

    /**
     * Retrieves a claim by its unique claim number.
     *
     * @param claimNumber The claim number.
     * @return An Optional containing the ClaimDto if found, otherwise empty.
     */
    Optional<ClaimDto> getClaimByNumber(String claimNumber);

    /**
     * Retrieves all claims associated with a specific policy ID.
     *
     * @param policyId The ID of the policy.
     * @return A list of ClaimDto objects.
     */
    List<ClaimDto> getClaimsByPolicyId(UUID policyId);

    /**
     * Retrieves all claims submitted by or for a specific user ID.
     *
     * @param userId The ID of the user.
     * @return A list of ClaimDto objects.
     */
    List<ClaimDto> getClaimsByUserId(UUID userId);

    /**
     * Updates the status of an existing claim.
     *
     * @param claimId The ID of the claim to update.
     * @param newStatus The new status (e.g., "PROCESSING", "APPROVED", "DENIED").
     * @param notes Optional notes regarding the status change.
     * @return The updated ClaimDto.
     */
    ClaimDto updateClaimStatus(UUID claimId, String newStatus, String notes);

    /**
     * Adds a document to an existing claim.
     *
     * @param claimId The ID of the claim.
     * @param file The document file to upload.
     * @param documentType The type of the document (e.g., "RECEIPT").
     * @return The updated ClaimDto with the new document added.
     * @throws IOException If there is an error handling the file upload.
     */
    ClaimDto addDocumentToClaim(UUID claimId, MultipartFile file, String documentType) throws IOException;

    /**
     * Retrieves a specific document associated with a claim.
     *
     * @param claimId The ID of the claim.
     * @param documentId The ID of the document.
     * @return byte array representing the document content.
     * @throws IOException If there is an error retrieving the document.
     */
    byte[] getClaimDocument(UUID claimId, UUID documentId) throws IOException;

}

