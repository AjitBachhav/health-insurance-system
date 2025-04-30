package userservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class AddressDto {
    private UUID addressId;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String addressType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

