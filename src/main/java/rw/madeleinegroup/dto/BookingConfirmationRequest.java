package rw.madeleinegroup.dto;

import rw.madeleinegroup.common.enums.PaymentMethod;

import java.math.BigDecimal;

public class BookingConfirmationRequest {
    private boolean clientPaidFull;
    private BigDecimal paidAmount;
    private PaymentMethod paymentMethod;
    private String paymentNotes;
    private boolean forceConfirm;

    public boolean isClientPaidFull() { return clientPaidFull; }
    public void setClientPaidFull(boolean clientPaidFull) { this.clientPaidFull = clientPaidFull; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentNotes() { return paymentNotes; }
    public void setPaymentNotes(String paymentNotes) { this.paymentNotes = paymentNotes; }
    public boolean isForceConfirm() { return forceConfirm; }
    public void setForceConfirm(boolean forceConfirm) { this.forceConfirm = forceConfirm; }
}
