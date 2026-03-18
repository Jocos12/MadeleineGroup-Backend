package rw.madeleinegroup.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import rw.madeleinegroup.dto.AuthResponse;
import rw.madeleinegroup.dto.LoginRequest;
import rw.madeleinegroup.dto.RegisterRequest;
import rw.madeleinegroup.dto.OtpVerifyRequest;
import rw.madeleinegroup.service.CustomUserDetailsService;
import rw.madeleinegroup.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        authService.registerClient(request);
        return ResponseEntity.ok(java.util.Map.of("message", "Account created. Check your email for verification code."));
    }

    @PostMapping("/request-otp")
    public ResponseEntity<Void> requestOtp(@Valid @RequestBody LoginRequest request) {
        authService.requestOtp(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyOtpAndLogin(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "Email is required"));
        }
        authService.requestForgotPasswordOtp(email);
        return ResponseEntity.ok(java.util.Map.of("message", "Verification code sent to your email. Check your inbox."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");
        String newPassword = body.get("newPassword");
        if (email == null || otp == null || newPassword == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "Email, OTP and new password are required"));
        }
        authService.resetPasswordWithOtp(email, otp, newPassword);
        return ResponseEntity.ok(java.util.Map.of("message", "Password reset successfully. You can now sign in."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(authService.getCurrentUser(principal.getId()));
    }
}
