package vn.baodt2911.photobooking.photobooking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyAlbumCreateRequestDTO {
    private String name;
    private String description;
    private Boolean isPublic;
}
