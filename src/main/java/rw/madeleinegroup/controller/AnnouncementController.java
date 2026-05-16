package rw.madeleinegroup.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rw.madeleinegroup.common.ApiResponse;
import rw.madeleinegroup.dto.AnnouncementDto;
import rw.madeleinegroup.dto.AnnouncementRequest;
import rw.madeleinegroup.exception.FileStorageException;
import rw.madeleinegroup.repository.UserRepository;
import rw.madeleinegroup.service.AnnouncementService;
import rw.madeleinegroup.service.CustomUserDetailsService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final UserRepository userRepository;

    public AnnouncementController(AnnouncementService announcementService, UserRepository userRepository) {
        this.announcementService = announcementService;
        this.userRepository = userRepository;
    }

    /** Visible announcements for the current user's role. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AnnouncementDto>>> listForMe(
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        var user = userRepository.findById(principal.getId()).orElse(null);
        return ResponseEntity.ok(ApiResponse.success(announcementService.listVisibleFor(user), "OK"));
    }

    /** Full list including inactive — CEO management UI. */
    @GetMapping("/managed")
    @PreAuthorize("hasRole('CEO')")
    public ResponseEntity<ApiResponse<List<AnnouncementDto>>> listManaged() {
        return ResponseEntity.ok(ApiResponse.success(announcementService.listAllForManagement(), "OK"));
    }

    @PostMapping
    @PreAuthorize("hasRole('CEO')")
    public ResponseEntity<ApiResponse<AnnouncementDto>> create(
            @Valid @RequestBody AnnouncementRequest body,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            AnnouncementDto created = announcementService.create(body, principal.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(created, "Created"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CEO')")
    public ResponseEntity<ApiResponse<AnnouncementDto>> update(@PathVariable Long id,
                                                               @Valid @RequestBody AnnouncementRequest body) {
        try {
            return ResponseEntity.ok(ApiResponse.success(announcementService.update(id, body), "Updated"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CEO')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            announcementService.delete(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping(path = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CEO')")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(@PathVariable Long id,
                                                                      @RequestParam("file") MultipartFile file) {
        try {
            AnnouncementDto dto = announcementService.addImage(id, file);
            List<String> urls = dto.imageUrls();
            String url = urls.isEmpty() ? "" : urls.get(urls.size() - 1);
            return ResponseEntity.ok(ApiResponse.success(
                    Map.of("url", url, "downloadUrl", url, "imageUrl", url),
                    "Uploaded"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (FileStorageException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
