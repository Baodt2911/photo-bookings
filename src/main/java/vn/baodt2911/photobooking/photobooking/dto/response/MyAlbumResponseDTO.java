package vn.baodt2911.photobooking.photobooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyAlbumResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private Boolean isPublic;
    private String userName;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer photoCount;
}
