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
import jakarta.servlet.http.HttpServletRequest;
import vn.baodt2911.photobooking.photobooking.entity.Album;
import vn.baodt2911.photobooking.photobooking.entity.Photo;
import vn.baodt2911.photobooking.photobooking.entity.User;
import vn.baodt2911.photobooking.photobooking.service.interfaces.AlbumService;
import vn.baodt2911.photobooking.photobooking.service.interfaces.UserService;
import vn.baodt2911.photobooking.photobooking.service.interfaces.GoogleDriveServiceInterface;
import vn.baodt2911.photobooking.photobooking.dto.request.AlbumCreateRequestDTO;
import vn.baodt2911.photobooking.photobooking.dto.request.AlbumUpdateRequestDTO;
import vn.baodt2911.photobooking.photobooking.dto.response.AlbumResponseDTO;
import vn.baodt2911.photobooking.photobooking.mapper.EntityMapper;
import vn.baodt2911.photobooking.photobooking.repository.PhotoRepository;
import vn.baodt2911.photobooking.photobooking.repository.PhotoMarkRepository;
import vn.baodt2911.photobooking.photobooking.repository.PhotoCommentRepository;
import vn.baodt2911.photobooking.photobooking.entity.PhotoMark;
import vn.baodt2911.photobooking.photobooking.entity.PhotoComment;
import vn.baodt2911.photobooking.photobooking.util.AuthUtil;

import java.util.*;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

@Controller
@RequestMapping("/admin")
public class AlbumController {

    @Autowired
    private AlbumService albumService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EntityMapper entityMapper;
    
    @Autowired
    private GoogleDriveServiceInterface googleDriveService;
    
    @Autowired
    private PhotoRepository photoRepository;
    
    @Autowired
    private PhotoMarkRepository photoMarkRepository;
    
    @Autowired
    private PhotoCommentRepository photoCommentRepository;

