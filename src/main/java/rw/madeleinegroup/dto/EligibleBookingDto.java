package rw.madeleinegroup.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EligibleBookingDto {
    private String bookingReference;
    private String eventType;
    private LocalDate eventDate;
    private BigDecimal estimatedAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingBalance;

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public BigDecimal getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(BigDecimal estimatedAmount) { this.estimatedAmount = estimatedAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }
}
