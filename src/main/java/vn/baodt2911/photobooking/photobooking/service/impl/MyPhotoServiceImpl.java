package vn.baodt2911.photobooking.photobooking.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.baodt2911.photobooking.photobooking.entity.MyPhoto;
import vn.baodt2911.photobooking.photobooking.entity.MyAlbum;
import vn.baodt2911.photobooking.photobooking.entity.User;
import vn.baodt2911.photobooking.photobooking.repository.MyPhotoRepository;
import vn.baodt2911.photobooking.photobooking.service.interfaces.MyPhotoService;
import vn.baodt2911.photobooking.photobooking.service.interfaces.MyAlbumService;
import vn.baodt2911.photobooking.photobooking.service.interfaces.UserService;
import vn.baodt2911.photobooking.photobooking.service.CloudinaryService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MyPhotoServiceImpl implements MyPhotoService {

    @Autowired
    private MyPhotoRepository myPhotoRepository;
    
    @Autowired
    private MyAlbumService myAlbumService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public List<MyPhoto> findAll() {
        return myPhotoRepository.findAll();
    }

    @Override
    public MyPhoto findById(UUID id) {
        return myPhotoRepository.findById(id).orElse(null);
    }

    @Override
    public MyPhoto save(MyPhoto myPhoto) {
        return myPhotoRepository.save(myPhoto);
    }

    @Override
    public void deleteById(UUID id) {
        MyPhoto myPhoto = findById(id);
        if (myPhoto != null) {
            // Delete image from Cloudinary
            cloudinaryService.deleteImage(myPhoto.getUrl());
            myPhotoRepository.deleteById(id);
        }
    }

    @Override
    public List<MyPhoto> findByMyAlbumId(UUID myAlbumId) {
        return myPhotoRepository.findByMyAlbumId(myAlbumId);
    }

    @Override
    public void deleteByMyAlbumId(UUID myAlbumId) {
        List<MyPhoto> photos = findByMyAlbumId(myAlbumId);
        for (MyPhoto photo : photos) {
            // Delete images from Cloudinary
            cloudinaryService.deleteImage(photo.getUrl());
        }
        myPhotoRepository.deleteByMyAlbumId(myAlbumId);
    }

    @Override
    public List<MyPhoto> findByUserId(UUID userId) {
        return myPhotoRepository.findByUserId(userId);
    }

    @Override
    public MyPhoto uploadPhoto(UUID myAlbumId, UUID userId, MultipartFile file, String title, String description) {
        try {
            // Get album and user
            MyAlbum myAlbum = myAlbumService.findById(myAlbumId);
            User user = userService.findById(userId);
            
            if (myAlbum == null || user == null) {
                throw new RuntimeException("Album hoặc User không tồn tại");
            }
            
            // Upload image to Cloudinary with album name as folder
            String sanitizedAlbumName = sanitizeFolderName(myAlbum.getName());
            String folderPath = "my-albums/" + sanitizedAlbumName;
            String imageUrl = cloudinaryService.saveImage(file, folderPath);
            
            // Create MyPhoto entity
            MyPhoto myPhoto = new MyPhoto();
            myPhoto.setMyAlbum(myAlbum);
            myPhoto.setUser(user);
            myPhoto.setUrl(imageUrl);
            myPhoto.setTitle(title != null ? title : file.getOriginalFilename());
            myPhoto.setDescription(description);
            myPhoto.setOrderIndex(0);
            myPhoto.setIsFavorite(false);
            
            return save(myPhoto);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @Override
    public List<MyPhoto> uploadMultiplePhotos(UUID myAlbumId, UUID userId, MultipartFile[] files) {
        List<MyPhoto> uploadedPhotos = new ArrayList<>();
        
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            if (!file.isEmpty()) {
                try {
                    MyPhoto myPhoto = uploadPhoto(myAlbumId, userId, file, null, null);
                    myPhoto.setOrderIndex(i); // Set order index
                    uploadedPhotos.add(save(myPhoto));
                } catch (Exception e) {
                    System.err.println("Lỗi upload file " + file.getOriginalFilename() + ": " + e.getMessage());
                }
            }
        }
        
        return uploadedPhotos;
    }

    @Override
    public long count() {
        return myPhotoRepository.count();
    }

    @Override
    public long countByMyAlbumId(UUID myAlbumId) {
        return myPhotoRepository.findByMyAlbumId(myAlbumId).size();
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
    public long countByUserId(UUID userId) {
        return myPhotoRepository.findByUserId(userId).size();
    }

    @Override
    public long countByIsFavorite(boolean isFavorite) {
        return myPhotoRepository.findByIsFavorite(isFavorite).size();
    }
}
