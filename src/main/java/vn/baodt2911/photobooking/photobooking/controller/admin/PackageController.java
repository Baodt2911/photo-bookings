package vn.baodt2911.photobooking.photobooking.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.baodt2911.photobooking.photobooking.entity.Package;
import vn.baodt2911.photobooking.photobooking.entity.User;
import vn.baodt2911.photobooking.photobooking.service.interfaces.PackageService;
import vn.baodt2911.photobooking.photobooking.dto.request.PackageCreateRequestDTO;
import vn.baodt2911.photobooking.photobooking.dto.request.PackageUpdateRequestDTO;
import vn.baodt2911.photobooking.photobooking.dto.response.PackageResponseDTO;
import vn.baodt2911.photobooking.photobooking.mapper.EntityMapper;
import vn.baodt2911.photobooking.photobooking.service.CloudinaryService;
import vn.baodt2911.photobooking.photobooking.util.AuthUtil;

import java.util.UUID;


@Controller
@RequestMapping("/admin")
public class PackageController {

    @Autowired
    private PackageService packageService;
    
    @Autowired
    private EntityMapper entityMapper;

    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping("/packages")
    public String packages(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        // Create Pageable object with sorting
        Pageable pageable = createPageable(page, size, sort);
        
        // Get packages with search, filter and pagination
        Page<Package> packages = packageService.findPackagesWithFilters(search, status, pageable);
        
        // Get statistics
        long totalPackages = packageService.count();
        long activePackages = packageService.countByActive(true);

        model.addAttribute("packages", packages);
        model.addAttribute("totalPackages", totalPackages);
        model.addAttribute("activePackages", activePackages);
        
        // Add search parameters for pagination links
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("sort", sort);

        return "admin/package";
    }
    
    private Pageable createPageable(int page, int size, String sort) {
        Sort.Direction direction = Sort.Direction.ASC;
        String sortField = "name";
        
        // Parse sort parameter
        if (sort != null && !sort.isEmpty()) {
            if (sort.endsWith("_desc")) {
                direction = Sort.Direction.DESC;
                sortField = sort.substring(0, sort.length() - 5);
            } else {
                sortField = sort;
            }
        }
        
        // Map sort field names
        switch (sortField) {
            case "price":
                sortField = "price";
                break;
            case "created_at":
                sortField = "createdAt";
                break;
            case "name":
            default:
                sortField = "name";
                break;
        }
        
        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }

    @PostMapping("/packages")
    @ResponseBody
    public ResponseEntity<?> createPackage(
            @ModelAttribute PackageCreateRequestDTO requestDTO,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            Package packageEntity = entityMapper.toPackageEntity(requestDTO);
            
            // Set createdBy from current user
            User currentUser = AuthUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body("Không tìm thấy thông tin người dùng");
            }
            packageEntity.setCreatedBy(currentUser);
            
            // Handle image upload
            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.saveImage(image, "photobooking/packages");
                packageEntity.setImageUrl(imageUrl);
            } else {
                // Set default image URL if no image provided
                packageEntity.setImageUrl("/images/default-package.jpg");
            }
            
            Package savedPackage = packageService.save(packageEntity);
            PackageResponseDTO packageDTO = entityMapper.toPackageResponseDTO(savedPackage);
            return ResponseEntity.ok(packageDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi tạo gói: " + e.getMessage());
        }
    }

    @GetMapping("/packages/{id}")
    @ResponseBody
    public ResponseEntity<?> getPackage(@PathVariable UUID id) {
        try {
            Package packageEntity = packageService.findById(id);
            if (packageEntity != null) {
                PackageResponseDTO packageDTO = entityMapper.toPackageResponseDTO(packageEntity);
                return ResponseEntity.ok(packageDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi lấy thông tin gói: " + e.getMessage());
        }
    }

    @PutMapping("/packages/{id}")
    @ResponseBody   
    public ResponseEntity<?> updatePackage(
            @PathVariable UUID id,
            @ModelAttribute PackageUpdateRequestDTO requestDTO,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            Package existingPackage = packageService.findById(id);
            if (existingPackage != null) {
                entityMapper.updatePackageEntity(existingPackage, requestDTO);
                
                // Handle image upload
                if (image != null && !image.isEmpty()) {
                    // Delete old image from Cloudinary if it exists
                    String oldImageUrl = existingPackage.getImageUrl();
                    if (oldImageUrl != null && cloudinaryService.isCloudinaryUrl(oldImageUrl)) {
                        cloudinaryService.deleteImage(oldImageUrl);
                    }
                    
                    // Upload new image
                    String imageUrl = cloudinaryService.saveImage(image, "photobooking/packages");
                    existingPackage.setImageUrl(imageUrl);
                }
                // If no new image provided, keep existing imageUrl
                
                Package updatedPackage = packageService.save(existingPackage);
                PackageResponseDTO packageDTO = entityMapper.toPackageResponseDTO(updatedPackage);
                return ResponseEntity.ok(packageDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật gói: " + e.getMessage());
        }
    }

    @DeleteMapping("/packages/{id}")
    @ResponseBody
    public ResponseEntity<?> deletePackage(@PathVariable UUID id) {
        try {
            Package packageEntity = packageService.findById(id);
            if (packageEntity != null) {
                // Delete image from Cloudinary if it exists
                String imageUrl = packageEntity.getImageUrl();
                if (imageUrl != null && cloudinaryService.isCloudinaryUrl(imageUrl)) {
                    cloudinaryService.deleteImage(imageUrl);
                }
                
                packageService.deleteById(id);
                return ResponseEntity.ok("Xóa gói thành công");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xóa gói: " + e.getMessage());
        }
    }

    @PostMapping("/packages/{id}/toggle-status")
    @ResponseBody
    public ResponseEntity<?> togglePackageStatus(@PathVariable UUID id) {
        try {
            Package packageEntity = packageService.findById(id);
            if (packageEntity != null) {
                packageEntity.setActive(!packageEntity.getActive());
                Package updatedPackage = packageService.save(packageEntity);
                PackageResponseDTO packageDTO = entityMapper.toPackageResponseDTO(updatedPackage);
                return ResponseEntity.ok(packageDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi thay đổi trạng thái: " + e.getMessage());
        }
    }
    
}