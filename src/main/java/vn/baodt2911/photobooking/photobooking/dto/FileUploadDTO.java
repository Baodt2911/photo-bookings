package vn.baodt2911.photobooking.photobooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadDTO {
    private MultipartFile file;
    private String description;
    private String category;
    
    public FileUploadDTO(MultipartFile file) {
        this.file = file;
    }
}
