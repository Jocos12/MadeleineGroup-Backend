package rw.madeleinegroup.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "client_experiences")
public class ClientExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(name = "author_email", nullable = false)
    private String authorEmail;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private Integer rating;

    private String eventType;

    private String eventDate;

    @Column(name = "client_photo_url")
    private String clientPhotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    public ClientExperience() {
    }

    public static ClientExperienceBuilder builder() {
        return new ClientExperienceBuilder();
    }

    public static class ClientExperienceBuilder {
        private User user;
        private String authorName;
        private String authorEmail;
        private String comment;
        private Integer rating;
        private String eventType;
        private String eventDate;
        private String clientPhotoUrl;
        private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

        public ClientExperienceBuilder user(User u) { this.user = u; return this; }
        public ClientExperienceBuilder authorName(String n) { this.authorName = n; return this; }
        public ClientExperienceBuilder authorEmail(String e) { this.authorEmail = e; return this; }
        public ClientExperienceBuilder comment(String c) { this.comment = c; return this; }
        public ClientExperienceBuilder rating(Integer r) { this.rating = r; return this; }
        public ClientExperienceBuilder eventType(String t) { this.eventType = t; return this; }
        public ClientExperienceBuilder eventDate(String d) { this.eventDate = d; return this; }
        public ClientExperienceBuilder clientPhotoUrl(String u) { this.clientPhotoUrl = u; return this; }
        public ClientExperienceBuilder approvalStatus(ApprovalStatus s) { this.approvalStatus = s; return this; }

        public ClientExperience build() {
            ClientExperience e = new ClientExperience();
            e.setUser(user);
            e.setAuthorName(authorName);
            e.setAuthorEmail(authorEmail);
            e.setComment(comment);
            e.setRating(rating);
            e.setEventType(eventType);
            e.setEventDate(eventDate);
            e.setClientPhotoUrl(clientPhotoUrl);
            e.setApprovalStatus(approvalStatus != null ? approvalStatus : ApprovalStatus.PENDING);
            return e;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public String getClientPhotoUrl() { return clientPhotoUrl; }
    public void setClientPhotoUrl(String clientPhotoUrl) { this.clientPhotoUrl = clientPhotoUrl; }
    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
