package vn.baodt2911.photobooking.photobooking.service.interfaces;

import vn.baodt2911.photobooking.photobooking.entity.Package;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PackageService {
    List<Package> findAll();
    Package findById(UUID id);
    Package save(Package packageEntity);
    void deleteById(UUID id);
    
    // New methods for search, filter and pagination
    Page<Package> findPackagesWithFilters(String search, Boolean status, Pageable pageable);
    long count();
    long countByActive(boolean active);
}
