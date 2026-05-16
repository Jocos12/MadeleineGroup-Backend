package rw.madeleinegroup.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PaymentAnalyticsResponse {
    private long totalPayments;
    /** Same as {@link #totalPayments} — preferred key for new clients. */
    private long totalCount;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;
    private int branchesWithPayments;
    /** Same as {@link #branchesWithPayments}. */
    private int activeBranchCount;
    private PaymentTopBranchDto topBranch;
    private List<PaymentBranchStatDto> byBranch = new ArrayList<>();
    private List<PaymentMonthStatDto> byMonth = new ArrayList<>();
    private List<PaymentRecorderStatDto> topRecorders = new ArrayList<>();

    public long getTotalPayments() { return totalPayments; }
    public void setTotalPayments(long totalPayments) { this.totalPayments = totalPayments; }
    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }
    public BigDecimal getNetBalance() { return netBalance; }
    public void setNetBalance(BigDecimal netBalance) { this.netBalance = netBalance; }
    public int getBranchesWithPayments() { return branchesWithPayments; }
    public void setBranchesWithPayments(int branchesWithPayments) { this.branchesWithPayments = branchesWithPayments; }
    public int getActiveBranchCount() { return activeBranchCount; }
    public void setActiveBranchCount(int activeBranchCount) { this.activeBranchCount = activeBranchCount; }
    public PaymentTopBranchDto getTopBranch() { return topBranch; }
    public void setTopBranch(PaymentTopBranchDto topBranch) { this.topBranch = topBranch; }
    public List<PaymentBranchStatDto> getByBranch() { return byBranch; }
    public void setByBranch(List<PaymentBranchStatDto> byBranch) { this.byBranch = byBranch; }
    public List<PaymentMonthStatDto> getByMonth() { return byMonth; }
    public void setByMonth(List<PaymentMonthStatDto> byMonth) { this.byMonth = byMonth; }
    public List<PaymentRecorderStatDto> getTopRecorders() { return topRecorders; }
    public void setTopRecorders(List<PaymentRecorderStatDto> topRecorders) { this.topRecorders = topRecorders; }
}
