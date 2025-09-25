package vn.baodt2911.photobooking.photobooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.baodt2911.photobooking.photobooking.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

    Optional<User> findById(UUID id);
}
