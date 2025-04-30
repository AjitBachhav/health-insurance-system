package planservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class ProviderNetworkDto {
    private UUID networkId;
    private String networkName;
    private String description;
}

