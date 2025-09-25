package vn.baodt2911.photobooking.photobooking.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.baodt2911.photobooking.photobooking.entity.Album;
import vn.baodt2911.photobooking.photobooking.entity.Photo;
import vn.baodt2911.photobooking.photobooking.entity.PhotoMark;
import vn.baodt2911.photobooking.photobooking.repository.PhotoCommentRepository;
import vn.baodt2911.photobooking.photobooking.entity.PhotoComment;
import vn.baodt2911.photobooking.photobooking.repository.PhotoMarkRepository;
import vn.baodt2911.photobooking.photobooking.service.interfaces.AlbumService;
import vn.baodt2911.photobooking.photobooking.repository.PhotoRepository;
import vn.baodt2911.photobooking.photobooking.util.PasswordUtil;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PublicAlbumController {

    @Autowired
    private AlbumService albumService;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private PhotoCommentRepository photoCommentRepository;
    
    @Autowired
    private PhotoMarkRepository photoMarkRepository;

    /**
     * Album view page - allows viewing and interacting with photos
     * URL: /album/:albumId
     */
    @GetMapping("/album/{albumId}")
    public String viewAlbum(@PathVariable UUID albumId, HttpServletRequest request, Model model) {
        Album album = albumService.findById(albumId);
        if (album == null) {
            return "error/404";
        }

        // Lấy danh sách photos trong album
        List<Photo> photos = photoRepository.findByAlbumId(albumId);
        
        // Tạo URL cho cover photo
        if (album.getCoverPhotoId() != null) {
            try {
                Photo coverPhoto = photoRepository.findById(album.getCoverPhotoId()).orElse(null);
                if (coverPhoto != null && coverPhoto.getDriveFileId() != null) {
                    String coverPhotoUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/api/photos/" + coverPhoto.getDriveFileId();
                    album.setCoverPhotoUrl(coverPhotoUrl);
                }
            } catch (Exception e) {
                System.err.println("Error getting cover photo for album " + album.getId() + ": " + e.getMessage());
            }
        }
        
        // Tạo URL cho từng photo (chỉ khi không có password)
        if (album.getPassword() == null || album.getPassword().isEmpty()) {
            photos.forEach(photo -> {
                if (photo.getDriveFileId() != null) {
                    String photoUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/api/photos/" + photo.getDriveFileId();
                    photo.setThumbnailUrl(photoUrl);
                }
            });
        }

        model.addAttribute("album", album);
        model.addAttribute("albumMode", "select");
        model.addAttribute("photos", photos);
        return "client/album";
    }

    /**
     * Album selection page - shows only recommended photos for selection
     * URL: /show/album/:albumId
     */
    @GetMapping("/show/album/{albumId}")
    public String showAlbum(@PathVariable UUID albumId, HttpServletRequest request, Model model) {
        Album album = albumService.findById(albumId);
        if (album == null) {
            return "error/404";
        }

        // Lấy danh sách photos trong album
        List<Photo> photos = photoRepository.findByAlbumId(albumId);
        
        // Tạo URL cho cover photo
        if (album.getCoverPhotoId() != null) {
            try {
                Photo coverPhoto = photoRepository.findById(album.getCoverPhotoId()).orElse(null);
                if (coverPhoto != null && coverPhoto.getDriveFileId() != null) {
                    String coverPhotoUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/api/photos/" + coverPhoto.getDriveFileId();
                    album.setCoverPhotoUrl(coverPhotoUrl);
                }
            } catch (Exception e) {
                System.err.println("Error getting cover photo for album " + album.getId() + ": " + e.getMessage());
            }
        }
        
        // Tạo URL cho từng photo (chỉ khi không có password)
        if (album.getPassword() == null || album.getPassword().isEmpty()) {
            photos.forEach(photo -> {
                if (photo.getDriveFileId() != null) {
                    String photoUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/api/photos/" + photo.getDriveFileId();
                    photo.setThumbnailUrl(photoUrl);
                }
            });
        }

        model.addAttribute("album", album);
        model.addAttribute("albumMode", "view");
        model.addAttribute("photos", photos);
        return "client/album";
    }

    /**
     * Verify album password
     */
    @PostMapping("/api/albums/{albumId}/verify-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyPassword(
            @PathVariable UUID albumId,
            @RequestBody Map<String, String> request) {
        
        try {
            Album album = albumService.findById(albumId);
            if (album == null) {
                return ResponseEntity.notFound().build();
            }

            String providedPassword = request.get("password");
            String albumPassword = album.getPassword();

            // If album has no password, allow access
            if (albumPassword == null || albumPassword.isEmpty()) {
                return ResponseEntity.ok(Map.of("success", true));
            }

            // Check password using PasswordUtil.matches for hashed passwords
            if (PasswordUtil.matches(providedPassword, albumPassword)) {
                return ResponseEntity.ok(Map.of("success", true));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mật khẩu không đúng"));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Lỗi xác thực"));
        }
    }

    /**
     * Get photos for album
     */
    @GetMapping("/api/albums/{albumId}/photos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAlbumPhotos(@PathVariable UUID albumId) {
        try {
            Album album = albumService.findById(albumId);
            if (album == null) {
                return ResponseEntity.notFound().build();
            }

            List<Photo> photos = photoRepository.findByAlbumId(albumId);
            
            // Convert photos to DTO with only essential info
            List<Map<String, Object>> photoDTOs = photos.stream().map(photo -> {
                Map<String, Object> photoDTO = new HashMap<>();
                photoDTO.put("id", photo.getId());
                photoDTO.put("driveFileId", photo.getDriveFileId());
                photoDTO.put("thumbnailUrl", "/api/photos/" + photo.getDriveFileId());
                
                // Get photo marks
                try {
                    Optional<PhotoMark> photoMark = photoMarkRepository.findByPhoto(photo);
                    photoDTO.put("isFavorite", photoMark.map(PhotoMark::getIsFavorite).orElse(false));
                    photoDTO.put("isSelected", photoMark.map(PhotoMark::getIsSelected).orElse(false));
                } catch (Exception e) {
                    // If there's an error getting photo marks, set defaults
                    photoDTO.put("isFavorite", false);
                    photoDTO.put("isSelected", false);
                }
                
                // Get comment status
                try {
                    List<PhotoComment> comments = photoCommentRepository.findByPhoto(photo);
                    photoDTO.put("hasComment", comments.size() > 0);
                } catch (Exception e) {
                    photoDTO.put("hasComment", false);
                }
                
                return photoDTO;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "photos", photoDTOs
            ));

        } catch (Exception e) {
            e.printStackTrace(); // Debug logging
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Lỗi tải ảnh: " + e.getMessage()));
        }
    }

    /**
     * Get photo status (favorite, selected, commented)
     */
    @GetMapping("/api/albums/{albumId}/photos/{photoId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPhotoStatus(
            @PathVariable UUID albumId,
            @PathVariable UUID photoId) {
        
        try {
            Photo photo = photoRepository.findById(photoId).orElse(null);
            if (photo == null) {
                return ResponseEntity.notFound().build();
            }

            // Get PhotoMark
            Optional<PhotoMark> photoMark = photoMarkRepository.findByPhoto(photo);
            
            // Get comment status
            List<PhotoComment> comments = photoCommentRepository.findByPhoto(photo);
            
            Map<String, Object> status = new HashMap<>();
            status.put("isFavorite", photoMark.map(PhotoMark::getIsFavorite).orElse(false));
            status.put("isSelected", photoMark.map(PhotoMark::getIsSelected).orElse(false));
            status.put("hasComment", comments.size() > 0);
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            System.err.println("Error getting photo status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi server"));
        }
    }

    /**
     * Toggle photo favorite status
     */
    @PostMapping("/api/photos/{photoId}/favorite")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @PathVariable UUID photoId,
            @RequestBody Map<String, Object> request) {
        
        try {
            Photo photo = photoRepository.findById(photoId).orElse(null);
            if (photo == null) {
                return ResponseEntity.notFound().build();
            }

            Boolean isFavorite = (Boolean) request.get("isFavorite");
            if (isFavorite == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu thông tin"));
            }

            // Get or create PhotoMark
            Optional<PhotoMark> existingMark = photoMarkRepository.findByPhoto(photo);
            PhotoMark photoMark;
            
            if (existingMark.isPresent()) {
                photoMark = existingMark.get();
            } else {
                photoMark = new PhotoMark();
                photoMark.setPhoto(photo);
            }

            photoMark.setIsFavorite(isFavorite);
            photoMarkRepository.save(photoMark);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "isFavorite", isFavorite
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Lỗi cập nhật"));
        }
    }

    /**
     * Get comments for photo
     */
    @GetMapping("/api/photos/{photoId}/comments")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPhotoComments(@PathVariable UUID photoId) {
        try {
            Photo photo = photoRepository.findById(photoId).orElse(null);
            if (photo == null) {
                return ResponseEntity.notFound().build();
            }
            
            List<PhotoComment> comments = photoCommentRepository.findByPhoto(photo);
            List<Map<String, Object>> commentDTOs = comments.stream().map(comment -> {
                Map<String, Object> commentDTO = new HashMap<>();
                commentDTO.put("id", comment.getId());
                commentDTO.put("comment", comment.getComment());
                commentDTO.put("createdAt", comment.getCreatedAt());
                return commentDTO;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "comments", commentDTOs
            ));
        } catch (Exception e) {
            System.err.println("Error getting comments: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Lỗi lấy bình luận: " + e.getMessage()));
        }
    }

    /**
     * Add comment to photo
     */
    @PostMapping("/api/photos/{photoId}/comment")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addComment(
            @PathVariable UUID photoId,
            @RequestBody Map<String, String> request) {
        
        try {
            Photo photo = photoRepository.findById(photoId).orElse(null);
            if (photo == null) {
                return ResponseEntity.notFound().build();
            }

            String content = request.get("content");
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Nội dung bình luận không được để trống"));
            }

            // Create and save comment to database
            PhotoComment photoComment = new PhotoComment();
            photoComment.setPhoto(photo);
            photoComment.setComment(content.trim());
            // For guest users, we can set guestToken or leave user as null
            // photoComment.setGuestToken("guest_" + System.currentTimeMillis());
            
            photoCommentRepository.save(photoComment);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã thêm bình luận"
            ));

        } catch (Exception e) {
            System.err.println("Error adding comment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Lỗi thêm bình luận: " + e.getMessage()));
        }
    }

    /**
     * Serve photos from Google Drive for public access
     * Exact same logic as AlbumController.getPhoto
     */
    @GetMapping("/api/photos/{driveFileId}")
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
     * Same logic as AlbumController
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
}
