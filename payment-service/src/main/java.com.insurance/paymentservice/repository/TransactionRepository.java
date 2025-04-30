package paymentservice.repository;

import paymentservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByPolicyId(UUID policyId);

    List<Transaction> findByUserId(UUID userId);

    Optional<Transaction> findByGatewayTransactionId(String gatewayTransactionId);

    List<Transaction> findByPolicyIdAndTransactionStatus(UUID policyId, String status);
}

