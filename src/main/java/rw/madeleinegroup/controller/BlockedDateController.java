package rw.madeleinegroup.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import rw.madeleinegroup.dto.BlockedDateRequest;
import rw.madeleinegroup.dto.BlockedDateResponse;
import rw.madeleinegroup.service.BlockedDateService;
import rw.madeleinegroup.service.CustomUserDetailsService;

import java.util.List;

@RestController
@RequestMapping("/api/blocked-dates")
public class BlockedDateController {

    private final BlockedDateService blockedDateService;

    public BlockedDateController(BlockedDateService blockedDateService) {
        this.blockedDateService = blockedDateService;
    }

    @GetMapping
    public ResponseEntity<List<BlockedDateResponse>> getAllBlockedDates() {
        return ResponseEntity.ok(blockedDateService.getAllBlockedDates());
    }

    @PostMapping
    public ResponseEntity<BlockedDateResponse> addBlockedDate(
            @Valid @RequestBody BlockedDateRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        Long adminId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(blockedDateService.addBlockedDate(request, adminId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeBlockedDate(@PathVariable Long id) {
        blockedDateService.removeBlockedDate(id);
        return ResponseEntity.noContent().build();
    }
}
