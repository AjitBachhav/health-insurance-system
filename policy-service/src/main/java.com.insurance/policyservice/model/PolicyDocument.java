package com.insurance.policyservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "policy_documents")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "policy") // Avoid recursion
public class PolicyDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "document_id")
    private UUID documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @NotBlank
    @Column(name = "document_type", nullable = false)
    private String documentType; // e.g., 'POLICY_SCHEDULE', 'ID_CARD'

    @NotBlank
    @Column(name = "document_url", nullable = false)
    private String documentUrl; // e.g., S3 URL or local path

    @CreationTimestamp
    @Column(name = "generated_at", updatable = false)
    private LocalDateTime generatedAt;
}

