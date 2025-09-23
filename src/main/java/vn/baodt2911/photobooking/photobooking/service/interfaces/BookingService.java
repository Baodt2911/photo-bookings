package vn.baodt2911.photobooking.photobooking.service.interfaces;

import vn.baodt2911.photobooking.photobooking.entity.Booking;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    List<Booking> findAll();
    Booking findById(UUID id);
    Booking save(Booking booking);
    void deleteById(UUID id);
    void updateStatus(UUID id, String status);
}
