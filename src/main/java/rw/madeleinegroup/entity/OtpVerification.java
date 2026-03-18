package rw.madeleinegroup.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verifications")
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 6)
    private String otp;

    @Column(nullable = false)
    private Boolean used = false;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public OtpVerification() {
    }

    public OtpVerification(Long id, String email, String otp, Boolean used, LocalDateTime expiresAt, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.otp = otp;
        this.used = used != null ? used : false;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public static OtpVerificationBuilder builder() {
        return new OtpVerificationBuilder();
    }

    public static class OtpVerificationBuilder {
        private Long id;
        private String email;
        private String otp;
        private Boolean used = false;
        private LocalDateTime expiresAt;
        private LocalDateTime createdAt;

        public OtpVerificationBuilder id(Long id) { this.id = id; return this; }
        public OtpVerificationBuilder email(String email) { this.email = email; return this; }
        public OtpVerificationBuilder otp(String otp) { this.otp = otp; return this; }
        public OtpVerificationBuilder used(Boolean used) { this.used = used; return this; }
        public OtpVerificationBuilder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public OtpVerificationBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public OtpVerification build() {
            return new OtpVerification(id, email, otp, used, expiresAt, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public Boolean getUsed() { return used; }
    public void setUsed(Boolean used) { this.used = used; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
