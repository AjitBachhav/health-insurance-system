package claimsservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "claim_documents")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "claim") // Avoid recursion
public class ClaimDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "document_id")
    private UUID documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Column(name = "document_name")
    private String documentName; // Original filename or description

    @NotBlank
    @Column(name = "document_type", nullable = false)
    private String documentType; // e.g., "RECEIPT", "MEDICAL_REPORT", "EXPLANATION_OF_BENEFITS"

    @NotBlank
    @Column(name = "document_url", nullable = false)
    private String documentUrl; // e.g., S3 URL or local path

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;
}

