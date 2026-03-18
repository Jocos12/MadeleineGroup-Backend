package rw.madeleinegroup.dto;

import java.util.List;

/**
 * Request to send reminder emails to selected clients.
 */
public class SendRemindersRequest {
    private List<Long> clientIds;
    private String subject;
    private String bodyTemplate; // May contain CLIENT_NAME, BOOKING_REFERENCE, REMAINING_AMOUNT, EVENT_DATE
    private String language;     // FR or EN; defaults to FR for template (greeting, labels, CTA, footer)

    public List<Long> getClientIds() { return clientIds; }
    public void setClientIds(List<Long> clientIds) { this.clientIds = clientIds; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBodyTemplate() { return bodyTemplate; }
    public void setBodyTemplate(String bodyTemplate) { this.bodyTemplate = bodyTemplate; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}
