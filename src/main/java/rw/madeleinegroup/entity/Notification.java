package rw.madeleinegroup.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    private String entityType;
    private Long entityId;

    @Column(name = "is_read", nullable = false)
    private Boolean read = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Must match MySQL {@code notifications.type} ENUM exactly (no DB migration from app code).
     */
    public enum NotificationType {
        BOOKING_CREATED,
        BOOKING_CONFIRMED,
        SYSTEM_ALERT,
        CLIENT_EXPERIENCE_SUBMITTED,
        CLIENT_EXPERIENCE_APPROVED,
        PAYMENT_RECORDED
    }

    public Notification() {
    }

    public Notification(Long id, User user, String title, String message, NotificationType type,
                        String entityType, Long entityId, Boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type;
        this.entityType = entityType;
        this.entityId = entityId;
        this.read = read != null ? read : false;
        this.createdAt = createdAt;
    }

    public static NotificationBuilder builder() {
        return new NotificationBuilder();
    }

    public static class NotificationBuilder {
        private Long id;
        private User user;
        private String title;
        private String message;
        private NotificationType type;
        private String entityType;
        private Long entityId;
        private Boolean read = false;
        private LocalDateTime createdAt;

        public NotificationBuilder id(Long id) { this.id = id; return this; }
        public NotificationBuilder user(User user) { this.user = user; return this; }
        public NotificationBuilder title(String title) { this.title = title; return this; }
        public NotificationBuilder message(String message) { this.message = message; return this; }
        public NotificationBuilder type(NotificationType type) { this.type = type; return this; }
        public NotificationBuilder entityType(String entityType) { this.entityType = entityType; return this; }
        public NotificationBuilder entityId(Long entityId) { this.entityId = entityId; return this; }
        public NotificationBuilder read(Boolean read) { this.read = read; return this; }
        public NotificationBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Notification build() {
            return new Notification(id, user, title, message, type, entityType, entityId, read, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
