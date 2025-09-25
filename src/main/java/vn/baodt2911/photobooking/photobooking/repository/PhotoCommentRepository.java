package vn.baodt2911.photobooking.photobooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.baodt2911.photobooking.photobooking.entity.PhotoComment;
import vn.baodt2911.photobooking.photobooking.entity.Photo;

import java.util.List;
import java.util.UUID;

public interface PhotoCommentRepository extends JpaRepository<PhotoComment, UUID> {
    List<PhotoComment> findByPhoto(Photo photo);
}