package planservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "provider_networks")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "plans") // Avoid recursion in equals/hashCode
public class ProviderNetwork {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "network_id")
    private UUID networkId;

    @NotBlank
    @Column(name = "network_name", nullable = false, unique = true) // Assuming network names are unique
    private String networkName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "providerNetworks", fetch = FetchType.LAZY)
    private Set<Plan> plans = new HashSet<>();

    public ProviderNetwork(String networkName, String description) {
        this.networkName = networkName;
        this.description = description;
    }
}

