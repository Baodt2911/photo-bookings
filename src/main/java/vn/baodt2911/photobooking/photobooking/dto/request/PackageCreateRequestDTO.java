package vn.baodt2911.photobooking.photobooking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageCreateRequestDTO {
    private String name;
    private String slug;
    private String description;
    private String includes;
    private BigDecimal price;
    private Integer durationMinutes;
    private Integer maxPeople;
}
