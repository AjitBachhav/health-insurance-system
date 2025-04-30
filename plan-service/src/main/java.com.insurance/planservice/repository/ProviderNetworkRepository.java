package planservice.repository;

import planservice.model.ProviderNetwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProviderNetworkRepository extends JpaRepository<ProviderNetwork, UUID> {
    Optional<ProviderNetwork> findByNetworkName(String networkName);
}

