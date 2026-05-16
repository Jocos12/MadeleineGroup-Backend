package rw.madeleinegroup.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import rw.madeleinegroup.common.ApiResponse;
import rw.madeleinegroup.dto.DeleteRequestDto;
import rw.madeleinegroup.dto.DeleteRequestSubmitRequest;
import rw.madeleinegroup.dto.ReviewRequest;
import rw.madeleinegroup.entity.DeleteRequestStatus;
import rw.madeleinegroup.service.CustomUserDetailsService;
import rw.madeleinegroup.service.DeleteRequestService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delete-requests")
public class DeleteRequestController {

    private final DeleteRequestService deleteRequestService;

    public DeleteRequestController(DeleteRequestService deleteRequestService) {
        this.deleteRequestService = deleteRequestService;
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<DeleteRequestDto>> submit(
            @Valid @RequestBody DeleteRequestSubmitRequest body,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            DeleteRequestDto dto = deleteRequestService.submitRequest(principal.getId(), body.getReason());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto, "Created"));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/my-request")
    public ResponseEntity<ApiResponse<DeleteRequestDto>> getMyRequest(
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return deleteRequestService.getMyRequest(principal.getId())
                .map(d -> ResponseEntity.ok(ApiResponse.success(d, "OK")))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.success(null, "No request")));
    }

    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            deleteRequestService.cancel(id, principal.getId());
            return ResponseEntity.ok(ApiResponse.success(null, "Cancelled"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CEO','ADMIN')")
    public ResponseEntity<ApiResponse<List<DeleteRequestDto>>> getAll(
            @RequestParam(required = false) String status) {
        DeleteRequestStatus st = null;
        if (status != null && !status.isBlank()) {
            try {
                st = DeleteRequestStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid status"));
            }
        }
        return ResponseEntity.ok(ApiResponse.success(deleteRequestService.getAll(st), "OK"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CEO','ADMIN')")
    public ResponseEntity<ApiResponse<DeleteRequestDto>> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(deleteRequestService.getById(id), "OK"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('CEO','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> approve(
            @PathVariable Long id,
            @RequestBody(required = false) ReviewRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            String note = request != null ? request.getReviewNote() : null;
            deleteRequestService.approve(id, principal.getId(), note);
            return ResponseEntity.ok(ApiResponse.success(null, "Approved"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('CEO','ADMIN')")
    public ResponseEntity<ApiResponse<DeleteRequestDto>> reject(
            @PathVariable Long id,
            @RequestBody(required = false) ReviewRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            String note = request != null ? request.getReviewNote() : null;
            DeleteRequestDto dto = deleteRequestService.reject(id, principal.getId(), note);
            return ResponseEntity.ok(ApiResponse.success(dto, "Rejected"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CEO','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable Long id) {
        try {
            deleteRequestService.deleteRecord(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/counts")
    @PreAuthorize("hasAnyRole('CEO','ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getCounts() {
        return ResponseEntity.ok(ApiResponse.success(deleteRequestService.getCounts(), "OK"));
    }
}
