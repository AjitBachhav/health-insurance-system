package planservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "benefits")
@Data
@NoArgsConstructor
public class Benefit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "benefit_id")
    private UUID benefitId;

    @NotBlank
    @Column(name = "benefit_name", nullable = false, unique = true) // Assuming benefit names are unique
    private String benefitName;

    @Column(columnDefinition = "TEXT")
    private String description;

    public Benefit(String benefitName, String description) {
        this.benefitName = benefitName;
        this.description = description;
    }
}

