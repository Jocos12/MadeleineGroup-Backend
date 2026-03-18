package rw.madeleinegroup.service;

import org.springframework.web.multipart.MultipartFile;
import rw.madeleinegroup.exception.FileStorageException;

public interface FileStorageService {

    /**
     * Store a file in the specified subfolder.
     * @param file The file to store
     * @param subfolder One of: 'profiles', 'experiences', 'receipts'
     * @return Relative URL of the stored file
     */
    String storeFile(MultipartFile file, String subfolder);

    void deleteFile(String fileUrl);

    String getFileUrl(String filename, String subfolder);
}
