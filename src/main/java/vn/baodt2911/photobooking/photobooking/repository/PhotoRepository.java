package vn.baodt2911.photobooking.photobooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.baodt2911.photobooking.photobooking.entity.Photo;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
}