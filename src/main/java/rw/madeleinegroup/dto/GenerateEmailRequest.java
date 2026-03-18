package rw.madeleinegroup.dto;

/**
 * Request to generate email template via Groq. No client data is sent.
 */
public class GenerateEmailRequest {
    private String emailType;   // PAYMENT_REMINDER, OVERDUE_NOTICE, FOLLOW_UP
    private String language;    // FR, EN
    private String userInstruction;
    private String tone;        // FORMAL, FRIENDLY, URGENT

    public String getEmailType() { return emailType; }
    public void setEmailType(String emailType) { this.emailType = emailType; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getUserInstruction() { return userInstruction; }
    public void setUserInstruction(String userInstruction) { this.userInstruction = userInstruction; }
    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }
}
