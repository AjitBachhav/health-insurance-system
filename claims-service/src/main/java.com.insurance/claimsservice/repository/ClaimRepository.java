package claimsservice.repository;

import claimsservice.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    Optional<Claim> findByClaimNumber(String claimNumber);

    List<Claim> findByPolicyId(UUID policyId);

    List<Claim> findByUserId(UUID userId);

    List<Claim> findByUserIdAndClaimStatus(UUID userId, String status);

    // Add more specific queries as needed
}

