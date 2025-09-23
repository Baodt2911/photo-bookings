package vn.baodt2911.photobooking.photobooking.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumSearchRequestDTO {
    private String name;
    private UUID createdById;
    private Boolean allowDownload;
    private Boolean allowComment;
    private String sortBy;
    private String sortDirection;
}
