package vn.baodt2911.photobooking.photobooking.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.baodt2911.photobooking.photobooking.enums.BookingStatus;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingSearchRequestDTO {
    private String name;
    private String email;
    private String phone;
    private UUID userId;
    private UUID packageId;
    private BookingStatus status;
    private Instant bookingDateFrom;
    private Instant bookingDateTo;
    private Instant createdAtFrom;
    private Instant createdAtTo;
    private String sortBy;
    private String sortDirection;
}
