package rw.madeleinegroup.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class BlockedDateRequest {
    @NotNull(message = "Date is required")
    private LocalDate blockedDate;

    private String reason;

    public LocalDate getBlockedDate() { return blockedDate; }
    public void setBlockedDate(LocalDate blockedDate) { this.blockedDate = blockedDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
