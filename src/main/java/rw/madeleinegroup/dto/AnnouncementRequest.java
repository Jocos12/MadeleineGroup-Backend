package rw.madeleinegroup.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import rw.madeleinegroup.entity.AnnouncementAudience;

import java.util.List;

public class AnnouncementRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String body;

    @NotNull
    private AnnouncementAudience audience;

    private Boolean active = true;

    /** Optional image URLs. Null = do not change existing images on update. */
    private List<String> imageUrls;

    /**
     * When true, sends branded HTML email to users matching {@link #getAudience()}.
     * JSON alias {@code sendEmail} matches the dashboard UI.
     */
    @JsonAlias("sendEmail")
    private Boolean sendNotificationEmail;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public AnnouncementAudience getAudience() {
        return audience;
    }

    public void setAudience(AnnouncementAudience audience) {
        this.audience = audience;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Boolean getSendNotificationEmail() {
        return sendNotificationEmail;
    }

    public void setSendNotificationEmail(Boolean sendNotificationEmail) {
        this.sendNotificationEmail = sendNotificationEmail;
    }
}
