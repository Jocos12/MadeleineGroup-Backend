package rw.madeleinegroup.dto;

import jakarta.validation.constraints.*;

public class ClientExperienceRequest {
    @NotBlank(message = "Comment is required")
    @Size(max = 2000)
    private String comment;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String eventType;

    private String eventDate;

    private String authorName;

    @Email
    private String authorEmail;

    private String authorPhotoUrl;

    private String clientPhotoUrl; // alias for authorPhotoUrl

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }
    public String getAuthorPhotoUrl() { return authorPhotoUrl; }
    public void setAuthorPhotoUrl(String authorPhotoUrl) { this.authorPhotoUrl = authorPhotoUrl; }
    public String getClientPhotoUrl() { return clientPhotoUrl; }
    public void setClientPhotoUrl(String clientPhotoUrl) { this.clientPhotoUrl = clientPhotoUrl; }
}
