package vn.baodt2911.photobooking.photobooking.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;
import jakarta.servlet.http.Cookie;
import vn.baodt2911.photobooking.photobooking.util.JwtUtil;
import vn.baodt2911.photobooking.photobooking.entity.User;
import vn.baodt2911.photobooking.photobooking.service.interfaces.UserService;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    @Value("${jwt.access.expiration}")
    private long accessExpiration;
    private final JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;
    
    // Helper method để xóa tất cả cookie
    private void clearAllCookies(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("access_token", "");
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refresh_token", "");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie accessTokenCookie = WebUtils.getCookie(request, "access_token");
        String accessToken = accessTokenCookie != null ? accessTokenCookie.getValue() : null;

        // Nếu không có access token, redirect về login bình thường
        if (accessToken == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return false;
        }

        // Kiểm tra access token hợp lệ
        if (jwtUtil.validate(accessToken, false)) {
            String email = jwtUtil.extractUsername(accessToken, false);
            User user = userService.findByEmail(email);
            if (user != null) {
                // Kiểm tra role admin
                if (!"admin".equals(user.getRole())) {
                    clearAllCookies(response);
                    response.sendRedirect(request.getContextPath() + "/admin/login?error=access_denied");
                    return false;
                }
                
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(user, null, null);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
            return true;
        }

        // Access token không hợp lệ, thử refresh token
        Cookie refreshCookie = WebUtils.getCookie(request, "refresh_token");
        String refreshToken = refreshCookie != null ? refreshCookie.getValue() : null;

        if (refreshToken != null && jwtUtil.validate(refreshToken, true)) {
            String email = jwtUtil.extractUsername(refreshToken, true);
            String newAccessToken = jwtUtil.generateAccessToken(email);
            Date accessExpirationDate = new Date(System.currentTimeMillis() + accessExpiration);
            long accessExpirationDateTime = accessExpirationDate.getTime();
            
            // Set lại access token vào cookie
            Cookie newAccessTokenCookie = new Cookie("access_token", newAccessToken);
            newAccessTokenCookie.setHttpOnly(true);
            newAccessTokenCookie.setSecure(false);
            newAccessTokenCookie.setPath("/");
            newAccessTokenCookie.setMaxAge((int) accessExpirationDateTime);
            response.addCookie(newAccessTokenCookie);

            // Set authentication context
            User user = userService.findByEmail(email);
            if (user != null) {
                // Kiểm tra role admin
                if (!"admin".equals(user.getRole())) {
                    clearAllCookies(response);
                    response.sendRedirect(request.getContextPath() + "/admin/login?error=access_denied");
                    return false;
                }
                
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(user, null, null);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
            return true;
        }

        // Không có token hợp lệ, redirect về login bình thường
        response.sendRedirect(request.getContextPath() + "/admin/login");
        return false;
    }


    // Các phương thức khác của HandlerInterceptor không bắt buộc phải override
//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        // Logic sau khi controller đã xử lý
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        // Logic sau khi view đã được render xong
//    }
}