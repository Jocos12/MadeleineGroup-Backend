package rw.madeleinegroup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.madeleinegroup.dto.UserResponse;
import rw.madeleinegroup.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('CEO', 'ADMIN')")
    public ResponseEntity<UserResponse> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleStatus(id));
    }
}