    @GetMapping("/albums")
    public String albums(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean allowDownload,
            @RequestParam(required = false) Boolean allowComment,
            @RequestParam(required = false) UUID createdById,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request,
            Model model) {
        
        // Create Pageable object with sorting
        Pageable pageable = createPageable(page, size, sort);
        
        // Get albums with search, filter and pagination
        Page<Album> albums = albumService.findAlbumsWithFilters(search, allowDownload, allowComment, createdById, pageable);
        
        // Process albums to add cover photo URLs and stats
        albums.getContent().forEach(album -> {
            if (album.getCoverPhotoId() != null) {
                try {
                    Photo coverPhoto = photoRepository.findById(album.getCoverPhotoId()).orElse(null);
                    if (coverPhoto != null && coverPhoto.getDriveFileId() != null) {
                        // Generate cover photo URL using driveFileId
                        String coverPhotoUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/admin/albums/photo/" + coverPhoto.getDriveFileId();
                        album.setCoverPhotoUrl(coverPhotoUrl);
                    }
                } catch (Exception e) {
                    System.err.println("Error getting cover photo for album " + album.getId() + ": " + e.getMessage());
                }
            }
            
            // Calculate album stats
            try {
                List<Photo> photos = photoRepository.findByAlbumId(album.getId());
                long favoriteCount = 0;
                long commentCount = 0;
                
                System.out.println("=== DEBUG STATS FOR ALBUM " + album.getId() + " ===");
                System.out.println("Total photos: " + photos.size());
                
                for (Photo photo : photos) {
                    try {
                        // Count favorites from PhotoMark
                        Optional<PhotoMark> markOpt = photoMarkRepository.findByPhoto(photo);
                        if (markOpt.isPresent() && markOpt.get().getIsFavorite()) {
                            favoriteCount++;
                            System.out.println("Photo " + photo.getId() + " has favorite");
                        }
                        
                        // Count comments from PhotoComment
                        List<PhotoComment> comments = photoCommentRepository.findByPhoto(photo);
                        commentCount += comments.size();
                        if (comments.size() > 0) {
                            System.out.println("Photo " + photo.getId() + " has " + comments.size() + " comments");
                        }
                    } catch (Exception e) {
                        // Skip this photo if there's an error
                        System.err.println("Error getting marks/comments for photo " + photo.getId() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                System.out.println("Album " + album.getId() + " final stats: " + favoriteCount + " favorites, " + commentCount + " comments");
                System.out.println("=== END DEBUG STATS ===");
                
                // Set stats as transient fields
                album.setFavoriteCount((long) favoriteCount);
                album.setCommentCount((long) commentCount);
            } catch (Exception e) {
                System.err.println("Error calculating stats for album " + album.getId() + ": " + e.getMessage());
                e.printStackTrace();
                album.setFavoriteCount(0L);
                album.setCommentCount(0L);
            }
        });
        
        // Get statistics
        long totalAlbums = albumService.count();
        long downloadAlbums = albumService.countByAllowDownload(true);
        long commentAlbums = albumService.countByAllowComment(true);

        model.addAttribute("albums", albums);
        model.addAttribute("totalAlbums", totalAlbums);
        model.addAttribute("downloadAlbums", downloadAlbums);
        model.addAttribute("commentAlbums", commentAlbums);
        
        // Add search parameters for pagination links
        model.addAttribute("search", search);
        model.addAttribute("allowDownload", allowDownload);
        model.addAttribute("allowComment", allowComment);
        model.addAttribute("createdById", createdById);
        model.addAttribute("sort", sort);

        return "admin/album";
    }

    @GetMapping("/albums/photo/{driveFileId}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable String driveFileId) {
        try {
            // Get photo from Google Drive
            String driveUrl = "https://drive.usercontent.google.com/download?id=" + driveFileId + "&export=view&authuser=0";
            
            // Create HTTP client
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(driveUrl))
                    .GET()
                    .build();
            
            java.net.http.HttpResponse<byte[]> response = client.send(request,      
                    java.net.http.HttpResponse.BodyHandlers.ofByteArray());
            
            if (response.statusCode() == 200) {
                byte[] imageData = response.body();
                
                // Determine content type based on file extension or content
                String contentType = determineContentType(driveFileId, imageData);
                
                return ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                        .body(imageData);
            } else {
                System.err.println("Failed to get photo from Google Drive. Status: " + response.statusCode());
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error getting photo from Google Drive: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Determine content type based on file extension or content
     */
    private String determineContentType(String driveFileId, byte[] imageData) {
        // Try to determine from file extension first
        if (driveFileId != null) {
            // For Google Drive, we can't easily get file extension from ID
            // So we'll try to detect from content
        }
        
        // Detect from content (magic bytes)
        if (imageData.length >= 4) {
            // JPEG
            if (imageData[0] == (byte) 0xFF && imageData[1] == (byte) 0xD8) {
                return "image/jpeg";
            }
            // PNG
            if (imageData[0] == (byte) 0x89 && imageData[1] == (byte) 0x50 && 
                imageData[2] == (byte) 0x4E && imageData[3] == (byte) 0x47) {
                return "image/png";
            }
            // GIF
            if (imageData[0] == (byte) 0x47 && imageData[1] == (byte) 0x49 && 
                imageData[2] == (byte) 0x46) {
                return "image/gif";
            }
        }
        
        // Default to JPEG if we can't determine
        return "image/jpeg";
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


    @PostMapping("/albums")
    @ResponseBody
    public ResponseEntity<?> createAlbum(@ModelAttribute AlbumCreateRequestDTO requestDTO) {
        try {
            System.out.println("=== CREATE ALBUM DEBUG ===");
            System.out.println("Request DTO: " + requestDTO);
            
            Album albumEntity = entityMapper.toAlbumEntity(requestDTO);
            System.out.println("Album Entity created: " + albumEntity.getName());
            
            // Get current user from AuthUtil
            User currentUser = AuthUtil.getCurrentUser();
            System.out.println("Current User: " + (currentUser != null ? currentUser.getEmail() : "NULL"));
            
            if (currentUser != null) {
                albumEntity.setCreatedBy(currentUser);
                System.out.println("Set createdBy user: " + currentUser.getEmail());
            }
            
            // Save album first
            System.out.println("Attempting to save album...");
            Album savedAlbum = albumService.save(albumEntity);
            System.out.println("Album saved successfully with ID: " + savedAlbum.getId());
            
            // If Google Drive link is provided, fetch and save photos
            if (requestDTO.getDriveFolderLink() != null && !requestDTO.getDriveFolderLink().trim().isEmpty()) {
                try {
                    String folderId = googleDriveService.extractFolderIdFromUrl(requestDTO.getDriveFolderLink());
                    if (folderId != null) {
                        List<Map<String, Object>> drivePhotos = googleDriveService.getImagesFromFolder(folderId);
                        System.out.println("Fetched " + drivePhotos.size() + " photos from Google Drive for album: " + savedAlbum.getId());
                        
                        // Save photos to database
                        UUID firstPhotoId = null;
                        for (int i = 0; i < drivePhotos.size(); i++) {
                            Map<String, Object> photoData = drivePhotos.get(i);
                            
                            Photo photo = new Photo();
                            photo.setAlbum(savedAlbum);
                            photo.setDriveFileId((String) photoData.get("id"));
                            photo.setName((String) photoData.get("name"));
                            photo.setThumbnailUrl((String) photoData.get("thumbnailUrl"));
                            photo.setOrderIndex(i + 1); // Start from 1
                            
                            Photo savedPhoto = photoRepository.save(photo);
                            
                            // Set first photo as cover photo
                            if (i == 0) {
                                firstPhotoId = savedPhoto.getId();
                            }
                            
                            System.out.println("Saved photo: " + photo.getName() + " (ID: " + savedPhoto.getId() + ")");
                        }
                        
                        // Update album with cover photo ID
                        if (firstPhotoId != null) {
                            savedAlbum.setCoverPhotoId(firstPhotoId);
                            albumService.save(savedAlbum);
                            System.out.println("Set cover photo ID: " + firstPhotoId);
                        }
                    }
                } catch (Exception e) {
                    // Log error but don't fail album creation
                    System.err.println("Error fetching photos from Google Drive: " + e.getMessage());
                }
            }
            
            AlbumResponseDTO albumDTO = entityMapper.toAlbumResponseDTO(savedAlbum);
            return ResponseEntity.ok(albumDTO);
        } catch (Exception e) {
            System.err.println("=== CREATE ALBUM ERROR ===");
            System.err.println("Error type: " + e.getClass().getSimpleName());
            System.err.println("Error message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi tạo album: " + e.getMessage());
        }
    }

    @GetMapping("/albums/{id}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAlbum(@PathVariable UUID id) {
        try {
            Album albumEntity = albumService.findById(id);
            if (albumEntity != null) {
                AlbumResponseDTO albumDTO = entityMapper.toAlbumResponseDTO(albumEntity);
                return ResponseEntity.ok(albumDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error getting album: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi lấy thông tin album: " + e.getMessage());
        }
    }

    @GetMapping("/albums/{id}/view")
    @Transactional(readOnly = true)
    public String viewAlbum(@PathVariable UUID id, HttpServletRequest request, Model model) {
        try {
            // Lấy thông tin album
            Album album = albumService.findById(id);
            if (album == null) {
                return "error/404";
            }
            
            // Set createdByName to avoid lazy loading issues
            if (album.getCreatedBy() != null) {
                album.setCreatedByName(album.getCreatedBy().getName());
            }
            
            // Lấy danh sách photos trong album
            List<Photo> photos = photoRepository.findByAlbumId(id);
            
            // Tạo URL cho cover photo
            if (album.getCoverPhotoId() != null) {
                try {
                    Photo coverPhoto = photoRepository.findById(album.getCoverPhotoId()).orElse(null);
                    if (coverPhoto != null && coverPhoto.getDriveFileId() != null) {
                        String coverPhotoUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/admin/albums/photo/" + coverPhoto.getDriveFileId();
                        album.setCoverPhotoUrl(coverPhotoUrl);
                    }
                } catch (Exception e) {
                    System.err.println("Error getting cover photo for album " + album.getId() + ": " + e.getMessage());
                }
            }
            
            // Tạo URL cho từng photo
            photos.forEach(photo -> {
                if (photo.getDriveFileId() != null) {
                    String photoUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/admin/albums/photo/" + photo.getDriveFileId();
                    photo.setThumbnailUrl(photoUrl);
                }
            });
            
            // Thêm vào model
            model.addAttribute("album", album);
            model.addAttribute("photos", photos);
            
            return "admin/view-album";
        } catch (Exception e) {
            System.err.println("Error viewing album: " + e.getMessage());
            e.printStackTrace();
            return "error/500";
        }
    }

    @GetMapping("/albums/test-comments")
    @ResponseBody
    public ResponseEntity<?> testComments() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Get all photos
            List<Photo> allPhotos = photoRepository.findAll();
            result.put("totalPhotos", allPhotos.size());
            
            List<Map<String, Object>> photoData = new ArrayList<>();
            
            for (Photo photo : allPhotos) {
                Map<String, Object> photoInfo = new HashMap<>();
                photoInfo.put("photoId", photo.getId());
                photoInfo.put("photoName", photo.getName());
                photoInfo.put("albumId", photo.getAlbum().getId());
                
                // Count comments
                List<PhotoComment> comments = photoCommentRepository.findByPhoto(photo);
                photoInfo.put("commentCount", comments.size());
                
                // Count favorites
                Optional<PhotoMark> mark = photoMarkRepository.findByPhoto(photo);
                photoInfo.put("hasFavorite", mark.isPresent() && mark.get().getIsFavorite());
                
                photoData.add(photoInfo);
            }
            
            result.put("photos", photoData);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/albums/{id}/edit")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAlbumForEdit(@PathVariable UUID id) {
        try {
            Album albumEntity = albumService.findById(id);
            if (albumEntity != null) {
                AlbumResponseDTO albumDTO = entityMapper.toAlbumResponseDTO(albumEntity);
                
                // Thêm isPassword vào response
                boolean isPassword = albumEntity.getPassword() != null && !albumEntity.getPassword().trim().isEmpty();
                albumDTO.setPassword(isPassword ? "***" : null); // Set placeholder hoặc null
                
                // Tạo Map để thêm isPassword
                Map<String, Object> response = new HashMap<>();
                response.put("id", albumDTO.getId());
                response.put("name", albumDTO.getName());
                response.put("customerName", albumDTO.getCustomerName());
                response.put("driveFolderLink", albumDTO.getDriveFolderLink());
                response.put("password", albumDTO.getPassword());
                response.put("isPassword", isPassword); // Thêm field này
                response.put("allowDownload", albumDTO.getAllowDownload());
                response.put("allowComment", albumDTO.getAllowComment());
                response.put("limitSelection", albumDTO.getLimitSelection());
                response.put("coverPhotoId", albumDTO.getCoverPhotoId());
                response.put("createdByName", albumDTO.getCreatedByName());
                response.put("createdAt", albumDTO.getCreatedAt());
                response.put("updatedAt", albumDTO.getUpdatedAt());
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error getting album for edit: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi lấy thông tin album: " + e.getMessage());
        }
    }

    @PutMapping("/albums/{id}")
    @ResponseBody   
    @Transactional
    public ResponseEntity<?> updateAlbum(
            @PathVariable UUID id,
            @ModelAttribute AlbumUpdateRequestDTO requestDTO) {
        try {
            Album existingAlbum = albumService.findById(id);
            if (existingAlbum != null) {
                entityMapper.updateAlbumEntity(existingAlbum, requestDTO);
                
                Album updatedAlbum = albumService.save(existingAlbum);
                AlbumResponseDTO albumDTO = entityMapper.toAlbumResponseDTO(updatedAlbum);
                return ResponseEntity.ok(albumDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error updating album: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật album: " + e.getMessage());
        }
    }

    @DeleteMapping("/albums/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteAlbum(@PathVariable UUID id) {
        try {
            System.out.println("=== DELETE ALBUM DEBUG ===");
            System.out.println("Album ID to delete: " + id);
            
            // Check if album exists
            Album album = albumService.findById(id);
            if (album == null) {
                System.out.println("Album not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("Found album: " + album.getName());
            
            // Delete album (cascade will handle photos)
            albumService.deleteById(id);
            
            System.out.println("Album deleted successfully: " + id);
            return ResponseEntity.ok(Map.of("message", "Album đã được xóa thành công"));
        } catch (Exception e) {
            System.err.println("=== DELETE ALBUM ERROR ===");
            System.err.println("Error type: " + e.getClass().getSimpleName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi khi xóa album: " + e.getMessage()));
        }
    }

    @PostMapping("/albums/{id}/toggle-download")
    @ResponseBody
    public ResponseEntity<?> toggleAlbumDownload(@PathVariable UUID id) {
        try {
            Album albumEntity = albumService.findById(id);
            if (albumEntity != null) {
                albumEntity.setAllowDownload(!albumEntity.getAllowDownload());
                Album updatedAlbum = albumService.save(albumEntity);
                AlbumResponseDTO albumDTO = entityMapper.toAlbumResponseDTO(updatedAlbum);
                return ResponseEntity.ok(albumDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi thay đổi trạng thái download: " + e.getMessage());
        }
    }

    @PostMapping("/albums/{id}/toggle-comment")
    @ResponseBody
    public ResponseEntity<?> toggleAlbumComment(@PathVariable UUID id) {
        try {
            Album albumEntity = albumService.findById(id);
            if (albumEntity != null) {
                albumEntity.setAllowComment(!albumEntity.getAllowComment());
                Album updatedAlbum = albumService.save(albumEntity);
                AlbumResponseDTO albumDTO = entityMapper.toAlbumResponseDTO(updatedAlbum);
                return ResponseEntity.ok(albumDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi thay đổi trạng thái comment: " + e.getMessage());
        }
    }

    @PostMapping("/albums/validate-drive-link")
    @ResponseBody
    public ResponseEntity<?> validateDriveLink(@RequestParam String driveLink) {
        try {
            // Extract folder ID from Google Drive link
            String folderId = googleDriveService.extractFolderIdFromUrl(driveLink);
            if (folderId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", "Link Google Drive không hợp lệ"
                ));
            }

            // Check if folder is public using Google Drive API
            boolean isPublic = googleDriveService.isFolderPublic(folderId);
            if (isPublic) {
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "folderId", folderId
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "Vui lòng chuyển folder sang public"
                ));
            }
        } catch (Exception e) {
            System.err.println("Controller error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "valid", false,
                "message", "Lỗi khi kiểm tra link: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/albums/fetch-drive-photos")
    @ResponseBody
    public ResponseEntity<?> fetchDrivePhotos(@RequestParam String driveLink) {
        try {
            String folderId = googleDriveService.extractFolderIdFromUrl(driveLink);
            if (folderId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Link Google Drive không hợp lệ"
                ));
            }

            // Fetch photos from Google Drive folder using real API
            List<Map<String, Object>> photos = googleDriveService.getImagesFromFolder(folderId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã lấy được " + photos.size() + " ảnh từ Google Drive",
                "photos", photos
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Lỗi khi lấy ảnh từ Google Drive: " + e.getMessage()
            ));
        }
    }

    /**
     * Set current photo as cover photo
     */
    @PostMapping("/albums/{albumId}/set-cover")
    @ResponseBody
    public ResponseEntity<?> setCoverPhoto(
            @PathVariable UUID albumId,
            @RequestBody Map<String, String> request) {
        try {
            System.out.println("=== SET COVER PHOTO DEBUG ===");
            System.out.println("Album ID: " + albumId);
            System.out.println("Request body: " + request);
            
            String photoId = request.get("photoId");
            if (photoId == null || photoId.trim().isEmpty()) {
                System.out.println("Photo ID is null or empty");
                return ResponseEntity.badRequest().body("Photo ID is required");
            }

            // Find the album
            Album album = albumService.findById(albumId);
            if (album == null) {
                return ResponseEntity.notFound().build();
            }

            // Find the photo
            Photo photo = photoRepository.findById(UUID.fromString(photoId)).orElse(null);
            if (photo == null) {
                return ResponseEntity.badRequest().body("Photo not found");
            }

            // Check if photo belongs to this album
            if (!photo.getAlbum().getId().equals(albumId)) {
                return ResponseEntity.badRequest().body("Photo does not belong to this album");
            }

            // Update album cover photo
            album.setCoverPhotoId(UUID.fromString(photoId));
            albumService.save(album);

            return ResponseEntity.ok(Map.of("message", "Cover photo updated successfully"));

        } catch (Exception e) {
            System.err.println("Error setting cover photo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error setting cover photo: " + e.getMessage());
        }
    }

    @PostMapping("/albums/{albumId}/photos/{photoId}/select")
    @ResponseBody
    public ResponseEntity<?> toggleSelect(
            @PathVariable UUID albumId,
            @PathVariable UUID photoId) {
        try {
            System.out.println("=== TOGGLE SELECT DEBUG ===");
            System.out.println("Album ID: " + albumId);
            System.out.println("Photo ID: " + photoId);
            
            // Find the photo
            Photo photo = photoRepository.findById(photoId).orElse(null);
            if (photo == null) {
                return ResponseEntity.badRequest().body("Photo not found");
            }

            // Check if photo belongs to this album
            if (!photo.getAlbum().getId().equals(albumId)) {
                return ResponseEntity.badRequest().body("Photo does not belong to this album");
            }

            // Find existing PhotoMark or create new one
            PhotoMark photoMark = photoMarkRepository.findByPhoto(photo).orElse(null);
            if (photoMark == null) {
                // Create new PhotoMark for this photo
                photoMark = new PhotoMark();
                photoMark.setPhoto(photo);
                photoMark.setIsSelected(false);
                photoMark.setIsFavorite(false);
            }

            // Toggle isSelected status
            boolean newSelectedStatus = !photoMark.getIsSelected();
            photoMark.setIsSelected(newSelectedStatus);
            photoMarkRepository.save(photoMark);

            System.out.println("PhotoMark saved with isSelected: " + newSelectedStatus);

            return ResponseEntity.ok(Map.of(
                "message", "Select status toggled successfully",
                "isSelected", newSelectedStatus,
                "success", true
            ));

        } catch (Exception e) {
            System.err.println("Error toggling select: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error toggling select: " + e.getMessage());
        }
    }

    @GetMapping("/albums/{albumId}/photos/{photoId}/status")
    @ResponseBody
    public ResponseEntity<?> getPhotoStatus(
            @PathVariable UUID albumId,
            @PathVariable UUID photoId) {
        try {
            // Find the photo
            Photo photo = photoRepository.findById(photoId).orElse(null);
            if (photo == null) {
                return ResponseEntity.badRequest().body("Photo not found");
            }

            // Check if photo belongs to this album
            if (!photo.getAlbum().getId().equals(albumId)) {
                return ResponseEntity.badRequest().body("Photo does not belong to this album");
            }

            // Get PhotoMark for this photo
            PhotoMark photoMark = photoMarkRepository.findByPhoto(photo).orElse(null);
            boolean isSelected = photoMark != null && photoMark.getIsSelected() != null && photoMark.getIsSelected();
            boolean isFavorite = photoMark != null && photoMark.getIsFavorite() != null && photoMark.getIsFavorite();
            
            // Get comment status
            List<PhotoComment> comments = photoCommentRepository.findByPhoto(photo);
            boolean hasComment = comments.size() > 0;

            return ResponseEntity.ok(Map.of(
                "isSelected", isSelected,
                "isFavorite", isFavorite,
                "hasComment", hasComment
            ));

        } catch (Exception e) {
            System.err.println("Error getting photo status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error getting photo status: " + e.getMessage());
        }
    }

    @GetMapping("/albums/{albumId}/download")
    public ResponseEntity<?> downloadAlbum(
            @PathVariable UUID albumId,
            @RequestParam(defaultValue = "all") String filter) {
        try {
            // Find the album
            Album album = albumService.findById(albumId);
            if (album == null) {
                return ResponseEntity.notFound().build();
            }

            // Get photos based on filter
            List<Photo> photos = getPhotosByFilter(album, filter);
            
            if (photos.isEmpty()) {
                // Return JSON message with 200 OK to avoid browser error
                String message = getEmptyFilterMessage(filter);
                return ResponseEntity.ok()
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", message, "success", false, "isEmpty", true));
            }

            // Create zip file
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                for (Photo photo : photos) {
                    try {
                        // Download image from Google Drive using driveFileId
                        String driveUrl = "https://drive.usercontent.google.com/download?id=" + photo.getDriveFileId() + "&export=view&authuser=0";
                        
                        // Create HTTP client
                        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                                .uri(java.net.URI.create(driveUrl))
                                .GET()
                                .build();
                        
                        java.net.http.HttpResponse<byte[]> response = client.send(request,      
                                java.net.http.HttpResponse.BodyHandlers.ofByteArray());
                        
                        if (response.statusCode() == 200) {
                            byte[] imageData = response.body();
                            
                            // Create zip entry
                            String fileName = photo.getName() != null ? photo.getName() : "photo_" + photo.getId() + ".jpg";
                            ZipEntry entry = new ZipEntry(fileName);
                            zos.putNextEntry(entry);
                            zos.write(imageData);
                            zos.closeEntry();
                        } else {
                            System.err.println("Failed to download photo " + photo.getId() + " from Google Drive. Status: " + response.statusCode());
                        }
                        
                    } catch (Exception e) {
                        System.err.println("Error processing photo " + photo.getId() + ": " + e.getMessage());
                        // Continue with other photos
                    }
                }
            }

            // Prepare response
            byte[] zipData = baos.toByteArray();
            String fileName = album.getName() + "_" + filter + ".zip";
            
            // Sanitize filename
            fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .header("Content-Type", "application/zip")
                    .body(zipData);

        } catch (Exception e) {
            System.err.println("Error creating download: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private List<Photo> getPhotosByFilter(Album album, String filter) {
        List<Photo> allPhotos = photoRepository.findByAlbumId(album.getId());
        
        switch (filter) {
            case "all":
                return allPhotos;
            case "recommended":
                // Recommended photos are photos marked with star by admin (is_selected = true)
                return allPhotos.stream()
                        .filter(photo -> {
                            PhotoMark photoMark = photoMarkRepository.findByPhoto(photo).orElse(null);
                            return photoMark != null && photoMark.getIsSelected() != null && photoMark.getIsSelected();
                        })
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            case "selected":
                // Selected photos are photos marked with heart by client (is_favorite = true)
                return allPhotos.stream()
                        .filter(photo -> {
                            PhotoMark photoMark = photoMarkRepository.findByPhoto(photo).orElse(null);
                            return photoMark != null && photoMark.getIsFavorite() != null && photoMark.getIsFavorite();
                        })
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            case "commented":
                // For now, return empty list (can be enhanced when comment system is implemented)
                return new ArrayList<>();
            case "tagged":
                // For now, return empty list (can be enhanced when tagging system is implemented)
                return new ArrayList<>();
            default:
                return allPhotos;
        }
    }

    private String getEmptyFilterMessage(String filter) {
        switch (filter) {
            case "all":
                return "Không có ảnh nào trong album";
            case "recommended":
                return "Không có ảnh được đề xuất";
            case "selected":
                return "Không có ảnh đã được chọn";
            case "commented":
                return "Không có ảnh nào có bình luận";
            case "tagged":
                return "Không có ảnh nào có gắn thẻ";
            default:
                return "Không có ảnh nào cho lựa chọn này";
        }
    }

}