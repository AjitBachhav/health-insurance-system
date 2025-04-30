package policyservice.repository;

import policyservice.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, UUID> {

    Optional<Policy> findByPolicyNumber(String policyNumber);

    List<Policy> findByUserId(UUID userId);

    List<Policy> findByUserIdAndPolicyStatus(UUID userId, String status);

    List<Policy> findByPlanId(UUID planId);

    // Add more specific queries as needed, e.g., find active policies by user
    // List<Policy> findByUserIdAndPolicyStatusIn(UUID userId, List<String> statuses);
}

