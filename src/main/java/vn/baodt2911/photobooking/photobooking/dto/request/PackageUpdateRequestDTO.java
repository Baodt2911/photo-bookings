package vn.baodt2911.photobooking.photobooking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageUpdateRequestDTO {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String includes;
    private BigDecimal price;
    private Integer durationMinutes;
    private Integer maxPeople;
}
