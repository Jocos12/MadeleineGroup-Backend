package rw.madeleinegroup.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OtpVerifyRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
