package vn.baodt2911.photobooking.photobooking.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyAlbumSearchRequestDTO {
    private String name;
    private UUID userId;
    private Boolean isPublic;
    private String sortBy;
    private String sortDirection;
}
