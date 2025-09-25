package vn.baodt2911.photobooking.photobooking.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtil {
    
    private static PasswordEncoder passwordEncoder;
    
    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        PasswordUtil.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Hash password sử dụng BCrypt
     * @param rawPassword Password gốc
     * @return Password đã được hash
     */
    public static String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            return null;
        }
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * Kiểm tra password có khớp với hash không
     * @param rawPassword Password gốc
     * @param hashedPassword Password đã hash
     * @return true nếu khớp, false nếu không
     */
    public static boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
    
    /**
     * Kiểm tra xem password có cần hash không
     * @param password Password cần kiểm tra
     * @return true nếu cần hash, false nếu đã hash hoặc null
     */
    public static boolean needsHashing(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        // BCrypt hash luôn bắt đầu với $2a$ hoặc $2b$
        return !password.startsWith("$2a$") && !password.startsWith("$2b$");
    }
}
