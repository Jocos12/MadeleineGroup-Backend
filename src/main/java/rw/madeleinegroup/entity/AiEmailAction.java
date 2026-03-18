package rw.madeleinegroup.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_email_actions")
public class AiEmailAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "emails_sent", nullable = false)
    private int emailsSent;

    @Column(name = "total_amount_rwf", precision = 15, scale = 2)
    private BigDecimal totalAmountRwf;

    /** JSON array of client names, e.g. ["Client A", "Client B"] */
    @Column(name = "clients_contacted", columnDefinition = "TEXT")
    private String clientsContacted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by_user_id")
    private User triggeredBy;

    @CreationTimestamp
    @Column(name = "triggered_at")
    private LocalDateTime triggeredAt;

    @Column(length = 500)
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public int getEmailsSent() { return emailsSent; }
    public void setEmailsSent(int emailsSent) { this.emailsSent = emailsSent; }
    public BigDecimal getTotalAmountRwf() { return totalAmountRwf; }
    public void setTotalAmountRwf(BigDecimal totalAmountRwf) { this.totalAmountRwf = totalAmountRwf; }
    public String getClientsContacted() { return clientsContacted; }
    public void setClientsContacted(String clientsContacted) { this.clientsContacted = clientsContacted; }
    public User getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(User triggeredBy) { this.triggeredBy = triggeredBy; }
    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
