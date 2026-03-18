package rw.madeleinegroup.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rw.madeleinegroup.exception.FileStorageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5 MB

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public String storeFile(MultipartFile file, String subfolder) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File is empty");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new FileStorageException("Invalid file type. Allowed: JPEG, PNG, WebP");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new FileStorageException("File too large. Max 5 MB");
        }

        try {
            Path root = Paths.get(uploadDir, subfolder);
            Files.createDirectories(root);

            String ext = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + ext;
            Path target = root.resolve(filename);

            Files.copy(file.getInputStream(), target);

            return "/" + subfolder + "/" + filename;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;
        try {
            String path = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path target = Paths.get(uploadDir, path);
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String filename, String subfolder) {
        return "/" + subfolder + "/" + filename;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }
}
