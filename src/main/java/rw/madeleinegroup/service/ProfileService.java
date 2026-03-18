package rw.madeleinegroup.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rw.madeleinegroup.dto.PasswordChangeRequest;
import rw.madeleinegroup.dto.ProfileUpdateRequest;
import rw.madeleinegroup.entity.User;
import rw.madeleinegroup.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ProfileService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_SIZE = 2 * 1024 * 1024; // 2MB

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload.dir:uploads/profile-photos}")
    private String uploadDir;

    @Value("${app.api-base-url:http://localhost:8082}")
    private String apiBaseUrl;

    public ProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Map<String, Object> updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().isBlank() ? null : request.getPhone().trim());
        }
        if (request.getProfilePhotoUrl() != null) {
            user.setProfilePhotoUrl(request.getProfilePhotoUrl().isBlank() ? null : request.getProfilePhotoUrl().trim());
        }
        user = userRepository.save(user);
        return toMeResponse(user);
    }

    @Transactional
    public Map<String, String> changePassword(Long userId, PasswordChangeRequest request) {
        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            throw new IllegalArgumentException("Current password is required");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return Map.of("message", "Password updated successfully");
    }

    public Map<String, String> uploadPhoto(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file uploaded");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPG, PNG, and WEBP files are accepted");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File is too large. Maximum size is 2MB");
        }
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        String ext = getExtension(contentType);
        String filename = UUID.randomUUID() + ext;
        Path target = dir.resolve(filename);
        Files.copy(file.getInputStream(), target);
        String photoUrl = apiBaseUrl + "/uploads/profile-photos/" + filename;
        return Map.of("photoUrl", photoUrl);
    }

    private String getExtension(String contentType) {
        if ("image/jpeg".equals(contentType)) return ".jpg";
        if ("image/png".equals(contentType)) return ".png";
        if ("image/webp".equals(contentType)) return ".webp";
        return ".jpg";
    }

    private Map<String, Object> toMeResponse(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("userId", user.getId());
        map.put("email", user.getEmail());
        map.put("fullName", user.getFullName());
        map.put("phone", user.getPhone());
        map.put("role", user.getRole() != null ? user.getRole().name() : null);
        map.put("roles", java.util.Set.of(user.getRole() != null ? user.getRole().name() : "CLIENT"));
        map.put("profilePhotoUrl", user.getProfilePhotoUrl());
        map.put("branchId", user.getBranch() != null ? user.getBranch().getId() : null);
        map.put("branchName", user.getBranch() != null ? user.getBranch().getName() : null);
        return map;
    }
}
