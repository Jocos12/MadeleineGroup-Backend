package rw.madeleinegroup.dto;

import jakarta.validation.constraints.*;
import rw.madeleinegroup.common.enums.ExpenseType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseRequest {
    @NotNull
    private Long branchId;

    @NotNull
    private ExpenseType category;

    @NotBlank
    private String description;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private LocalDate expenseDate;

    private String receiptUrl;

    /** CASH, BANK_TRANSFER, CREDIT_CARD, MOBILE_MONEY, CHEQUE, OTHER */
    private String paymentMethod;

    private String referenceNumber;
    private String paidTo;
    private String notes;
    private String roomNumber;

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public ExpenseType getCategory() { return category; }
    public void setCategory(ExpenseType category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public String getPaidTo() { return paidTo; }
    public void setPaidTo(String paidTo) { this.paidTo = paidTo; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
}
