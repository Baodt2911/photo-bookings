package vn.baodt2911.photobooking.photobooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyPhotoResponseDTO {
    private UUID id;
    private String url;
    private String title;
    private String description;
    private Integer orderIndex;
    private Boolean isFavorite;
    private Instant createdAt;
    private Instant updatedAt;
    private Long size; // File size in bytes
}
