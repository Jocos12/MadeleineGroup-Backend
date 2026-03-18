package rw.madeleinegroup.dto;

import java.time.LocalDateTime;

public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private String type;
    private Long relatedId;
    private String relatedType;
    private Long userId;
    private Boolean read;
    private LocalDateTime createdAt;

    public NotificationResponse() {}
    public NotificationResponse(Long id, String title, String message, String type, Long relatedId, String relatedType, Long userId, Boolean read, LocalDateTime createdAt) {
        this.id = id; this.title = title; this.message = message; this.type = type; this.relatedId = relatedId; this.relatedType = relatedType; this.userId = userId; this.read = read; this.createdAt = createdAt;
    }
    public static NotificationResponseBuilder builder() { return new NotificationResponseBuilder(); }
    public static class NotificationResponseBuilder {
        private Long id, relatedId, userId; private String title, message, type, relatedType; private Boolean read; private LocalDateTime createdAt;
        public NotificationResponseBuilder id(Long v) { id = v; return this; } public NotificationResponseBuilder title(String v) { title = v; return this; } public NotificationResponseBuilder message(String v) { message = v; return this; } public NotificationResponseBuilder type(String v) { type = v; return this; } public NotificationResponseBuilder relatedId(Long v) { relatedId = v; return this; } public NotificationResponseBuilder relatedType(String v) { relatedType = v; return this; } public NotificationResponseBuilder userId(Long v) { userId = v; return this; } public NotificationResponseBuilder read(Boolean v) { read = v; return this; } public NotificationResponseBuilder createdAt(LocalDateTime v) { createdAt = v; return this; }
        public NotificationResponse build() { return new NotificationResponse(id, title, message, type, relatedId, relatedType, userId, read, createdAt); }
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; } public String getTitle() { return title; } public void setTitle(String v) { this.title = v; } public String getMessage() { return message; } public void setMessage(String v) { this.message = v; } public String getType() { return type; } public void setType(String v) { this.type = v; } public Long getRelatedId() { return relatedId; } public void setRelatedId(Long v) { this.relatedId = v; } public String getRelatedType() { return relatedType; } public void setRelatedType(String v) { this.relatedType = v; } public Long getUserId() { return userId; } public void setUserId(Long v) { this.userId = v; } public Boolean getRead() { return read; } public void setRead(Boolean v) { this.read = v; } public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
