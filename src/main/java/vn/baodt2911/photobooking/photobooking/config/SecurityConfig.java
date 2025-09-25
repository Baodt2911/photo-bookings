package vn.baodt2911.photobooking.photobooking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - không cần authentication
                .requestMatchers(
                    "/v1/api/hello",           // API test endpoint
                    "/auth/login",             // Đăng nhập
                    "/auth/register",          // Đăng ký
                    "/auth/refresh",           // Refresh token
                    "/auth/logout",            // Đăng xuất
                    "/admin/login",            // Trang đăng nhập admin
                    "/admin/register",         // Trang đăng ký admin
                    "/test/**",                       // Trang chủ
                    "/album/**",               // Public album pages
                    "/show/**",                // Public show pages
                    "/api/albums/**",          // Public album APIs
                    "/api/photos/**",          // Public photo APIs
                    "/css/**",                 // Static CSS files
                    "/js/**",                  // Static JS files
                    "/images/**",              // Static images
                    "/favicon.ico",            // Favicon
                    "/error"                   // Error pages
                ).permitAll()
                
                // Admin endpoints - cho phép AuthInterceptor xử lý authentication
                .requestMatchers("/admin/**").permitAll()
                
                // User endpoints - cần authentication với role USER
                .requestMatchers("/user/**").hasRole("USER")
                
                // Tất cả các request khác cần authentication
                .anyRequest().authenticated()
            )
            .csrf(AbstractHttpConfigurer::disable)
            // Disable form login vì dự án sử dụng JWT authentication
            .formLogin(AbstractHttpConfigurer::disable)
            // Disable HTTP Basic authentication
            .httpBasic(AbstractHttpConfigurer::disable);
            
        return http.build();
    }
}
