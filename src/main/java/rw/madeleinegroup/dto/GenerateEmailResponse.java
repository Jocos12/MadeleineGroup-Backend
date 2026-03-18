package rw.madeleinegroup.dto;

/**
 * Generated email template with placeholders (CLIENT_NAME, BOOKING_REFERENCE, REMAINING_AMOUNT, EVENT_DATE).
 */
public class GenerateEmailResponse {
    private String subject;
    private String body;

    public GenerateEmailResponse() {}
    public GenerateEmailResponse(String subject, String body) {
        this.subject = subject;
        this.body = body;
    }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
