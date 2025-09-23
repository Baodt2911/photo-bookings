package vn.baodt2911.photobooking.photobooking.controller.admin;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.baodt2911.photobooking.photobooking.entity.User;
import vn.baodt2911.photobooking.photobooking.service.interfaces.UserService;
import vn.baodt2911.photobooking.photobooking.util.JwtUtil;

import java.util.Date;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;
    @Value("${jwt.access.expiration}")
    private long accessExpiration;
    private final JwtUtil jwtUtil;
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, RedirectAttributes redirectAttributes, HttpServletResponse response) {
        try {
            User user =  userService.login(email, password);

            String accessToken = jwtUtil.generateAccessToken(email);
            String refreshToken = jwtUtil.generateRefreshToken(email);

            Date refreshExpirationDate = new Date(System.currentTimeMillis() + refreshExpiration);
            long refreshExpirationDateTime = refreshExpirationDate.getTime();

            Date accessExpirationDate = new Date(System.currentTimeMillis() + accessExpiration);
            long accessExpirationDateTime = accessExpirationDate.getTime();

            Cookie accessTokenCookie = new Cookie("access_token", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge((int) accessExpirationDateTime);
            response.addCookie(accessTokenCookie);

            Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge((int) refreshExpirationDateTime);
            response.addCookie(refreshTokenCookie);

            if ("admin".equals(user.getRole())) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/admin/login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Email hoặc mật khẩu không đúng.");
            return "redirect:/admin/login";
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String name, @RequestParam String email, @RequestParam String password) {
        try {
            userService.register(name, email, password);
            return ResponseEntity.ok("Đăng ký thành công. Vui lòng đăng nhập.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "refresh_token", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Refresh token không tồn tại.");
        }

        if (!jwtUtil.validate(refreshToken, true)) {
            return ResponseEntity.badRequest().body("Refresh token không hợp lệ hoặc đã hết hạn.");
        }

        String email = jwtUtil.extractUsername(refreshToken, true);
        String newAccessToken = jwtUtil.generateAccessToken(email);
        Date accessExpirationDate = new Date(System.currentTimeMillis() + accessExpiration);
        long accessExpirationDateTime = accessExpirationDate.getTime();
        // Set lại access token vào cookie
        Cookie accessTokenCookie = new Cookie("access_token", newAccessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) accessExpirationDateTime); // 1 giờ
        response.addCookie(accessTokenCookie);

        return ResponseEntity.ok().build();
    }


    @PostMapping("/logout")
    public String logout(HttpServletResponse response, RedirectAttributes redirectAttributes) {
        try {
            // Xóa cookie access_token
            Cookie accessTokenCookie = new Cookie("access_token", "");
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(0); // expire ngay lập tức
            response.addCookie(accessTokenCookie);

            // Xóa cookie refresh_token
            Cookie refreshTokenCookie = new Cookie("refresh_token", "");
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0); // expire ngay lập tức
            response.addCookie(refreshTokenCookie);

            // Thêm thông báo đăng xuất thành công
            redirectAttributes.addFlashAttribute("success", "Đăng xuất thành công!");
            
            return "redirect:/admin/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi đăng xuất.");
            return "redirect:/admin/login";
        }
    }
}
