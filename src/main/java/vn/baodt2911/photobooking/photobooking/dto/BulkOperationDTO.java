package vn.baodt2911.photobooking.photobooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkOperationDTO {
    private List<UUID> ids;
    private String operation;
    private Object data;
    
    public BulkOperationDTO(List<UUID> ids, String operation) {
        this.ids = ids;
        this.operation = operation;
    }
}
