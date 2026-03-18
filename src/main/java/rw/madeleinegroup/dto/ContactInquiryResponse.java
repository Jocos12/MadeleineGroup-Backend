package rw.madeleinegroup.dto;

import java.time.LocalDateTime;

public class ContactInquiryResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private LocalDateTime createdAt;

    public ContactInquiryResponse() {}
    public ContactInquiryResponse(Long id, String name, String email, String phone, String subject, String message, LocalDateTime createdAt) {
        this.id = id; this.name = name; this.email = email; this.phone = phone; this.subject = subject; this.message = message; this.createdAt = createdAt;
    }
    public static ContactInquiryResponseBuilder builder() { return new ContactInquiryResponseBuilder(); }
    public static class ContactInquiryResponseBuilder {
        private Long id; private String name, email, phone, subject, message; private LocalDateTime createdAt;
        public ContactInquiryResponseBuilder id(Long v) { id = v; return this; } public ContactInquiryResponseBuilder name(String v) { name = v; return this; } public ContactInquiryResponseBuilder email(String v) { email = v; return this; } public ContactInquiryResponseBuilder phone(String v) { phone = v; return this; } public ContactInquiryResponseBuilder subject(String v) { subject = v; return this; } public ContactInquiryResponseBuilder message(String v) { message = v; return this; } public ContactInquiryResponseBuilder createdAt(LocalDateTime v) { createdAt = v; return this; }
        public ContactInquiryResponse build() { return new ContactInquiryResponse(id, name, email, phone, subject, message, createdAt); }
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; } public String getName() { return name; } public void setName(String v) { this.name = v; } public String getEmail() { return email; } public void setEmail(String v) { this.email = v; } public String getPhone() { return phone; } public void setPhone(String v) { this.phone = v; } public String getSubject() { return subject; } public void setSubject(String v) { this.subject = v; } public String getMessage() { return message; } public void setMessage(String v) { this.message = v; } public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
