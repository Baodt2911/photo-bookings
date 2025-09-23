package vn.baodt2911.photobooking.photobooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.baodt2911.photobooking.photobooking.entity.MyPhoto;

import java.util.List;
import java.util.UUID;

public interface MyPhotoRepository extends JpaRepository<MyPhoto, UUID> {
    List<MyPhoto> findByMyAlbumId(UUID myAlbumId);
    List<MyPhoto> findByUserId(UUID userId);
    List<MyPhoto> findByIsFavorite(boolean isFavorite);
    void deleteByMyAlbumId(UUID myAlbumId);
}