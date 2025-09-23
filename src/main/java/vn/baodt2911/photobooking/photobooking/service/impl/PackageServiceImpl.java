package vn.baodt2911.photobooking.photobooking.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.baodt2911.photobooking.photobooking.entity.Package;
import vn.baodt2911.photobooking.photobooking.repository.PackageRepository;
import vn.baodt2911.photobooking.photobooking.service.interfaces.PackageService;

import java.util.List;
import java.util.UUID;

@Service
public class PackageServiceImpl implements PackageService {

    @Autowired
    private PackageRepository packageRepository;

    @Override
    public List<Package> findAll() {
        return packageRepository.findAll();
    }

    @Override
    public Package findById(UUID id) {
        return packageRepository.findById(id).orElse(null);
    }

    @Override
    public Package save(Package packageEntity) {
        return packageRepository.save(packageEntity);
    }

    @Override
    public void deleteById(UUID id) {
        packageRepository.deleteById(id);
    }
    
    @Override
    public Page<Package> findPackagesWithFilters(String search, Boolean status, Pageable pageable) {
        if (search != null && !search.trim().isEmpty() && status != null) {
            // Search with status filter
            return packageRepository.findByNameContainingIgnoreCaseAndActive(search.trim(), status, pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            // Search only
            return packageRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else if (status != null) {
            // Status filter only
            return packageRepository.findByActive(status, pageable);
        } else {
            // No filters, return all
            return packageRepository.findAll(pageable);
        }
    }
    
    @Override
    public long count() {
        return packageRepository.count();
    }
    
    @Override
    public long countByActive(boolean active) {
        return packageRepository.countByActive(active);
    }
}
