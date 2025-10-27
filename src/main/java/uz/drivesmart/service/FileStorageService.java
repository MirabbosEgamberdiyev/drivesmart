package uz.drivesmart.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.drivesmart.exception.BusinessException;
import uz.drivesmart.exception.ResourceNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.upload.dir:uploads/questions}")
    private String uploadDir;

    @Value("${app.upload.max-size:5242880}") // 5MB
    private long maxFileSize;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/jpg", "image/webp"
    );

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    /**
     * Base64'dan file'ga convert qilish va saqlash
     */
    public String saveImageFromBase64(String base64Data, String prefix) {
        try {
            // Base64 validation
            if (base64Data == null || !base64Data.contains(",")) {
                throw new BusinessException("Noto'g'ri Base64 format");
            }

            // Data URL parse qilish: data:image/png;base64,iVBORw0KG...
            String[] parts = base64Data.split(",");
            String metadata = parts[0]; // data:image/png;base64
            String base64Image = parts[1];

            // Content type aniqlash
            String contentType = metadata.substring(
                    metadata.indexOf(":") + 1,
                    metadata.indexOf(";")
            );

            if (!ALLOWED_TYPES.contains(contentType)) {
                throw new BusinessException("Rasm formati qo'llab-quvvatlanmaydi: " + contentType);
            }

            // Decode
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            // Size validation
            if (imageBytes.length > maxFileSize) {
                throw new BusinessException(
                        String.format("Rasm hajmi %dMB dan oshmasligi kerak", maxFileSize / 1024 / 1024)
                );
            }

            // Unique filename
            String extension = contentType.split("/")[1];
            String filename = String.format(
                    "%s_%d_%s.%s",
                    prefix,
                    System.currentTimeMillis(),
                    UUID.randomUUID().toString().substring(0, 8),
                    extension
            );

            // Save to disk
            Path filePath = Paths.get(uploadDir, filename);
            Files.write(filePath, imageBytes);

            log.info("Image saved: {}, size: {} bytes", filename, imageBytes.length);

            return filename; // Faqat filename qaytaramiz

        } catch (IllegalArgumentException e) {
            throw new BusinessException("Base64 decode xatosi");
        } catch (IOException e) {
            throw new RuntimeException("Faylni saqlashda xatolik", e);
        }
    }

    /**
     * File'ni o'qish va Base64'ga convert qilish (API response uchun)
     */
    public String getImageAsBase64(String filename) {
        try {
            Path filePath = Paths.get(uploadDir, filename);
            if (!Files.exists(filePath)) {
                throw new ResourceNotFoundException("Rasm topilmadi: " + filename);
            }

            byte[] imageBytes = Files.readAllBytes(filePath);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Content type aniqlash
            String contentType = Files.probeContentType(filePath);

            return String.format("data:%s;base64,%s", contentType, base64Image);

        } catch (IOException e) {
            throw new RuntimeException("Rasmni o'qishda xatolik", e);
        }
    }

    /**
     * File'ni o'chirish
     */
    public void deleteImage(String filename) {
        try {
            Path filePath = Paths.get(uploadDir, filename);
            Files.deleteIfExists(filePath);
            log.info("Image deleted: {}", filename);
        } catch (IOException e) {
            log.error("Failed to delete image: {}", filename, e);
        }
    }
}