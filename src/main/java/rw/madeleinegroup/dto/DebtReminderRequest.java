package rw.madeleinegroup.dto;

import jakarta.validation.constraints.NotNull;

public class DebtReminderRequest {

    @NotNull
    private Long bookingId;

    /**
     * Ignored by the API — reminders are logged as email only.
     * @deprecated kept for backward compatibility
     */
    private String method;

    /** Email body (may be generated on the client in EN, FR or RW). */
    private String message;

    /** en | fr | rw — language of the email text */
    private String emailLanguage;

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getEmailLanguage() { return emailLanguage; }
    public void setEmailLanguage(String emailLanguage) { this.emailLanguage = emailLanguage; }
}
