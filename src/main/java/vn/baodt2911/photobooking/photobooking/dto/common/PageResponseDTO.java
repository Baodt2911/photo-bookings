package vn.baodt2911.photobooking.photobooking.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDTO<T> {
    private List<T> content;
    private PaginationDTO pagination;
    
    public static <T> PageResponseDTO<T> of(List<T> content, PaginationDTO pagination) {
        return new PageResponseDTO<>(content, pagination);
    }
}
