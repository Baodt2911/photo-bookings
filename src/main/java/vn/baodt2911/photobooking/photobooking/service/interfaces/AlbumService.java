package vn.baodt2911.photobooking.photobooking.service.interfaces;

import vn.baodt2911.photobooking.photobooking.entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AlbumService {
    List<Album> findAll();
    Album findById(UUID id);
    Album save(Album album);
    void deleteById(UUID id);
    
    // New methods for search, filter and pagination
    Page<Album> findAlbumsWithFilters(String search, Boolean allowDownload, Boolean allowComment, UUID createdById, Pageable pageable);
    long count();
    long countByAllowDownload(boolean allowDownload);
    long countByAllowComment(boolean allowComment);
    long countByCreatedById(UUID createdById);
}
