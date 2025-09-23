package vn.baodt2911.photobooking.photobooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageResponseDTO {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private String currency;
    private Integer durationMinutes;
    private Integer maxPeople;
    private String includes;
    private String imageUrl;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
