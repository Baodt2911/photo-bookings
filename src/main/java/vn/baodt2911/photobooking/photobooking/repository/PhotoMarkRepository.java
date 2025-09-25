package vn.baodt2911.photobooking.photobooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.baodt2911.photobooking.photobooking.entity.PhotoMark;
import vn.baodt2911.photobooking.photobooking.entity.Photo;

import java.util.Optional;
import java.util.UUID;

public interface PhotoMarkRepository extends JpaRepository<PhotoMark, UUID> {
    long countByPhotoAndIsFavoriteTrue(Photo photo);
    long countByPhotoAndIsSelectedTrue(Photo photo);
    Optional<PhotoMark> findByPhoto(Photo photo);
}