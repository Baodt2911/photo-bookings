package vn.baodt2911.photobooking.photobooking.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import vn.baodt2911.photobooking.photobooking.entity.User;
import vn.baodt2911.photobooking.photobooking.service.interfaces.UserService;

import java.util.UUID;

@Component
public class AuthUtil {
    
    private static UserService userService;
    
    @Autowired
    public void setUserService(UserService userService) {
        AuthUtil.userService = userService;
    }
    
    /**
     * Get current user ID from authentication context
     * @return UUID of current user, null if not authenticated
     */
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof User) {
                return ((User) principal).getId();
            } else if (principal instanceof String) {
                // If principal is email/username, find user by email
                User user = userService.findByEmail((String) principal);
                return user != null ? user.getId() : null;
            }
        }
        return null;
    }
    
    /**
     * Get current user object from authentication context
     * @return User object of current user, null if not authenticated
     */
    public static User getCurrentUser() {
        UUID userId = getCurrentUserId();
        return userId != null ? userService.findById(userId) : null;
    }
    
    /**
     * Check if current user is authenticated
     * @return true if user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
    
    /**
     * Get current user email from authentication context
     * @return email of current user, null if not authenticated
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof User) {
                return ((User) principal).getEmail();
            } else if (principal instanceof String) {
                return (String) principal;
            }
        }
        return null;
    }
}
