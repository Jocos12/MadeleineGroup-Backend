package rw.madeleinegroup.dto;

import rw.madeleinegroup.entity.ClientExperience;
import rw.madeleinegroup.entity.User;

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
    private String rejectionReason;

    public ClientExperienceResponse() {
    }

    public static ClientExperienceResponse fromEntity(ClientExperience e) {
        if (e == null) {
            return null;
        }
        Long uid = null;
        User u = e.getUser();
        if (u != null) {
            uid = u.getId();
        }
        return ClientExperienceResponse.builder()
                .id(e.getId())
                .userId(uid)
                .authorName(e.getAuthorName())
                .authorEmail(e.getAuthorEmail())
                .comment(e.getComment())
                .rating(e.getRating())
                .eventType(e.getEventType())
                .eventDate(e.getEventDate())
                .clientPhotoUrl(e.getClientPhotoUrl())
                .approvalStatus(e.getApprovalStatus())
                .createdAt(e.getCreatedAt())
                .approvedAt(e.getApprovedAt())
                .rejectionReason(e.getRejectionReason())
                .build();
    }

    public static ClientExperienceResponseBuilder builder() {
        return new ClientExperienceResponseBuilder();
    }

    public static class ClientExperienceResponseBuilder {
        private Long id;
        private Long userId;
        private String authorName;
        private String authorEmail;
        private String comment;
        private String eventType;
        private String eventDate;
        private String clientPhotoUrl;
        private Integer rating;
        private ClientExperience.ApprovalStatus approvalStatus;
        private LocalDateTime createdAt;
        private LocalDateTime approvedAt;
        private String rejectionReason;

        public ClientExperienceResponseBuilder id(Long v) {
            id = v;
            return this;
        }

        public ClientExperienceResponseBuilder userId(Long v) {
            userId = v;
            return this;
        }

        public ClientExperienceResponseBuilder authorName(String v) {
            authorName = v;
            return this;
        }

        public ClientExperienceResponseBuilder authorEmail(String v) {
            authorEmail = v;
            return this;
        }

        public ClientExperienceResponseBuilder comment(String v) {
            comment = v;
            return this;
        }

        public ClientExperienceResponseBuilder rating(Integer v) {
            rating = v;
            return this;
        }

        public ClientExperienceResponseBuilder eventType(String v) {
            eventType = v;
            return this;
        }

        public ClientExperienceResponseBuilder eventDate(String v) {
            eventDate = v;
            return this;
        }

        public ClientExperienceResponseBuilder clientPhotoUrl(String v) {
            clientPhotoUrl = v;
            return this;
        }

        public ClientExperienceResponseBuilder approvalStatus(ClientExperience.ApprovalStatus v) {
            approvalStatus = v;
            return this;
        }

        public ClientExperienceResponseBuilder createdAt(LocalDateTime v) {
            createdAt = v;
            return this;
        }

        public ClientExperienceResponseBuilder approvedAt(LocalDateTime v) {
            approvedAt = v;
            return this;
        }

        public ClientExperienceResponseBuilder rejectionReason(String v) {
            rejectionReason = v;
            return this;
        }

        public ClientExperienceResponse build() {
            ClientExperienceResponse r = new ClientExperienceResponse();
            r.id = id;
            r.userId = userId;
            r.authorName = authorName;
            r.authorEmail = authorEmail;
            r.comment = comment;
            r.rating = rating;
            r.eventType = eventType;
            r.eventDate = eventDate;
            r.clientPhotoUrl = clientPhotoUrl;
            r.approvalStatus = approvalStatus;
            r.createdAt = createdAt;
            r.approvedAt = approvedAt;
            r.rejectionReason = rejectionReason;
            return r;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getClientPhotoUrl() {
        return clientPhotoUrl;
    }

    public void setClientPhotoUrl(String clientPhotoUrl) {
        this.clientPhotoUrl = clientPhotoUrl;
    }

    public ClientExperience.ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(ClientExperience.ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
