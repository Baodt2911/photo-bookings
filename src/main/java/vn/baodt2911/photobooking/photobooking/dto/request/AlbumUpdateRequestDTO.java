package vn.baodt2911.photobooking.photobooking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumUpdateRequestDTO {
    private UUID id;
    private String name;
    private String customerName;
    private String driveFolderLink;
    private String password;
    private Boolean allowDownload;
    private Boolean allowComment;
    private Integer limitSelection;
}
