package rw.madeleinegroup.dto;

import java.time.LocalDateTime;

public class DebtReminderItemDto {
    private Long id;
    private LocalDateTime sentAt;
    private String method;
    private String emailLanguage;
    private String message;
    private String sentByName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getEmailLanguage() { return emailLanguage; }
    public void setEmailLanguage(String emailLanguage) { this.emailLanguage = emailLanguage; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSentByName() { return sentByName; }
    public void setSentByName(String sentByName) { this.sentByName = sentByName; }
}
