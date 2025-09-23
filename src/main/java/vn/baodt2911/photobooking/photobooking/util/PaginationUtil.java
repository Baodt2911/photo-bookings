package vn.baodt2911.photobooking.photobooking.util;

import org.springframework.data.domain.Page;
import vn.baodt2911.photobooking.photobooking.dto.common.PaginationDTO;

public class PaginationUtil {
    
    public static PaginationDTO toPaginationDTO(Page<?> page) {
        PaginationDTO pagination = new PaginationDTO();
        pagination.setPage(page.getNumber());
        pagination.setSize(page.getSize());
        pagination.setTotalElements(page.getTotalElements());
        pagination.setTotalPages(page.getTotalPages());
        pagination.setHasNext(page.hasNext());
        pagination.setHasPrevious(page.hasPrevious());
        return pagination;
    }
}
