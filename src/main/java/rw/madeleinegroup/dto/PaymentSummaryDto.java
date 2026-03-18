package rw.madeleinegroup.dto;

import rw.madeleinegroup.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentSummaryDto {
    private BigDecimal estimatedAmount;
    private BigDecimal totalPaidAmount;
    private BigDecimal remainingBalance;
    private PaymentStatus paymentStatus;
    private List<PaymentItemDto> payments;

    public static class PaymentItemDto {
        private Long id;
        private LocalDateTime recordedAt;
        private BigDecimal amount;
        private String paymentMethod;
        private String notes;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public LocalDateTime getRecordedAt() { return recordedAt; }
        public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public BigDecimal getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(BigDecimal estimatedAmount) { this.estimatedAmount = estimatedAmount; }
    public BigDecimal getTotalPaidAmount() { return totalPaidAmount; }
    public void setTotalPaidAmount(BigDecimal totalPaidAmount) { this.totalPaidAmount = totalPaidAmount; }
    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public List<PaymentItemDto> getPayments() { return payments; }
    public void setPayments(List<PaymentItemDto> payments) { this.payments = payments; }
}
