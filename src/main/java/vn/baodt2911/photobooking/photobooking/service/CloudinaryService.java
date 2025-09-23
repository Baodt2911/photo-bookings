package vn.baodt2911.photobooking.photobooking.service;

import io.github.cdimascio.dotenv.Dotenv;
import com.cloudinary.Cloudinary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private Cloudinary cloudinary;

    public CloudinaryService() {
        try {
            Dotenv dotenv = Dotenv.load();
            this.cloudinary = new Cloudinary(dotenv.get("CLOUDINARY_URL"));
        } catch (Exception e) {
            System.err.println("Error initializing Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Upload image to Cloudinary
     * @param image MultipartFile to upload
     * @param folder Folder name in Cloudinary (e.g., "photobooking/packages", "photobooking/albums")
     * @return Secure URL of uploaded image
     * @throws IOException if upload fails
     */
    public String saveImage(MultipartFile image, String folder) throws IOException {
        try {
            // Upload image to Cloudinary
            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("folder", folder);
            uploadParams.put("public_id", UUID.randomUUID().toString());
            uploadParams.put("overwrite", true);
            uploadParams.put("resource_type", "auto");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                image.getBytes(),
                uploadParams
            );
            
            // Return the secure URL from Cloudinary
            return (String) uploadResult.get("secure_url");
            
        } catch (Exception e) {
            System.err.println("Error uploading to Cloudinary: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Upload image to Cloudinary with default folder
     * @param image MultipartFile to upload
     * @return Secure URL of uploaded image
     * @throws IOException if upload fails
     */
    public String saveImage(MultipartFile image) throws IOException {
        return saveImage(image, "photobooking");
    }

    /**
     * Delete image from Cloudinary by URL
     * @param imageUrl Cloudinary URL of the image to delete
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteImage(String imageUrl) {
        try {
            // Extract public_id from Cloudinary URL
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId != null) {
                Map<String, Object> destroyParams = new HashMap<>();
                @SuppressWarnings("unchecked")
                Map<String, Object> result = cloudinary.uploader().destroy(publicId, destroyParams);
                return "ok".equals(result.get("result"));
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting image from Cloudinary: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete multiple images from Cloudinary
     * @param imageUrls Array of Cloudinary URLs to delete
     * @return Number of successfully deleted images
     */
    public int deleteImages(String[] imageUrls) {
        int deletedCount = 0;
        for (String imageUrl : imageUrls) {
            if (deleteImage(imageUrl)) {
                deletedCount++;
            }
        }
        return deletedCount;
    }

    /**
     * Delete all resources in a folder from Cloudinary
     * @param folderPath Folder path to delete (e.g., "my-albums/wedding_photos")
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteFolder(String folderPath) {
        try {
            // First, delete all resources in the folder
            Map<String, Object> deleteParams = new HashMap<>();
            deleteParams.put("prefix", folderPath + "/");
            deleteParams.put("invalidate", true);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> deleteResult = cloudinary.api().deleteResourcesByPrefix(folderPath + "/", deleteParams);
            
            System.out.println("Deleted all resources in folder: " + folderPath);
            
            // Then try to delete the empty folder
            try {
                Map<String, Object> destroyParams = new HashMap<>();
                destroyParams.put("folder", folderPath);
                destroyParams.put("invalidate", true);
                
                @SuppressWarnings("unchecked")
                Map<String, Object> result = cloudinary.api().deleteFolder(folderPath, destroyParams);
                
                System.out.println("Deleted empty folder from Cloudinary: " + folderPath);
            } catch (com.cloudinary.api.exceptions.NotFound e) {
                System.out.println("Folder not found or already deleted: " + folderPath);
            }
            
            return true;
        } catch (com.cloudinary.api.exceptions.NotFound e) {
            // Folder doesn't exist, this is not an error
            System.out.println("Folder not found (already deleted): " + folderPath);
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting folder from Cloudinary: " + folderPath + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Extract public_id from Cloudinary URL
     * @param url Cloudinary URL
     * @return public_id or null if extraction fails
     */
   private String extractPublicIdFromUrl(String url) {
    if (url == null || !url.contains("cloudinary.com")) {
        return null;
    }
    
    System.out.println("Debug: Extracting public_id from URL: " + url);
    
    try {
        // Cloudinary URL formats:
        // https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{public_id}.{format}
        // https://res.cloudinary.com/{cloud_name}/image/upload/{public_id}.{format}
        // https://res.cloudinary.com/{cloud_name}/image/upload/{folder}/{public_id}.{format}
        // https://res.cloudinary.com/{cloud_name}/image/upload/c_fill,w_100/{public_id}.{format}
        
        // Find the "/upload/" part first
        int uploadIndex = url.indexOf("/upload/");
        if (uploadIndex == -1) {
            System.err.println("Invalid Cloudinary URL: missing '/upload/' segment");
            return null;
        }
        
        // Get everything after "/upload/"
        String afterUpload = url.substring(uploadIndex + 8); // 8 = length of "/upload/"
        
        // Split by "/" to get segments
        String[] segments = afterUpload.split("/");
        if (segments.length == 0) {
            return null;
        }
        
        // Find the last segment (should contain filename with extension)
        String lastSegment = segments[segments.length - 1];
        
        // Remove file extension from last segment
        int dotIndex = lastSegment.lastIndexOf('.');
        if (dotIndex <= 0) {
            System.err.println("No file extension found in: " + lastSegment);
            return null;
        }
        
        String filename = lastSegment.substring(0, dotIndex);
        
        // Build public_id
        StringBuilder publicIdBuilder = new StringBuilder();
        
        // Add all segments except the last one (and skip version/transformation segments)
        for (int i = 0; i < segments.length - 1; i++) {
            String segment = segments[i];
            
            // Skip version segments (v1234567890)
            if (segment.matches("^v\\d+$")) {
                continue;
            }
            
            // Skip transformation segments (contain = or , characters)
            if (segment.contains("=") || segment.contains(",")) {
                continue;
            }
            
            // Add folder segments
            if (publicIdBuilder.length() > 0) {
                publicIdBuilder.append("/");
            }
            publicIdBuilder.append(segment);
        }
        
        // Add filename
        if (publicIdBuilder.length() > 0) {
            publicIdBuilder.append("/");
        }
        publicIdBuilder.append(filename);
        
        String publicId = publicIdBuilder.toString();
        System.out.println("Debug: Extracted public_id: " + publicId);
        
        return publicId.isEmpty() ? null : publicId;
        
    } catch (Exception e) {
        System.err.println("Error extracting public_id from URL: " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}

    /**
     * Check if URL is a valid Cloudinary URL
     * @param url URL to check
     * @return true if valid Cloudinary URL
     */
    public boolean isCloudinaryUrl(String url) {
        return url != null && url.contains("cloudinary.com");
    }

    /**
     * Get Cloudinary instance (for advanced usage)
     * @return Cloudinary instance
     */
    public Cloudinary getCloudinary() {
        return cloudinary;
    }

    /**
     * Debug method to test public_id extraction
     * @param url Cloudinary URL to test
     * @return extracted public_id
     */
    public String debugExtractPublicId(String url) {
        System.out.println("Debug: Extracting public_id from URL: " + url);
        String publicId = extractPublicIdFromUrl(url);
        System.out.println("Debug: Extracted public_id: " + publicId);
        return publicId;
    }
}
