package rw.madeleinegroup.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "blocked_dates", uniqueConstraints = @UniqueConstraint(columnNames = "blocked_date"))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BlockedDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blocked_date", nullable = false)
    private LocalDate blockedDate;

    @Column(length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_by_id")
    private User blockedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public BlockedDate() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getBlockedDate() {
        return blockedDate;
    }

    public void setBlockedDate(LocalDate blockedDate) {
        this.blockedDate = blockedDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public User getBlockedBy() {
        return blockedBy;
    }

    public void setBlockedBy(User blockedBy) {
        this.blockedBy = blockedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
