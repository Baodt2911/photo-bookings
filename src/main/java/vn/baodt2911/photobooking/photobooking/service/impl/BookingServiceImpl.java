package vn.baodt2911.photobooking.photobooking.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.baodt2911.photobooking.photobooking.entity.Booking;
import vn.baodt2911.photobooking.photobooking.enums.BookingStatus;
import vn.baodt2911.photobooking.photobooking.repository.BookingRepository;
import vn.baodt2911.photobooking.photobooking.service.interfaces.BookingService;

import java.util.List;
import java.util.UUID;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    @Override
    public Booking findById(UUID id) {
        return bookingRepository.findById(id).orElse(null);
    }

    @Override
    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }

    @Override
    public void deleteById(UUID id) {
        bookingRepository.deleteById(id);
    }

    @Override
    public void updateStatus(UUID id, String status) {
        Booking booking = findById(id);
        if (booking != null) {
            booking.setStatus(BookingStatus.valueOf(status.toUpperCase()));
            save(booking);
        }
    }
}
