package rw.madeleinegroup.dto;

import java.math.BigDecimal;

public class PaymentBranchStatDto {
    private Long branchId;
    private String branchName;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;
    private long paymentCount;
    private BigDecimal percentageOfTotal;

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }
    public BigDecimal getNetBalance() { return netBalance; }
    public void setNetBalance(BigDecimal netBalance) { this.netBalance = netBalance; }
    public long getPaymentCount() { return paymentCount; }
    public void setPaymentCount(long paymentCount) { this.paymentCount = paymentCount; }
    public BigDecimal getPercentageOfTotal() { return percentageOfTotal; }
    public void setPercentageOfTotal(BigDecimal percentageOfTotal) { this.percentageOfTotal = percentageOfTotal; }
}
