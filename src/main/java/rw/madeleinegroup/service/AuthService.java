package rw.madeleinegroup.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rw.madeleinegroup.dto.AuthResponse;
import rw.madeleinegroup.dto.LoginRequest;
import rw.madeleinegroup.dto.OtpVerifyRequest;
import rw.madeleinegroup.dto.RegisterRequest;
import rw.madeleinegroup.entity.LoginAudit;
import rw.madeleinegroup.entity.OtpVerification;
import rw.madeleinegroup.entity.Role;
import rw.madeleinegroup.entity.User;
import rw.madeleinegroup.repository.LoginAuditRepository;
import rw.madeleinegroup.repository.OtpVerificationRepository;
import rw.madeleinegroup.repository.UserRepository;
import rw.madeleinegroup.security.JwtUtil;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OtpVerificationRepository otpRepository;
    private final LoginAuditRepository loginAuditRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, OtpVerificationRepository otpRepository,
                       LoginAuditRepository loginAuditRepository, OtpService otpService,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.loginAuditRepository = loginAuditRepository;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public void registerClient(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(Role.CLIENT);
        user.setEnabled(true);
        user.setEmailVerified(false);
        userRepository.save(user);
        otpService.generateAndSendOtp(request.getEmail());
    }

    public void requestOtp(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with email: " + request.getEmail());
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }
        otpService.generateAndSendOtp(request.getEmail());
    }

    public AuthResponse verifyOtpAndLogin(OtpVerifyRequest request) {
        OtpVerification otpRecord = otpRepository.findByEmailAndOtpAndUsedFalse(request.getEmail(), request.getOtp())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OTP"));
        if (otpRecord.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }
        otpRecord.setUsed(true);
        otpRepository.save(otpRecord);
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        LoginAudit audit = new LoginAudit();
        audit.setUser(user);
        audit.setEmail(user.getEmail());
        audit.setFullName(user.getFullName());
        audit.setRole(user.getRole() != null ? user.getRole().name() : null);
        loginAuditRepository.save(audit);
        String token = jwtUtil.generateToken(user.getEmail());
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .roles(java.util.Set.of(user.getRole().name()))
                .otpRequired(false)
                .build();
    }

    /** Forgot password: send OTP to email (no password required) */
    public void requestForgotPasswordOtp(String email) {
        if (userRepository.findByEmail(email).isEmpty()) {
            throw new IllegalArgumentException("User not found with email: " + email);
        }
        otpService.generateAndSendOtp(email);
    }

    /** Reset password using OTP verification */
    public void resetPasswordWithOtp(String email, String otp, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        var otpRecord = otpRepository.findByEmailAndOtpAndUsedFalse(email, otp)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OTP"));
        if (otpRecord.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }
        otpRecord.setUsed(true);
        otpRepository.save(otpRecord);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public java.util.Map<String, Object> getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", user.getId());
        map.put("userId", user.getId());
        map.put("email", user.getEmail());
        map.put("fullName", user.getFullName());
        map.put("phone", user.getPhone());
        map.put("profilePhotoUrl", user.getProfilePhotoUrl());
        map.put("role", user.getRole() != null ? user.getRole().name() : null);
        map.put("roles", java.util.Set.of(user.getRole() != null ? user.getRole().name() : "CLIENT"));
        map.put("branchId", user.getBranch() != null ? user.getBranch().getId() : null);
        map.put("branchName", user.getBranch() != null ? user.getBranch().getName() : null);
        return map;
    }
}
