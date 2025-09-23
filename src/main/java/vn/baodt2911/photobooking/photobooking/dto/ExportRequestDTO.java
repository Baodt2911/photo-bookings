package vn.baodt2911.photobooking.photobooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequestDTO {
    private String entityType;
    private String format;
    private Instant dateFrom;
    private Instant dateTo;
    private String[] fields;
    private String filename;
}
