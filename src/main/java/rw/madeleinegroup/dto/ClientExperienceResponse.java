package rw.madeleinegroup.dto;

import rw.madeleinegroup.entity.ClientExperience;

import java.time.LocalDateTime;

public class ClientExperienceResponse {
    private Long id;
    private Long userId;
    private String authorName;
    private String authorEmail;
    private String comment;
    private Integer rating;
    private String eventType;
    private String eventDate;
    private String clientPhotoUrl;
    private ClientExperience.ApprovalStatus approvalStatus;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;

    public ClientExperienceResponse() {}
    public ClientExperienceResponse(Long id, Long userId, String authorName, String authorEmail, String comment, Integer rating, String eventType, String eventDate, String clientPhotoUrl, ClientExperience.ApprovalStatus approvalStatus, LocalDateTime createdAt, LocalDateTime approvedAt) {
        this.id = id; this.userId = userId; this.authorName = authorName; this.authorEmail = authorEmail; this.comment = comment; this.rating = rating; this.eventType = eventType; this.eventDate = eventDate; this.clientPhotoUrl = clientPhotoUrl; this.approvalStatus = approvalStatus; this.createdAt = createdAt; this.approvedAt = approvedAt;
    }
    public static ClientExperienceResponseBuilder builder() { return new ClientExperienceResponseBuilder(); }
    public static class ClientExperienceResponseBuilder {
        private Long id, userId; private String authorName, authorEmail, comment, eventType, eventDate, clientPhotoUrl; private Integer rating; private ClientExperience.ApprovalStatus approvalStatus; private LocalDateTime createdAt, approvedAt;
        public ClientExperienceResponseBuilder id(Long v) { id = v; return this; } public ClientExperienceResponseBuilder userId(Long v) { userId = v; return this; } public ClientExperienceResponseBuilder authorName(String v) { authorName = v; return this; } public ClientExperienceResponseBuilder authorEmail(String v) { authorEmail = v; return this; } public ClientExperienceResponseBuilder comment(String v) { comment = v; return this; } public ClientExperienceResponseBuilder rating(Integer v) { rating = v; return this; } public ClientExperienceResponseBuilder eventType(String v) { eventType = v; return this; } public ClientExperienceResponseBuilder eventDate(String v) { eventDate = v; return this; } public ClientExperienceResponseBuilder clientPhotoUrl(String v) { clientPhotoUrl = v; return this; } public ClientExperienceResponseBuilder approvalStatus(ClientExperience.ApprovalStatus v) { approvalStatus = v; return this; } public ClientExperienceResponseBuilder createdAt(LocalDateTime v) { createdAt = v; return this; } public ClientExperienceResponseBuilder approvedAt(LocalDateTime v) { approvedAt = v; return this; }
        public ClientExperienceResponse build() { return new ClientExperienceResponse(id, userId, authorName, authorEmail, comment, rating, eventType, eventDate, clientPhotoUrl, approvalStatus, createdAt, approvedAt); }
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; } public Long getUserId() { return userId; } public void setUserId(Long v) { this.userId = v; } public String getAuthorName() { return authorName; } public void setAuthorName(String v) { this.authorName = v; } public String getAuthorEmail() { return authorEmail; } public void setAuthorEmail(String v) { this.authorEmail = v; } public String getComment() { return comment; } public void setComment(String v) { this.comment = v; } public Integer getRating() { return rating; } public void setRating(Integer v) { this.rating = v; } public String getEventType() { return eventType; } public void setEventType(String v) { this.eventType = v; } public String getEventDate() { return eventDate; } public void setEventDate(String v) { this.eventDate = v; } public String getClientPhotoUrl() { return clientPhotoUrl; } public void setClientPhotoUrl(String v) { this.clientPhotoUrl = v; } public ClientExperience.ApprovalStatus getApprovalStatus() { return approvalStatus; } public void setApprovalStatus(ClientExperience.ApprovalStatus v) { this.approvalStatus = v; } public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { this.createdAt = v; } public LocalDateTime getApprovedAt() { return approvedAt; } public void setApprovedAt(LocalDateTime v) { this.approvedAt = v; }
}
