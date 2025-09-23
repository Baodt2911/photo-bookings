package vn.baodt2911.photobooking.photobooking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyAlbumUpdateRequestDTO {
    private UUID id;
    private String name;
    private String description;
    private Boolean isPublic;
    private UUID coverId; // For existing cover photo from DB
    private Integer coverIndex; // For new cover photo from uploaded files
    private List<MultipartFile> images; // New images to upload
}
