package claimsservice.service;

import claimsservice.dto.ClaimDocumentDto;
import claimsservice.dto.ClaimDto;
import claimsservice.dto.ClaimStatusHistoryDto;
import claimsservice.model.Claim;
import claimsservice.model.ClaimDocument;
import claimsservice.model.ClaimStatusHistory;
import claimsservice.repository.ClaimRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClaimServiceImpl implements ClaimService {

    private static final Logger log = LoggerFactory.getLogger(ClaimServiceImpl.class);

    private final ClaimRepository claimRepository;
    // TODO: Inject S3 client or other document storage service
    // TODO: Inject Policy/User service client for validation
    // TODO: Inject Kafka/RabbitMQ template for event publishing

    // Simple local storage path (replace with proper storage solution)
    private final Path documentStorageLocation = Paths.get("/home/ubuntu/claim_documents").toAbsolutePath().normalize();

    @Autowired
    public ClaimServiceImpl(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
        try {
            Files.createDirectories(this.documentStorageLocation);
        } catch (Exception ex) {
            log.error("Could not create the directory where the uploaded files will be stored.", ex);
            // Handle error appropriately
        }
    }

    @Override
    @Transactional
    public ClaimDto submitClaim(ClaimDto claimDto, List<MultipartFile> files) throws IOException {
        log.info("Submitting new claim for policyId: {}", claimDto.getPolicyId());
        // TODO: Validate policyId and userId exist and policy is active (call Policy/User service)

        Claim claim = mapToEntity(claimDto);
        claim.setClaimNumber(generateClaimNumber());
        claim.setClaimStatus("SUBMITTED");
        claim.setAmountPaid(null); // Ensure amountPaid is null or zero initially

        // Add initial status history
        claim.addStatusHistory(new ClaimStatusHistory(claim, "SUBMITTED", "Claim submitted by user."));

        // Handle file uploads
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    ClaimDocument doc = storeDocument(claim, file, "SUBMITTED_DOCUMENT"); // Default type
                    claim.addDocument(doc);
                }
            }
        }

        Claim savedClaim = claimRepository.save(claim);
        log.info("Submitted claim with ID: {} and Number: {}", savedClaim.getClaimId(), savedClaim.getClaimNumber());
        // TODO: Publish ClaimSubmittedEvent

        return mapToDtoWithDetails(savedClaim);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClaimDto> getClaimById(UUID claimId) {
        return claimRepository.findById(claimId).map(this::mapToDtoWithDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClaimDto> getClaimByNumber(String claimNumber) {
        return claimRepository.findByClaimNumber(claimNumber).map(this::mapToDtoWithDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimDto> getClaimsByPolicyId(UUID policyId) {
        return claimRepository.findByPolicyId(policyId).stream()
                .map(this::mapToDto) // Use basic mapping for lists
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimDto> getClaimsByUserId(UUID userId) {
        return claimRepository.findByUserId(userId).stream()
                .map(this::mapToDto) // Use basic mapping for lists
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClaimDto updateClaimStatus(UUID claimId, String newStatus, String notes) {
        log.info("Updating status for claimId: {} to {}", claimId, newStatus);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found with id: " + claimId));

        // TODO: Add validation for status transitions
        claim.setClaimStatus(newStatus);
        claim.addStatusHistory(new ClaimStatusHistory(claim, newStatus, notes));

        // Update amountPaid if status is PAID (example logic)
        if ("PAID".equals(newStatus)) {
            // This logic might be more complex, potentially requiring payment details
            // For now, assume amountPaid is set based on approval
            if (claim.getAmountPaid() == null || claim.getAmountPaid().compareTo(claim.getAmountClaimed()) != 0) {
                log.warn("Claim {} marked as PAID, but amountPaid might need adjustment.", claimId);
                // Potentially set amountPaid = amountClaimed or based on adjudication result
                // claim.setAmountPaid(claim.getAmountClaimed()); 
            }
        }

        Claim updatedClaim = claimRepository.save(claim);
        log.info("Updated status for claimId: {} to {}", updatedClaim.getClaimId(), updatedClaim.getClaimStatus());
        // TODO: Publish ClaimStatusUpdatedEvent

        return mapToDtoWithDetails(updatedClaim);
    }

    @Override
    @Transactional
    public ClaimDto addDocumentToClaim(UUID claimId, MultipartFile file, String documentType) throws IOException {
        log.info("Adding document to claimId: {}", claimId);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found with id: " + claimId));

        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file.");
        }

        ClaimDocument doc = storeDocument(claim, file, documentType);
        claim.addDocument(doc);

        Claim updatedClaim = claimRepository.save(claim); // Cascade should save the document
        log.info("Added document {} to claimId: {}", doc.getDocumentId(), updatedClaim.getClaimId());

        return mapToDtoWithDetails(updatedClaim);
    }

    @Override
    public byte[] getClaimDocument(UUID claimId, UUID documentId) throws IOException {
        log.info("Retrieving document {} for claimId: {}", documentId, claimId);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found with id: " + claimId));

        ClaimDocument document = claim.getDocuments().stream()
                .filter(doc -> doc.getDocumentId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId + " for claim " + claimId));

        // Assuming documentUrl stores the local file path for now
        try {
            Path filePath = Paths.get(document.getDocumentUrl());
            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                throw new IOException("Document file not found or not readable: " + document.getDocumentUrl());
            }
            return Files.readAllBytes(filePath);
        } catch (Exception ex) {
            log.error("Error reading document file: {}", document.getDocumentUrl(), ex);
            throw new IOException("Error retrieving document file", ex);
        }
        // TODO: Adapt for S3 or other storage by fetching from the respective service
    }

    // --- Helper Methods --- 

    private ClaimDocument storeDocument(Claim claim, MultipartFile file, String documentType) throws IOException {
        // Normalize file name
        String originalFileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9.\\-]", "_");

        try {
            // Check if the filename contains invalid characters
            if (uniqueFileName.contains("..")) {
                throw new IOException("Filename contains invalid path sequence " + uniqueFileName);
            }

            Path targetLocation = this.documentStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            ClaimDocument claimDocument = new ClaimDocument();
            claimDocument.setClaim(claim);
            claimDocument.setDocumentName(originalFileName);
            claimDocument.setDocumentType(documentType != null ? documentType : "GENERAL");
            claimDocument.setDocumentUrl(targetLocation.toString()); // Store local path
            // TODO: Replace with S3 URL after uploading to S3

            return claimDocument;
        } catch (IOException ex) {
            log.error("Could not store file {}. Please try again!", uniqueFileName, ex);
            throw new IOException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    private String generateClaimNumber() {
        // Simple example: Prefix + timestamp + random digits
        return "CLM-" + System.currentTimeMillis() + "-" + String.format("%05d", (int) (Math.random() * 100000));
    }

    // Basic mapping (for lists)
    private ClaimDto mapToDto(Claim claim) {
        ClaimDto dto = new ClaimDto();
        dto.setClaimId(claim.getClaimId());
        dto.setClaimNumber(claim.getClaimNumber());
        dto.setPolicyId(claim.getPolicyId());
        dto.setUserId(claim.getUserId());
        dto.setDependentId(claim.getDependentId());
        dto.setDateOfService(claim.getDateOfService());
        dto.setAmountClaimed(claim.getAmountClaimed());
        dto.setAmountPaid(claim.getAmountPaid());
        dto.setClaimStatus(claim.getClaimStatus());
        dto.setSubmissionDate(claim.getSubmissionDate());
        dto.setLastUpdatedDate(claim.getLastUpdatedDate());
        // Omit collections for list view
        return dto;
    }

    // Detailed mapping (for single view)
    private ClaimDto mapToDtoWithDetails(Claim claim) {
        ClaimDto dto = mapToDto(claim);
        dto.setProviderName(claim.getProviderName());
        dto.setDiagnosisCode(claim.getDiagnosisCode());
        dto.setProcedureCode(claim.getProcedureCode());
        if (claim.getDocuments() != null) {
            dto.setDocuments(claim.getDocuments().stream().map(this::mapDocumentToDto).collect(Collectors.toList()));
        }
        if (claim.getStatusHistory() != null) {
            dto.setStatusHistory(claim.getStatusHistory().stream().map(this::mapStatusHistoryToDto).collect(Collectors.toList()));
        }
        return dto;
    }

    private Claim mapToEntity(ClaimDto dto) {
        Claim claim = new Claim();
        // claimId, claimNumber, status, submissionDate, lastUpdatedDate are set internally
        claim.setPolicyId(dto.getPolicyId());
        claim.setUserId(dto.getUserId());
        claim.setDependentId(dto.getDependentId());
        claim.setDateOfService(dto.getDateOfService());
        claim.setProviderName(dto.getProviderName());
        claim.setDiagnosisCode(dto.getDiagnosisCode());
        claim.setProcedureCode(dto.getProcedureCode());
        claim.setAmountClaimed(dto.getAmountClaimed());
        // amountPaid is usually set during processing/payment
        // Documents and StatusHistory are added via convenience methods
        return claim;
    }

    private ClaimDocumentDto mapDocumentToDto(ClaimDocument entity) {
        ClaimDocumentDto dto = new ClaimDocumentDto();
        dto.setDocumentId(entity.getDocumentId());
        dto.setDocumentName(entity.getDocumentName());
        dto.setDocumentType(entity.getDocumentType());
        dto.setDocumentUrl(entity.getDocumentUrl()); // Consider if URL should be exposed
        dto.setUploadedAt(entity.getUploadedAt());
        return dto;
    }

    private ClaimStatusHistoryDto mapStatusHistoryToDto(ClaimStatusHistory entity) {
        ClaimStatusHistoryDto dto = new ClaimStatusHistoryDto();
        dto.setHistoryId(entity.getHistoryId());
        dto.setStatus(entity.getStatus());
        dto.setNotes(entity.getNotes());
        dto.setChangedAt(entity.getChangedAt());
        return dto;
    }
}

