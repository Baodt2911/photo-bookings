package vn.baodt2911.photobooking.photobooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResponseDTO {
    private UUID id;
    private String name;
    private String customerName;
    private String driveFolderLink;
    private String password;
    private Boolean allowDownload;
    private Boolean allowComment;
    private Integer limitSelection;
    private UUID coverPhotoId;
    private String createdByName;
    private Instant createdAt;
    private Instant updatedAt;
}
