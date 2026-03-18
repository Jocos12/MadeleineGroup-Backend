package rw.madeleinegroup.dto;

import jakarta.validation.constraints.*;

public class ContactRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    private String phone;

    @NotBlank(message = "Subject is required")
    private String subject; // wedding, corporate, quote, other

    @NotBlank(message = "Message is required")
    @Size(max = 2000)
    private String message;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
