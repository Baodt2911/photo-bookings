package vn.baodt2911.photobooking.photobooking.service.interfaces;

import vn.baodt2911.photobooking.photobooking.entity.MyPhoto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MyPhotoService {
    List<MyPhoto> findAll();
    MyPhoto findById(UUID id);
    MyPhoto save(MyPhoto myPhoto);
    void deleteById(UUID id);
    
    // Album-related methods
    List<MyPhoto> findByMyAlbumId(UUID myAlbumId);
    void deleteByMyAlbumId(UUID myAlbumId);
    
    // User-related methods
    List<MyPhoto> findByUserId(UUID userId);
    
    // Upload methods
    MyPhoto uploadPhoto(UUID myAlbumId, UUID userId, MultipartFile file, String title, String description);
    List<MyPhoto> uploadMultiplePhotos(UUID myAlbumId, UUID userId, MultipartFile[] files);
    
    // Utility methods
    long count();
    long countByMyAlbumId(UUID myAlbumId);
    long countByUserId(UUID userId);
    long countByIsFavorite(boolean isFavorite);
}
