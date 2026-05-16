package rw.madeleinegroup.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import rw.madeleinegroup.common.ApiResponse;
import rw.madeleinegroup.dto.ContactInquiryDto;
import rw.madeleinegroup.dto.ContactInquiryRequest;
import rw.madeleinegroup.dto.ReplyRequest;
import rw.madeleinegroup.entity.User;
import rw.madeleinegroup.repository.UserRepository;
import rw.madeleinegroup.service.ContactInquiryService;
import rw.madeleinegroup.service.CustomUserDetailsService;

import java.util.List;

@RestController
@RequestMapping("/api/contact-inquiries")
public class ContactInquiryController {

    private final ContactInquiryService contactInquiryService;
    private final UserRepository userRepository;

    public ContactInquiryController(ContactInquiryService contactInquiryService, UserRepository userRepository) {
        this.contactInquiryService = contactInquiryService;
        this.userRepository = userRepository;
    }

    @PostMapping("/public")
    public ResponseEntity<ApiResponse<ContactInquiryDto>> submitPublic(@Valid @RequestBody ContactInquiryRequest request) {
        try {
            ContactInquiryDto dto = contactInquiryService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto, "Created"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CEO','ADMIN')")
    public ResponseEntity<ApiResponse<List<ContactInquiryDto>>> getAll(
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false) Boolean replied) {
        return ResponseEntity.ok(ApiResponse.success(contactInquiryService.getAll(read, replied), "OK"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CEO','ADMIN')")
    public ResponseEntity<ApiResponse<ContactInquiryDto>> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(contactInquiryService.getById(id), "OK"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('CEO','ADMIN')")
    public ResponseEntity<ApiResponse<ContactInquiryDto>> markAsRead(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(contactInquiryService.markAsRead(id), "OK"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/unread")
    @PreAuthorize("hasAnyRole('CEO','ADMIN')")
    public ResponseEntity<ApiResponse<ContactInquiryDto>> markAsUnread(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(contactInquiryService.markAsUnread(id), "OK"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/mark-all-read")
    @PreAuthorize("hasAnyRole('CEO','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        contactInquiryService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success(null, "OK"));
    }

    @PostMapping("/{id}/reply")
    @PreAuthorize("hasAnyRole('CEO','ADMIN')")
    public ResponseEntity<ApiResponse<ContactInquiryDto>> reply(
            @PathVariable Long id,
            @Valid @RequestBody ReplyRequest body,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        try {
            String author = principal != null ? principal.getUsername() : "Madeleine Group";
            if (principal != null) {
                User u = userRepository.findById(principal.getId()).orElse(null);
                if (u != null && u.getFullName() != null && !u.getFullName().isBlank()) {
                    author = u.getFullName().trim();
                }
            }
            return ResponseEntity.ok(ApiResponse.success(
                    contactInquiryService.reply(id, body.getReplyMessage(), author), "OK"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CEO','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            contactInquiryService.delete(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/delete-read")
    @PreAuthorize("hasAnyRole('CEO','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAllRead() {
        contactInquiryService.deleteAllRead();
        return ResponseEntity.ok(ApiResponse.success(null, "OK"));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('CEO','ADMIN')")
    public ResponseEntity<ApiResponse<List<ContactInquiryDto>>> search(@RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.success(contactInquiryService.search(query), "OK"));
    }

    @GetMapping("/count-unread")
    @PreAuthorize("hasAnyRole('CEO','ADMIN')")
    public ResponseEntity<ApiResponse<Long>> countUnread() {
        return ResponseEntity.ok(ApiResponse.success(contactInquiryService.countUnread(), "OK"));
    }
}
