package vn.baodt2911.photobooking.photobooking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.baodt2911.photobooking.photobooking.entity.Album;
import java.util.UUID;

public interface AlbumRepository extends JpaRepository<Album, UUID> {
    
    // Search methods
    Page<Album> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Album> findByNameContainingIgnoreCaseAndAllowDownload(String name, boolean allowDownload, Pageable pageable);
    Page<Album> findByNameContainingIgnoreCaseAndAllowComment(String name, boolean allowComment, Pageable pageable);
    Page<Album> findByNameContainingIgnoreCaseAndCreatedById(String name, UUID createdById, Pageable pageable);
    Page<Album> findByNameContainingIgnoreCaseAndAllowDownloadAndAllowComment(String name, boolean allowDownload, boolean allowComment, Pageable pageable);
    Page<Album> findByNameContainingIgnoreCaseAndAllowDownloadAndCreatedById(String name, boolean allowDownload, UUID createdById, Pageable pageable);
    Page<Album> findByNameContainingIgnoreCaseAndAllowCommentAndCreatedById(String name, boolean allowComment, UUID createdById, Pageable pageable);
    Page<Album> findByNameContainingIgnoreCaseAndAllowDownloadAndAllowCommentAndCreatedById(String name, boolean allowDownload, boolean allowComment, UUID createdById, Pageable pageable);
    
    // Filter methods
    Page<Album> findByAllowDownload(boolean allowDownload, Pageable pageable);
    Page<Album> findByAllowComment(boolean allowComment, Pageable pageable);
    Page<Album> findByCreatedById(UUID createdById, Pageable pageable);
    Page<Album> findByAllowDownloadAndAllowComment(boolean allowDownload, boolean allowComment, Pageable pageable);
    Page<Album> findByAllowDownloadAndCreatedById(boolean allowDownload, UUID createdById, Pageable pageable);
    Page<Album> findByAllowCommentAndCreatedById(boolean allowComment, UUID createdById, Pageable pageable);
    Page<Album> findByAllowDownloadAndAllowCommentAndCreatedById(boolean allowDownload, boolean allowComment, UUID createdById, Pageable pageable);
    
    // Count methods
    long countByAllowDownload(boolean allowDownload);
    long countByAllowComment(boolean allowComment);
    long countByCreatedById(UUID createdById);
}