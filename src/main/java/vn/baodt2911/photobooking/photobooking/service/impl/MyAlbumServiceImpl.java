package vn.baodt2911.photobooking.photobooking.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.baodt2911.photobooking.photobooking.entity.MyAlbum;
import vn.baodt2911.photobooking.photobooking.repository.MyAlbumRepository;
import vn.baodt2911.photobooking.photobooking.service.interfaces.MyAlbumService;
import vn.baodt2911.photobooking.photobooking.service.CloudinaryService;

import java.util.List;
import java.util.UUID;

@Service
public class MyAlbumServiceImpl implements MyAlbumService {

    @Autowired
    private MyAlbumRepository myAlbumRepository;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public List<MyAlbum> findAll() {
        return myAlbumRepository.findAll();
    }

    @Override
    public MyAlbum findById(UUID id) {
        return myAlbumRepository.findById(id).orElse(null);
    }

    @Override
    public MyAlbum save(MyAlbum myAlbum) {
        return myAlbumRepository.save(myAlbum);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        MyAlbum myAlbum = findById(id);
        if (myAlbum != null) {
            // Get album name before deleting
            String albumName = myAlbum.getName();
            
            // Delete entire folder from Cloudinary (includes all photos)
            deleteAlbumFolderFromCloudinary(albumName);
            
            // Delete album from database
            myAlbumRepository.deleteById(id);
        }
    }
    
    /**
     * Delete entire album folder from Cloudinary
     */
    private void deleteAlbumFolderFromCloudinary(String albumName) {
        try {
            String sanitizedAlbumName = sanitizeFolderName(albumName);
            String folderPath = "my-albums/" + sanitizedAlbumName;
            
            // Delete entire folder (includes all photos)
            boolean deleted = cloudinaryService.deleteFolder(folderPath);
            if (deleted) {
                System.out.println("Deleted album folder from Cloudinary: " + folderPath);
            } else {
                System.out.println("Album folder not found or already deleted: " + folderPath);
            }
        } catch (Exception e) {
            // Don't log as error since folder might not exist
            System.out.println("Album folder not found or already deleted: " + albumName);
        }
    }
    
    /**
     * Sanitize album name for Cloudinary folder path
     * Remove special characters and replace spaces with underscores
     */
    private String sanitizeFolderName(String albumName) {
        if (albumName == null || albumName.trim().isEmpty()) {
            return "untitled";
        }
        
        return albumName.trim()
                .replaceAll("[^a-zA-Z0-9\\s\\-_]", "") // Remove special characters except spaces, hyphens, underscores
                .replaceAll("\\s+", "_") // Replace spaces with underscores
                .replaceAll("_{2,}", "_") // Replace multiple underscores with single underscore
                .toLowerCase(); // Convert to lowercase
    }
    
    @Override
    public Page<MyAlbum> findMyAlbumsWithFilters(String search, Boolean isPublic, UUID userId, Pageable pageable) {
        if (search != null && !search.trim().isEmpty() && isPublic != null && userId != null) {
            // Search with all filters
            return myAlbumRepository.findByNameContainingIgnoreCaseAndIsPublicAndUserId(search.trim(), isPublic, userId, pageable);
        } else if (search != null && !search.trim().isEmpty() && isPublic != null) {
            // Search with public filter
            return myAlbumRepository.findByNameContainingIgnoreCaseAndIsPublic(search.trim(), isPublic, pageable);
        } else if (search != null && !search.trim().isEmpty() && userId != null) {
            // Search with user filter
            return myAlbumRepository.findByNameContainingIgnoreCaseAndUserId(search.trim(), userId, pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            // Search only
            return myAlbumRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else if (isPublic != null && userId != null) {
            // Public and user filters
            return myAlbumRepository.findByIsPublicAndUserId(isPublic, userId, pageable);
        } else if (isPublic != null) {
            // Public filter only
            return myAlbumRepository.findByIsPublic(isPublic, pageable);
        } else if (userId != null) {
            // User filter only
            return myAlbumRepository.findByUserId(userId, pageable);
        } else {
            // No filters, return all
            return myAlbumRepository.findAll(pageable);
        }
    }
    
    @Override
    public long count() {
        return myAlbumRepository.count();
    }
    
    @Override
    public long countByIsPublic(boolean isPublic) {
        return myAlbumRepository.countByIsPublic(isPublic);
    }
    
    @Override
    public long countByUserId(UUID userId) {
        return myAlbumRepository.countByUserId(userId);
    }
    
    @Override
    public String getCoverPhotoUrl(MyAlbum myAlbum) {
        if (myAlbum == null) {
            return null;
        }
        
        if (myAlbum.getCoverPhotoId() != null) {
            return myAlbum.getMyPhotos().stream()
                    .filter(photo -> photo.getId().equals(myAlbum.getCoverPhotoId()))
                    .findFirst()
                    .map(photo -> photo.getUrl())
                    .orElse(null);
        }
        
        // If no cover photo selected, return first photo URL
        return myAlbum.getMyPhotos().isEmpty() ? null : myAlbum.getMyPhotos().iterator().next().getUrl();
    }
}
