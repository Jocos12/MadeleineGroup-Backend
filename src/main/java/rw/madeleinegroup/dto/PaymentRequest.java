package rw.madeleinegroup.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

import rw.madeleinegroup.common.enums.PaymentMethod;
import rw.madeleinegroup.common.enums.PaymentStatus;

public class PaymentRequest {
    @NotNull
    private Long branchId;

    private Long bookingId;

    @NotNull
    private String type; // INCOME, EXPENSE

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    private String description;

    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
}
