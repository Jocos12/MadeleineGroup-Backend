package rw.madeleinegroup.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseUpdateRequest {
    private Long branchId;
    private String category;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String receiptUrl;
    private String updateReason;

    public String getUpdateReason() { return updateReason; }
    public void setUpdateReason(String updateReason) { this.updateReason = updateReason; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
}
