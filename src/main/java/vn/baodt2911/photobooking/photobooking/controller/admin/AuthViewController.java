package vn.baodt2911.photobooking.photobooking.controller.admin;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.WebUtils;
import vn.baodt2911.photobooking.photobooking.util.JwtUtil;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
class AuthViewController {
    private final JwtUtil jwtUtil;
    @GetMapping("/login")
    public String showLoginPage(HttpServletRequest request, Model model, @RequestParam(required = false) String error) {
        // Xử lý thông báo lỗi trước
        if ("access_denied".equals(error)) {
            model.addAttribute("error", "Bạn không có quyền truy cập vào trang admin. Chỉ admin mới được phép truy cập.");
            return "admin/login"; // Hiển thị trang login với thông báo lỗi, không redirect
        }

        Cookie accessTokenCookie = WebUtils.getCookie(request, "access_token");

        // Kiểm tra nếu người dùng đã có access token hợp lệ
        if (accessTokenCookie != null && jwtUtil.validate(accessTokenCookie.getValue(), false)) {
            // Đã đăng nhập, chuyển hướng về trang dashboard
            return "redirect:/admin/dashboard";
        }

        // Chưa đăng nhập, hiển thị trang login
        return "admin/login";
    }

    @GetMapping("/register")
    public String showRegisterPage(HttpServletRequest request) {
        Cookie accessTokenCookie = WebUtils.getCookie(request, "access_token");

        // Kiểm tra nếu người dùng đã có access token hợp lệ
        if (accessTokenCookie != null && jwtUtil.validate(accessTokenCookie.getValue(), false)) {
            // Đã đăng nhập, chuyển hướng về trang dashboard
            return "redirect:/admin/dashboard";
        }

        // Chưa đăng nhập, hiển thị trang đăng ký
        return "admin/register";
    }
}
