package rw.madeleinegroup.dto;

import rw.madeleinegroup.common.enums.PaymentMethod;
import rw.madeleinegroup.common.enums.PaymentStatus;
import rw.madeleinegroup.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    private Long id;
    private Long branchId;
    private String branchName;
    private Long bookingId;
    private String bookingReference;
    private Long clientId;
    private String clientName;
    private Payment.PaymentType type;
    private BigDecimal amount;
    private BigDecimal remainingBalance;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String description;
    private Long recordedById;
    private String recordedByName;
    private LocalDateTime recordedAt;
    private String updatedByName;
    private LocalDateTime updatedAt;

    public static PaymentResponse from(Payment p) {
        PaymentResponse r = new PaymentResponse();
        r.setId(p.getId());
        r.setBranchId(p.getBranch() != null ? p.getBranch().getId() : null);
        r.setBranchName(p.getBranch() != null ? p.getBranch().getName() : null);
        r.setBookingId(p.getBooking() != null ? p.getBooking().getId() : null);
        r.setBookingReference(p.getBooking() != null ? p.getBooking().getBookingReference() : null);
        r.setClientId(p.getClient() != null ? p.getClient().getId() : (p.getBooking() != null && p.getBooking().getClient() != null ? p.getBooking().getClient().getId() : null));
        r.setClientName(p.getClient() != null ? p.getClient().getFullName() : (p.getBooking() != null && p.getBooking().getClient() != null ? p.getBooking().getClient().getFullName() : null));
        r.setType(p.getType());
        r.setAmount(p.getAmount());
        r.setRemainingBalance(p.getRemainingBalance());
        r.setPaymentMethod(p.getPaymentMethod());
        r.setPaymentStatus(p.getPaymentStatus());
        r.setDescription(p.getDescription());
        r.setRecordedById(p.getRecordedBy() != null ? p.getRecordedBy().getId() : null);
        r.setRecordedByName(p.getRecordedBy() != null ? p.getRecordedBy().getFullName() : null);
        r.setRecordedAt(p.getRecordedAt());
        r.setUpdatedByName(p.getUpdatedBy() != null ? p.getUpdatedBy().getFullName() : null);
        r.setUpdatedAt(p.getUpdatedAt());
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public Payment.PaymentType getType() { return type; }
    public void setType(Payment.PaymentType type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getRecordedById() { return recordedById; }
    public void setRecordedById(Long recordedById) { this.recordedById = recordedById; }
    public String getRecordedByName() { return recordedByName; }
    public void setRecordedByName(String recordedByName) { this.recordedByName = recordedByName; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public String getUpdatedByName() { return updatedByName; }
    public void setUpdatedByName(String updatedByName) { this.updatedByName = updatedByName; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
