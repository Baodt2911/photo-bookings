package vn.baodt2911.photobooking.photobooking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.baodt2911.photobooking.photobooking.entity.Package;
import java.util.UUID;

public interface PackageRepository extends JpaRepository<Package, UUID> {
    
    // Search methods
    Page<Package> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Package> findByNameContainingIgnoreCaseAndActive(String name, boolean active, Pageable pageable);
    
    // Filter methods
    Page<Package> findByActive(boolean active, Pageable pageable);
    
    // Count methods
    long countByActive(boolean active);
}