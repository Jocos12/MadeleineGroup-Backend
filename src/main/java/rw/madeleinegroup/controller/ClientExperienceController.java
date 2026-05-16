package rw.madeleinegroup.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import rw.madeleinegroup.dto.ClientExperienceRequest;
import rw.madeleinegroup.dto.ClientExperienceResponse;
import rw.madeleinegroup.dto.RejectRequest;
import rw.madeleinegroup.entity.ClientExperience;
import rw.madeleinegroup.service.ClientExperienceService;
import rw.madeleinegroup.service.CustomUserDetailsService;

import java.util.List;

@RestController
@RequestMapping("/api/client-experiences")
public class ClientExperienceController {

    private final ClientExperienceService clientExperienceService;

    public ClientExperienceController(ClientExperienceService clientExperienceService) {
        this.clientExperienceService = clientExperienceService;
    }

    @PostMapping
    public ResponseEntity<?> submitExperience(@Valid @RequestBody ClientExperienceRequest request,
                                             @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        ClientExperience exp = clientExperienceService.submitExperience(request, principal != null ? principal.getId() : null);
        return ResponseEntity.ok(exp.getId());
    }

    @GetMapping("/public")
    public ResponseEntity<List<ClientExperienceResponse>> getApprovedExperiences() {
        return ResponseEntity.ok(clientExperienceService.getApprovedExperiences());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('CEO', 'ADMIN')")
    public ResponseEntity<List<ClientExperienceResponse>> getAllExperiences() {
        return ResponseEntity.ok(clientExperienceService.getAllExperiences());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('CEO', 'ADMIN')")
    public ResponseEntity<List<ClientExperienceResponse>> getPendingForApproval() {
        return ResponseEntity.ok(clientExperienceService.getPendingExperiences());
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('CEO', 'ADMIN')")
    public ResponseEntity<?> approve(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        clientExperienceService.approveExperience(id, principal.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('CEO', 'ADMIN')")
    public ResponseEntity<?> reject(@PathVariable Long id,
                                   @RequestBody(required = false) RejectRequest body,
                                   @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        String reason = body != null ? body.getRejectionReason() : null;
        clientExperienceService.rejectWithReason(id, principal.getEmail(), reason);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CEO', 'ADMIN')")
    public ResponseEntity<ClientExperienceResponse> update(@PathVariable Long id,
                                                           @Valid @RequestBody ClientExperienceRequest request) {
        return ResponseEntity.ok(clientExperienceService.updateExperience(id, request));
    }
}
