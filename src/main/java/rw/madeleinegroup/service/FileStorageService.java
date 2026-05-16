package rw.madeleinegroup.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeFile(MultipartFile file, String subfolder);

    void deleteFile(String fileUrl);

    String getFileUrl(String filename, String subfolder);
}
