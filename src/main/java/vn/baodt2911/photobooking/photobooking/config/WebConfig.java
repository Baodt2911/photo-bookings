package vn.baodt2911.photobooking.photobooking.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.baodt2911.photobooking.photobooking.interceptor.AuthInterceptor;

@Configuration
public class WebConfig  implements WebMvcConfigurer{
    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Áp dụng AuthInterceptor cho admin endpoints trừ login và register
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                    "/admin/login", 
                    "/admin/login**",  // Exclude cả login với parameters
                    "/admin/register", 
                    "/admin/register**", // Exclude cả register với parameters
                    "/css/**", 
                    "/js/**", 
                    "/images/**",
                    "/favicon.ico"
                );
    }
}
