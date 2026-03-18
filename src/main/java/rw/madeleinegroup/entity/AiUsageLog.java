package rw.madeleinegroup.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_usage_logs")
public class AiUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "user_message", columnDefinition = "TEXT")
    private String userMessage;

    @Column(name = "response_length")
    private Integer responseLength;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "was_rate_limited")
    private Boolean wasRateLimited = false;

    @Column(name = "error_occurred")
    private Boolean errorOccurred = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getUserMessage() { return userMessage; }
    public void setUserMessage(String userMessage) { this.userMessage = userMessage; }
    public Integer getResponseLength() { return responseLength; }
    public void setResponseLength(Integer responseLength) { this.responseLength = responseLength; }
    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    public Boolean getWasRateLimited() { return wasRateLimited; }
    public void setWasRateLimited(Boolean wasRateLimited) { this.wasRateLimited = wasRateLimited; }
    public Boolean getErrorOccurred() { return errorOccurred; }
    public void setErrorOccurred(Boolean errorOccurred) { this.errorOccurred = errorOccurred; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
