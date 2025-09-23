package vn.baodt2911.photobooking.photobooking.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.baodt2911.photobooking.photobooking.service.interfaces.AlbumService;
import vn.baodt2911.photobooking.photobooking.service.interfaces.BookingService;
import vn.baodt2911.photobooking.photobooking.service.interfaces.PackageService;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private PackageService packageService;
    
    @Autowired
    private AlbumService albumService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Lấy thống kê thực tế từ database
        List<Map<String, String>> bookings = bookingService.findAll().stream()
            .map(booking -> Map.of(
                "name", booking.getName(),
                "email", booking.getEmail(),
                "packageName", booking.getPackageField() != null ? booking.getPackageField().getName() : "N/A",
                "date", booking.getBookingDate().toString(),
                "status", booking.getStatus().toString()
            ))
            .toList();

        model.addAttribute("username", "Admin");
        model.addAttribute("bookingCount", bookingService.findAll().size());
        model.addAttribute("packageCount", packageService.findAll().size());
        model.addAttribute("albumCount", albumService.findAll().size());
        model.addAttribute("bookings", bookings);

        return "admin/dashboard";
    }
}
