package vn.baodt2911.photobooking.photobooking.service.interfaces;

import java.util.List;
import java.util.Map;

public interface GoogleDriveServiceInterface {
    
    /**
     * Check if a Google Drive folder is public
     * @param folderId The Google Drive folder ID
     * @return true if folder is public, false otherwise
     */
    boolean isFolderPublic(String folderId);
    
    /**
     * Get all image files from a Google Drive folder
     * @param folderId The Google Drive folder ID
     * @return List of image file information
     */
    List<Map<String, Object>> getImagesFromFolder(String folderId);
    
    /**
     * Extract folder ID from Google Drive URL
     * @param driveUrl The Google Drive URL
     * @return The folder ID or null if not found
     */
    String extractFolderIdFromUrl(String driveUrl);

}