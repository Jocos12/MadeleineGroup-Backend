package rw.madeleinegroup.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Booking fully paid — eligible for invoice email from dashboard. */
public class InvoiceEligibleRowDto {
    private Long bookingId;
    private String bookingReference;
    private String clientName;
    private String clientEmail;
    private String branchName;
    private LocalDate eventDate;
    private BigDecimal estimatedAmount;
    private BigDecimal paidAmount;
    private List<InvoicePaymentLineDto> paymentLines = new ArrayList<>();

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public BigDecimal getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(BigDecimal estimatedAmount) { this.estimatedAmount = estimatedAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public List<InvoicePaymentLineDto> getPaymentLines() {
        return paymentLines;
    }

    public void setPaymentLines(List<InvoicePaymentLineDto> paymentLines) {
        this.paymentLines = paymentLines != null ? paymentLines : new ArrayList<>();
    }
}
