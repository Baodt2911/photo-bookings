package vn.baodt2911.photobooking.photobooking.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.baodt2911.photobooking.photobooking.entity.Booking;
import vn.baodt2911.photobooking.photobooking.entity.Package;
import vn.baodt2911.photobooking.photobooking.service.interfaces.BookingService;
import vn.baodt2911.photobooking.photobooking.service.interfaces.PackageService;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PackageService packageService;

    @GetMapping("/bookings")
    public String bookings(Model model) {
        List<Booking> bookings = bookingService.findAll();
        List<Package> packages = packageService.findAll();
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("packages", packages);
        model.addAttribute("totalBookings", bookings.size());
        model.addAttribute("pendingBookings", bookings.stream().filter(b -> "pending".equals(b.getStatus())).count());
        model.addAttribute("confirmedBookings", bookings.stream().filter(b -> "confirmed".equals(b.getStatus())).count());
        
        return "admin/booking";
    }

    @PostMapping("/bookings/{id}/confirm")
    public String confirmBooking(@PathVariable UUID id) {
        bookingService.updateStatus(id, "confirmed");
        return "redirect:/admin/bookings";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable UUID id) {
        bookingService.updateStatus(id, "cancelled");
        return "redirect:/admin/bookings";
    }

    @GetMapping("/bookings/{id}")
    public String viewBooking(@PathVariable UUID id, Model model) {
        Booking booking = bookingService.findById(id);
        model.addAttribute("booking", booking);
        return "admin/booking-detail";
    }
}
