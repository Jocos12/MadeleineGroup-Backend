package rw.madeleinegroup.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Log of a debt / payment reminder sent for a booking (email, SMS, WhatsApp, etc.).
 */
@Entity
@Table(name = "debt_reminders")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DebtReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnore
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnore
    private Client client;

    @CreationTimestamp
    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sent_by")
    private User sentBy;

    /** Always "email" for debt reminders from the Debts dashboard. */
    @Column(length = 20)
    private String method;

    /** Language used for the email body: en, fr, rw */
    @Column(name = "email_language", length = 8)
    private String emailLanguage;

    @Column(columnDefinition = "TEXT")
    private String message;

    public DebtReminder() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public User getSentBy() { return sentBy; }
    public void setSentBy(User sentBy) { this.sentBy = sentBy; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getEmailLanguage() { return emailLanguage; }
    public void setEmailLanguage(String emailLanguage) { this.emailLanguage = emailLanguage; }
}
