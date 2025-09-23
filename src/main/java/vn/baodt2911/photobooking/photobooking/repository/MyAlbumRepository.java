package vn.baodt2911.photobooking.photobooking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.baodt2911.photobooking.photobooking.entity.MyAlbum;
import java.util.UUID;

public interface MyAlbumRepository extends JpaRepository<MyAlbum, UUID> {
    
    // Search methods
    @EntityGraph(attributePaths = {"myPhotos", "user"})
    Page<MyAlbum> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    @EntityGraph(attributePaths = {"myPhotos", "user"})
    Page<MyAlbum> findByNameContainingIgnoreCaseAndIsPublic(String name, boolean isPublic, Pageable pageable);
    
    @EntityGraph(attributePaths = {"myPhotos", "user"})
    Page<MyAlbum> findByNameContainingIgnoreCaseAndUserId(String name, UUID userId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"myPhotos", "user"})
    Page<MyAlbum> findByNameContainingIgnoreCaseAndIsPublicAndUserId(String name, boolean isPublic, UUID userId, Pageable pageable);
    
    // Filter methods
    @EntityGraph(attributePaths = {"myPhotos", "user"})
    Page<MyAlbum> findByIsPublic(boolean isPublic, Pageable pageable);
    
    @EntityGraph(attributePaths = {"myPhotos", "user"})
    Page<MyAlbum> findByUserId(UUID userId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"myPhotos", "user"})
    Page<MyAlbum> findByIsPublicAndUserId(boolean isPublic, UUID userId, Pageable pageable);
    
    // Default findAll with EntityGraph
    @Override
    @EntityGraph(attributePaths = {"myPhotos", "user"})
    Page<MyAlbum> findAll(Pageable pageable);
    
    // Count methods
    long countByIsPublic(boolean isPublic);
    long countByUserId(UUID userId);
}