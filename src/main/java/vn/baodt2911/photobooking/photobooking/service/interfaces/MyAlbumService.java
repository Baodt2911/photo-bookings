package vn.baodt2911.photobooking.photobooking.service.interfaces;

import vn.baodt2911.photobooking.photobooking.entity.MyAlbum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MyAlbumService {
    List<MyAlbum> findAll();
    MyAlbum findById(UUID id);
    MyAlbum save(MyAlbum myAlbum);
    void deleteById(UUID id);
    
    // New methods for search, filter and pagination
    Page<MyAlbum> findMyAlbumsWithFilters(String search, Boolean isPublic, UUID userId, Pageable pageable);
    long count();
    long countByIsPublic(boolean isPublic);
    long countByUserId(UUID userId);
    
    // Method to get cover photo URL
    String getCoverPhotoUrl(MyAlbum myAlbum);
}
