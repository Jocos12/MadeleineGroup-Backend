package rw.madeleinegroup.dto;

import rw.madeleinegroup.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingConfirmationResponse {
    private boolean confirmed;
    private String bookingReference;
    private String clientName;
    private LocalDate eventDate;
    private BigDecimal estimatedAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingBalance;
    private PaymentStatus paymentStatus;
    private String message;
    private String messageFr;

    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }
    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public BigDecimal getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(BigDecimal estimatedAmount) { this.estimatedAmount = estimatedAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getMessageFr() { return messageFr; }
    public void setMessageFr(String messageFr) { this.messageFr = messageFr; }
}
