package rw.madeleinegroup.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "contact_inquiries")
public class ContactInquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "is_read")
    private Boolean isRead = false;

    public ContactInquiry() {
    }

    public ContactInquiry(Long id, String name, String email, String phone, String subject,
                          String message, LocalDateTime createdAt, Boolean isRead) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.subject = subject;
        this.message = message;
        this.createdAt = createdAt;
        this.isRead = isRead != null ? isRead : false;
    }

    public static ContactInquiryBuilder builder() {
        return new ContactInquiryBuilder();
    }

    public static class ContactInquiryBuilder {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String subject;
        private String message;
        private LocalDateTime createdAt;
        private Boolean isRead = false;

        public ContactInquiryBuilder id(Long id) { this.id = id; return this; }
        public ContactInquiryBuilder name(String name) { this.name = name; return this; }
        public ContactInquiryBuilder email(String email) { this.email = email; return this; }
        public ContactInquiryBuilder phone(String phone) { this.phone = phone; return this; }
        public ContactInquiryBuilder subject(String subject) { this.subject = subject; return this; }
        public ContactInquiryBuilder message(String message) { this.message = message; return this; }
        public ContactInquiryBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ContactInquiryBuilder isRead(Boolean isRead) { this.isRead = isRead; return this; }

        public ContactInquiry build() {
            return new ContactInquiry(id, name, email, phone, subject, message, createdAt, isRead);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
}
