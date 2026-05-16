package rw.madeleinegroup.dto;

import rw.madeleinegroup.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DebtBookingRowDto {
    private Long bookingId;
    private String bookingReference;
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private Long branchId;
    private String branchName;
    private BookingStatus status;
    private LocalDate eventDate;
    private BigDecimal estimatedAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingBalance;
    private String debtNotes;
    private String paymentMethod;
    private LocalDateTime lastReminderSent;

    public DebtBookingRowDto() {}

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public BigDecimal getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(BigDecimal estimatedAmount) { this.estimatedAmount = estimatedAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }
    public String getDebtNotes() { return debtNotes; }
    public void setDebtNotes(String debtNotes) { this.debtNotes = debtNotes; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public LocalDateTime getLastReminderSent() { return lastReminderSent; }
    public void setLastReminderSent(LocalDateTime lastReminderSent) { this.lastReminderSent = lastReminderSent; }
}
