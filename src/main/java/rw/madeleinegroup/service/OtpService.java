package rw.madeleinegroup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.madeleinegroup.entity.OtpVerification;
import rw.madeleinegroup.entity.User;
import rw.madeleinegroup.repository.OtpVerificationRepository;
import rw.madeleinegroup.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpVerificationRepository otpRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.otp.enabled:true}")
    private boolean otpEnabled;

    @Value("${app.otp.log-to-console:true}")
    private boolean logToConsole;

    public OtpService(OtpVerificationRepository otpRepository, UserRepository userRepository, EmailService emailService) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /** Public method for AuthService to generate OTP code */
    public String generateOtp() {
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    @Transactional
    public String generateAndSendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String otp = generateOtp();
        OtpVerification otpVerification = OtpVerification.builder()
                .email(email)
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .used(false)
                .build();
        otpRepository.save(otpVerification);

        if (otpEnabled) {
            emailService.sendOtpEmail(email, otp);
        }
        if (logToConsole) {
            log.info("OTP for {} (copy this if email fails): {}", email, otp);
        } else {
            log.info("OTP generated for {}: {}", email, otpEnabled ? "sent via email" : otp);
        }

        return otpEnabled ? "OTP sent to your email" : otp;
    }

    public boolean verifyOtp(String email, String otpCode) {
        return otpRepository.findByEmailAndOtpAndUsedFalse(email, otpCode)
                .filter(otp -> otp.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(otp -> {
                    otp.setUsed(true);
                    otpRepository.save(otp);
                    return true;
                })
                .orElse(false);
    }
}
