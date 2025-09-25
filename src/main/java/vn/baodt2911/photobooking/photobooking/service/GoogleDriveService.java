package vn.baodt2911.photobooking.photobooking.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import vn.baodt2911.photobooking.photobooking.service.interfaces.GoogleDriveServiceInterface;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
public class GoogleDriveService implements GoogleDriveServiceInterface {

    private static final String APPLICATION_NAME = "PhotoBooking";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    
    // Load API key from .env file
    private static final Dotenv dotenv = Dotenv.configure().load();
    private static final String GOOGLE_DRIVE_API_KEY = dotenv.get("GOOGLE_DRIVE_API_KEY");

    /**
     * Creates a Drive service instance without authentication (for public folders)
     */
    private Drive createDriveService() throws IOException, GeneralSecurityException {
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Check if a Google Drive folder is public by trying to access it
     */
    public boolean isFolderPublic(String folderId) {      
        try {
            Drive service = createDriveService();
            // Simple approach: try to get folder info
            File folder = service.files().get(folderId)
                    .setKey(GOOGLE_DRIVE_API_KEY)
                    .setFields("id,name")
                    .execute();

            
            // If we can get folder info, it's accessible
            if (folder != null) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all image files from a Google Drive folder
     */
    public List<Map<String, Object>> getImagesFromFolder(String folderId) {
        List<Map<String, Object>> images = new ArrayList<>();
        
        try {
            Drive service = createDriveService();

            // Query for image files in the folder
            String query = "'" + folderId + "' in parents and " +
                    "(mimeType='image/jpeg' or mimeType='image/png' or mimeType='image/gif' or " +
                    "mimeType='image/bmp' or mimeType='image/webp') and trashed=false";

            FileList result = service.files().list()
                    .setKey(GOOGLE_DRIVE_API_KEY)
                    .setQ(query)
                    .setFields("nextPageToken, files(id, name, mimeType, size, thumbnailLink, webViewLink)")
                    .execute();

            List<File> files = result.getFiles();
            if (files == null || files.isEmpty()) {
                System.out.println("No image files found in folder: " + folderId);
                return images;
            }

            for (File file : files) {
                Map<String, Object> imageData = new HashMap<>();
                imageData.put("id", file.getId());
                imageData.put("name", file.getName());
                imageData.put("mimeType", file.getMimeType());
                imageData.put("size", file.getSize() != null ? Long.parseLong(String.valueOf(file.getSize())) : 0);
                // Generate thumbnail URL using direct download format
                String thumbnailUrl = "https://drive.google.com/file/d/" + file.getId() + "/view";
                imageData.put("thumbnailUrl", thumbnailUrl);
                imageData.put("webViewLink", file.getWebViewLink());
                
                // Generate direct download URL
                String downloadUrl = "https://drive.google.com/uc?id=" + file.getId();
                imageData.put("downloadUrl", downloadUrl);
                
                images.add(imageData);
            }

        } catch (Exception e) {
            System.err.println("Error fetching images from Google Drive: " + e.getMessage());
        }

        return images;
    }

    /**
     * Extract folder ID from Google Drive URL
     */
    public String extractFolderIdFromUrl(String driveUrl) {
        try {
            if (driveUrl.contains("/folders/")) {
                String[] parts = driveUrl.split("/folders/");
                if (parts.length > 1) {
                    String folderPart = parts[1];
                    // Remove any query parameters
                    if (folderPart.contains("?")) {
                        folderPart = folderPart.split("\\?")[0];
                    }
                    return folderPart;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

}