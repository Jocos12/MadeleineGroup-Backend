package rw.madeleinegroup.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BlockedDateResponse {
    private Long id;
    private LocalDate blockedDate;
    private String reason;
    private String blockedByName;
    private LocalDateTime createdAt;

    public BlockedDateResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getBlockedDate() { return blockedDate; }
    public void setBlockedDate(LocalDate blockedDate) { this.blockedDate = blockedDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getBlockedByName() { return blockedByName; }
    public void setBlockedByName(String blockedByName) { this.blockedByName = blockedByName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
