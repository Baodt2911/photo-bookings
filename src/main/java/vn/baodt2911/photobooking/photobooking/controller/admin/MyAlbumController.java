package vn.baodt2911.photobooking.photobooking.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.baodt2911.photobooking.photobooking.dto.response.MyPhotoResponseDTO;
import vn.baodt2911.photobooking.photobooking.dto.request.MyAlbumUpdateRequestDTO;
import vn.baodt2911.photobooking.photobooking.util.AuthUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import vn.baodt2911.photobooking.photobooking.entity.MyAlbum;
import vn.baodt2911.photobooking.photobooking.entity.MyPhoto;
import vn.baodt2911.photobooking.photobooking.entity.User;
import vn.baodt2911.photobooking.photobooking.service.interfaces.MyAlbumService;
import vn.baodt2911.photobooking.photobooking.service.interfaces.MyPhotoService;
import vn.baodt2911.photobooking.photobooking.service.interfaces.UserService;
import vn.baodt2911.photobooking.photobooking.dto.request.MyAlbumCreateRequestDTO;
import vn.baodt2911.photobooking.photobooking.dto.request.MyAlbumUpdateRequestDTO;
import vn.baodt2911.photobooking.photobooking.dto.response.MyAlbumResponseDTO;
import vn.baodt2911.photobooking.photobooking.mapper.EntityMapper;
import vn.baodt2911.photobooking.photobooking.util.AuthUtil;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class MyAlbumController {

    @Autowired
    private MyAlbumService myAlbumService;
    
    @Autowired
    private MyPhotoService myPhotoService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EntityMapper entityMapper;

    @GetMapping("/my-albums")
    public String myAlbums(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(required = false) UUID userId,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        // Create Pageable object with sorting
        Pageable pageable = createPageable(page, size, sort);
        
        // Get my albums with search, filter and pagination
        Page<MyAlbum> myAlbums = myAlbumService.findMyAlbumsWithFilters(search, isPublic, userId, pageable);
        
        // Get statistics
        long totalMyAlbums = myAlbumService.count();
        long publicMyAlbums = myAlbumService.countByIsPublic(true);
        long totalMyPhotos = myPhotoService.count();
        long favoritePhotos = myPhotoService.countByIsFavorite(true);

        model.addAttribute("myAlbums", myAlbums);
        model.addAttribute("totalMyAlbums", totalMyAlbums);
        model.addAttribute("publicMyAlbums", publicMyAlbums);
        model.addAttribute("totalMyPhotos", totalMyPhotos);
        model.addAttribute("favoritePhotos", favoritePhotos);
        model.addAttribute("myAlbumService", myAlbumService);
        
        // Add search parameters for pagination links
        model.addAttribute("search", search);
        model.addAttribute("isPublic", isPublic);
        model.addAttribute("sort", sort);
        return "admin/my-album";
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
            case "created_at":
                sortField = "createdAt";
                break;
            case "updated_at":
                sortField = "updatedAt";
                break;
            case "name":
            default:
                sortField = "name";
                break;
        }
        
        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }

    @PostMapping("/my-albums")
    @ResponseBody
    public ResponseEntity<?> createMyAlbum(
            @ModelAttribute MyAlbumCreateRequestDTO requestDTO,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "coverPhotoIndex", required = false) Integer coverPhotoIndex) {
        try {
            // Get current user from authentication context
            UUID currentUserId = AuthUtil.getCurrentUserId();
            if (currentUserId == null) {
                return ResponseEntity.badRequest().body("Không tìm thấy thông tin người dùng");
            }
            
            MyAlbum myAlbumEntity = entityMapper.toMyAlbumEntity(requestDTO);
            
            // Set current user
            User currentUser = AuthUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body("Người dùng không tồn tại");
            }
            myAlbumEntity.setUser(currentUser);
            
            MyAlbum savedMyAlbum = myAlbumService.save(myAlbumEntity);
            
            // Upload images if provided
            List<MyPhoto> uploadedPhotos = new ArrayList<>();
            if (images != null && images.length > 0) {
                uploadedPhotos = myPhotoService.uploadMultiplePhotos(
                    savedMyAlbum.getId(), 
                    currentUserId, 
                    images
                );
                
                // Set cover photo if specified by index
                if (coverPhotoIndex != null && coverPhotoIndex >= 0 && coverPhotoIndex < uploadedPhotos.size()) {
                    MyPhoto coverPhoto = uploadedPhotos.get(coverPhotoIndex);
                    savedMyAlbum.setCoverPhotoId(coverPhoto.getId());
                    myAlbumService.save(savedMyAlbum);
                    System.out.println("Set cover photo: " + coverPhoto.getId());
                }
            }
            
            MyAlbumResponseDTO myAlbumDTO = entityMapper.toMyAlbumResponseDTO(savedMyAlbum);
            return ResponseEntity.ok(myAlbumDTO);
        } catch (Exception e) {
            System.err.println("Error creating my album: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi tạo album: " + e.getMessage());
        }
    }

    @GetMapping("/my-albums/{id}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<?> getMyAlbum(@PathVariable UUID id) {
        try {
            MyAlbum myAlbumEntity = myAlbumService.findById(id);
            if (myAlbumEntity != null) {
                MyAlbumResponseDTO myAlbumDTO = entityMapper.toMyAlbumResponseDTO(myAlbumEntity);
                return ResponseEntity.ok(myAlbumDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi lấy thông tin album: " + e.getMessage());
        }
    }

    @PutMapping("/my-albums/{id}")
    @ResponseBody   
    @Transactional
    public ResponseEntity<?> updateMyAlbum(
            @PathVariable UUID id,
            @ModelAttribute MyAlbumUpdateRequestDTO requestDTO) {
        try {
            MyAlbum existingMyAlbum = myAlbumService.findById(id);
            System.out.println("id: " + id);
            System.out.println("existingMyAlbum: " + existingMyAlbum);
            System.out.println("requestDTO: " + requestDTO);
            
            if (existingMyAlbum != null) {
                // Update basic album information
                entityMapper.updateMyAlbumEntity(existingMyAlbum, requestDTO);
                
                // Upload new images if any
                List<MyPhoto> uploadedPhotos = new ArrayList<>();
                if (requestDTO.getImages() != null && !requestDTO.getImages().isEmpty()) {
                    // Get current user from authentication context
                    UUID currentUserId = AuthUtil.getCurrentUserId();
                    uploadedPhotos = myPhotoService.uploadMultiplePhotos(
                        existingMyAlbum.getId(), 
                        currentUserId, 
                        requestDTO.getImages().toArray(new MultipartFile[0])
                    );
                }
                
                // Handle cover photo selection
                if (requestDTO.getCoverId() != null) {
                    // Cover photo is existing from DB - set coverId directly
                    existingMyAlbum.setCoverPhotoId(requestDTO.getCoverId());
                } else if (requestDTO.getCoverIndex() != null && !uploadedPhotos.isEmpty()) {
                    // Cover photo is new upload - set coverId from uploaded photos
                    if (requestDTO.getCoverIndex() < uploadedPhotos.size()) {
                        MyPhoto coverPhoto = uploadedPhotos.get(requestDTO.getCoverIndex());
                        existingMyAlbum.setCoverPhotoId(coverPhoto.getId());
                        System.out.println("Set cover photo: " + coverPhoto.getId());
                    }
                }
                
                MyAlbum updatedMyAlbum = myAlbumService.save(existingMyAlbum);
                return ResponseEntity.ok(Map.of("message", "Cập nhật album thành công", "id", updatedMyAlbum.getId()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật album: " + e.getMessage());
        }
    }

    @DeleteMapping("/my-albums/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteMyAlbum(@PathVariable UUID id) {
        try {
            myAlbumService.deleteById(id);
            return ResponseEntity.ok("Xóa album thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xóa album: " + e.getMessage());
        }
    }

    @PostMapping("/my-albums/{id}/toggle-visibility")
    @ResponseBody
    public ResponseEntity<?> toggleMyAlbumVisibility(@PathVariable UUID id) {
        try {
            MyAlbum myAlbumEntity = myAlbumService.findById(id);
            if (myAlbumEntity != null) {
                myAlbumEntity.setIsPublic(!myAlbumEntity.getIsPublic());
                MyAlbum updatedMyAlbum = myAlbumService.save(myAlbumEntity);
                MyAlbumResponseDTO myAlbumDTO = entityMapper.toMyAlbumResponseDTO(updatedMyAlbum);
                return ResponseEntity.ok(myAlbumDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi thay đổi trạng thái: " + e.getMessage());
        }
    }
    
    @PostMapping("/my-albums/{id}/photos")
    @ResponseBody
    public ResponseEntity<?> uploadPhotosToAlbum(
            @PathVariable UUID id,
            @RequestParam("images") MultipartFile[] images) {
        try {
            MyAlbum myAlbum = myAlbumService.findById(id);
            if (myAlbum == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Get current user from authentication context
            UUID currentUserId = AuthUtil.getCurrentUserId();
            if (currentUserId == null) {
                return ResponseEntity.badRequest().body("Không tìm thấy thông tin người dùng");
            }
            
            List<MyPhoto> uploadedPhotos = myPhotoService.uploadMultiplePhotos(
                id, 
                currentUserId, 
                images
            );
            
            return ResponseEntity.ok(uploadedPhotos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi upload ảnh: " + e.getMessage());
        }
    }
    
    @GetMapping("/my-albums/{id}/photos")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAlbumPhotos(@PathVariable UUID id) {
        try {
            MyAlbum myAlbum = myAlbumService.findById(id);
            if (myAlbum == null) {
                return ResponseEntity.notFound().build();
            }
            
            List<MyPhoto> photos = myPhotoService.findByMyAlbumId(id);
            // Convert to DTO to avoid lazy loading issues
            List<MyPhotoResponseDTO> photoDTOs = photos.stream()
                    .map(entityMapper::toMyPhotoResponseDTO)
                    .toList();
            return ResponseEntity.ok(photoDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi lấy danh sách ảnh: " + e.getMessage());
        }
    }

    @DeleteMapping("/my-albums/{albumId}/photos/{photoId}")
    @ResponseBody
    public ResponseEntity<?> deleteAlbumPhoto(@PathVariable UUID albumId, @PathVariable UUID photoId) {
        try {
            MyPhoto photo = myPhotoService.findById(photoId);
            if (photo == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if photo belongs to the album
            if (!photo.getMyAlbum().getId().equals(albumId)) {
                return ResponseEntity.badRequest().body("Ảnh không thuộc album này");
            }
            
            myPhotoService.deleteById(photoId);
            return ResponseEntity.ok("Xóa ảnh thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xóa ảnh: " + e.getMessage());
        }
    }
    
    private UUID getDefaultUserId() {
        return AuthUtil.getCurrentUserId();
    }
    
    @GetMapping("/my-albums/test")
    @ResponseBody
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok("Test endpoint working");
    }
}
