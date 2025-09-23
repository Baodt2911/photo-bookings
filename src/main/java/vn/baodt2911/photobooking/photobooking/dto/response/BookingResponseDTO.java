package vn.baodt2911.photobooking.photobooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.baodt2911.photobooking.photobooking.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {
    private UUID id;
    private String userName;
    private String userEmail;
    private String packageName;
    private String name;
    private String email;
    private String phone;
    private BigDecimal bookingPrice;
    private Instant bookingDate;
    private BookingStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
