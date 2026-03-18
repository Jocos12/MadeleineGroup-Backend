package rw.madeleinegroup.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseRequest {
    @NotNull
    private Long branchId;

    @NotBlank
    private String category;   // SALAIRE, MATERIEL, LOCATION, AUTRES

    @NotBlank
    private String description;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private LocalDate expenseDate;

    private String receiptUrl;

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
