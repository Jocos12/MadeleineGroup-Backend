package rw.madeleinegroup.dto;

import rw.madeleinegroup.common.enums.PaymentMethod;
import rw.madeleinegroup.common.enums.PaymentStatus;

import java.math.BigDecimal;

public class PaymentUpdateRequest {
    private Long branchId;
    private Long bookingId;
    private Long clientId;
    private String type; // INCOME, EXPENSE
    private BigDecimal amount;
    private BigDecimal remainingBalance;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String description;
    private String updateReason;

    public String getUpdateReason() { return updateReason; }
    public void setUpdateReason(String updateReason) { this.updateReason = updateReason; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
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
}
