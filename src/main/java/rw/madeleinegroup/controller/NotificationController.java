package rw.madeleinegroup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import rw.madeleinegroup.entity.Notification;
import rw.madeleinegroup.repository.UserRepository;
import rw.madeleinegroup.service.CustomUserDetailsService;
import rw.madeleinegroup.service.NotificationService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService,
                                  UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> list(@AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal,
                                  @RequestParam(defaultValue = "80") int limit,
                                  @RequestParam(defaultValue = "desc") String sort,
                                  @RequestParam(defaultValue = "all") String filter) {
        if (principal == null) return ResponseEntity.status(401).build();
        var user = userRepository.findById(principal.getId()).orElse(null);
        List<Notification> list = notificationService.getNotificationsForUser(user, limit, sort, filter);
        return ResponseEntity.ok(list.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> unreadCount(@AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        var user = userRepository.findById(principal.getId()).orElse(null);
        long count = notificationService.getUnreadCountForUser(user);
        return ResponseEntity.ok(java.util.Map.of("unreadCount", count));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id,
                                         @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        var user = userRepository.findById(principal.getId()).orElse(null);
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        var user = userRepository.findById(principal.getId()).orElse(null);
        if (user != null) notificationService.markAllAsReadForUser(user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id,
                                                   @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        var user = userRepository.findById(principal.getId()).orElse(null);
        notificationService.deleteForUser(id, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/read")
    public ResponseEntity<Void> deleteReadNotifications(
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        var user = userRepository.findById(principal.getId()).orElse(null);
        notificationService.deleteReadForUser(user);
        return ResponseEntity.noContent().build();
    }

    private Object toDto(Notification n) {
        return java.util.Map.of(
                "id", n.getId(),
                "title", n.getTitle() != null ? n.getTitle() : "",
                "message", n.getMessage() != null ? n.getMessage() : "",
                "type", n.getType().name(),
                "read", n.getRead(),
                "createdAt", n.getCreatedAt() != null ? n.getCreatedAt().toString() : ""
        );
    }
}
