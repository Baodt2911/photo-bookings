package vn.baodt2911.photobooking.photobooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorDTO {
    private String field;
    private String message;
    private Object rejectedValue;
    
    public ValidationErrorDTO(String field, String message) {
        this.field = field;
        this.message = message;
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ValidationErrorsDTO {
    private List<ValidationErrorDTO> errors;
    private Map<String, List<String>> fieldErrors;
    
    public ValidationErrorsDTO(List<ValidationErrorDTO> errors) {
        this.errors = errors;
    }
}
