package vn.baodt2911.photobooking.photobooking.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageSearchRequestDTO {
    private String name;
    private String slug;
    private Boolean active;
    private Double minPrice;
    private Double maxPrice;
    private Integer minDuration;
    private Integer maxDuration;
    private Integer minMaxPeople;
    private Integer maxMaxPeople;
    private String sortBy;
    private String sortDirection;
}
