package vn.baodt2911.photobooking.photobooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.baodt2911.photobooking.photobooking.entity.Booking;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
}