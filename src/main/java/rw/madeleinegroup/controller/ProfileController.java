package rw.madeleinegroup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rw.madeleinegroup.dto.PasswordChangeRequest;
import rw.madeleinegroup.dto.ProfileUpdateRequest;
import rw.madeleinegroup.service.CustomUserDetailsService;
import rw.madeleinegroup.service.ProfileService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping("/photo")
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("photo") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) throws IOException {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(profileService.uploadPhoto(principal.getId(), file));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody ProfileUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        Map<String, Object> updated = profileService.updateProfile(principal.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @RequestBody PasswordChangeRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(profileService.changePassword(principal.getId(), request));
    }
}
