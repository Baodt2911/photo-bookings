package vn.baodt2911.photobooking.photobooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponseDTO {
    private long totalPackages;
    private long activePackages;
    private long totalBookings;
    private long pendingBookings;
    private long completedBookings;
    private long totalAlbums;
    private long totalUsers;
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
}
