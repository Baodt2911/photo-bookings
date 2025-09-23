package vn.baodt2911.photobooking.photobooking.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import jakarta.servlet.MultipartConfigElement;

@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // Set max file size to 50MB
        factory.setMaxFileSize(DataSize.ofMegabytes(50));
        
        // Set max request size to 100MB (for multiple files)
        factory.setMaxRequestSize(DataSize.ofMegabytes(100));
        
        // Set location for temporary files
        factory.setLocation(System.getProperty("java.io.tmpdir"));
        
        return factory.createMultipartConfig();
    }
}
