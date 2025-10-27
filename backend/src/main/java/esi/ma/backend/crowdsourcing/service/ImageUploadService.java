package esi.ma.backend.crowdsourcing.service;
import esi.ma.backend.common.exception.InvalidRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class ImageUploadService {

    @Value("${upload.local.path:./uploads}")
    private String localUploadPath;

    /**
     * Upload report image to local filesystem
     */
    public String uploadReportImage(MultipartFile file, String submitterId) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadDir = Paths.get(localUploadPath, "reports");
        Files.createDirectories(uploadDir);

        // Validate file
        if (!isValidImageFile(file)) {
            throw new InvalidRequestException.InvalidFileException("Invalid image file");
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = submitterId + "_" + System.currentTimeMillis() + extension;

        // Save file
        Path filePath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative URL
        return "/uploads/reports/" + filename;
    }

    /**
     * Delete image
     */
    public void deleteImage(String imageUrl) {
        try {
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(localUploadPath, "reports", filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete image from local filesystem", e);
        }
    }

    private boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null &&
                (contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/webp"));
    }
}