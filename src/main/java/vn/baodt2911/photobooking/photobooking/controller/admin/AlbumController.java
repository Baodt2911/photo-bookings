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
import vn.baodt2911.photobooking.photobooking.entity.Album;
import vn.baodt2911.photobooking.photobooking.entity.User;
import vn.baodt2911.photobooking.photobooking.service.interfaces.AlbumService;
import vn.baodt2911.photobooking.photobooking.service.interfaces.UserService;
import vn.baodt2911.photobooking.photobooking.dto.request.AlbumCreateRequestDTO;
import vn.baodt2911.photobooking.photobooking.dto.request.AlbumUpdateRequestDTO;
import vn.baodt2911.photobooking.photobooking.dto.response.AlbumResponseDTO;
import vn.baodt2911.photobooking.photobooking.mapper.EntityMapper;

import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AlbumController {

    @Autowired
    private AlbumService albumService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EntityMapper entityMapper;

    @GetMapping("/albums")
    public String albums(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean allowDownload,
            @RequestParam(required = false) Boolean allowComment,
            @RequestParam(required = false) UUID createdById,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        // Create Pageable object with sorting
        Pageable pageable = createPageable(page, size, sort);
        
        // Get albums with search, filter and pagination
        Page<Album> albums = albumService.findAlbumsWithFilters(search, allowDownload, allowComment, createdById, pageable);
        
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
    public ResponseEntity<?> createAlbum(
            @ModelAttribute AlbumCreateRequestDTO requestDTO,
            @RequestParam(value = "createdById", required = false) UUID createdById) {
        try {
            Album albumEntity = entityMapper.toAlbumEntity(requestDTO);
            
            // Set creator if provided
            if (createdById != null) {
                User user = userService.findByEmail("admin@example.com"); // Default user for now
                if (user != null) {
                    albumEntity.setCreatedBy(user);
                }
            }
            
            Album savedAlbum = albumService.save(albumEntity);
            AlbumResponseDTO albumDTO = entityMapper.toAlbumResponseDTO(savedAlbum);
            return ResponseEntity.ok(albumDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi tạo album: " + e.getMessage());
        }
    }

    @GetMapping("/albums/{id}")
    @ResponseBody
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
            return ResponseEntity.badRequest().body("Lỗi khi lấy thông tin album: " + e.getMessage());
        }
    }

    @PutMapping("/albums/{id}")
    @ResponseBody   
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
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật album: " + e.getMessage());
        }
    }

    @DeleteMapping("/albums/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteAlbum(@PathVariable UUID id) {
        try {
            albumService.deleteById(id);
            return ResponseEntity.ok("Xóa album thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xóa album: " + e.getMessage());
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
}