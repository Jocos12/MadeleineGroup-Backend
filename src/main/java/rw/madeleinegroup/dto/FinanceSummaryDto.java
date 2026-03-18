package rw.madeleinegroup.dto;

import rw.madeleinegroup.entity.Branch;

import java.math.BigDecimal;
import java.util.List;

public class FinanceSummaryDto {
    private Branch branch;         // null if global summary
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netProfit;
    private BigDecimal pendingAmount;
    private Long totalBookings;
    private Long completedBookings;
    private Long pendingBookings;
    private Long cancelledBookings;
    private Long totalClients;
    private Integer period;        // Month or year
    private Integer year;
    private List<MonthlyBreakdown> monthlyBreakdown;

    public static class MonthlyBreakdown {
        private String month;
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal net;

        public MonthlyBreakdown() {}
        public MonthlyBreakdown(String month, BigDecimal income, BigDecimal expenses, BigDecimal net) {
            this.month = month;
            this.income = income;
            this.expenses = expenses;
            this.net = net;
        }
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public BigDecimal getIncome() { return income; }
        public void setIncome(BigDecimal income) { this.income = income; }
        public BigDecimal getExpenses() { return expenses; }
        public void setExpenses(BigDecimal expenses) { this.expenses = expenses; }
        public BigDecimal getNet() { return net; }
        public void setNet(BigDecimal net) { this.net = net; }
    }

    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }
    public BigDecimal getNetProfit() { return netProfit; }
    public void setNetProfit(BigDecimal netProfit) { this.netProfit = netProfit; }
    public BigDecimal getPendingAmount() { return pendingAmount; }
    public void setPendingAmount(BigDecimal pendingAmount) { this.pendingAmount = pendingAmount; }
    public Long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(Long totalBookings) { this.totalBookings = totalBookings; }
    public Long getCompletedBookings() { return completedBookings; }
    public void setCompletedBookings(Long completedBookings) { this.completedBookings = completedBookings; }
    public Long getPendingBookings() { return pendingBookings; }
    public void setPendingBookings(Long pendingBookings) { this.pendingBookings = pendingBookings; }
    public Long getCancelledBookings() { return cancelledBookings; }
    public void setCancelledBookings(Long cancelledBookings) { this.cancelledBookings = cancelledBookings; }
    public Long getTotalClients() { return totalClients; }
    public void setTotalClients(Long totalClients) { this.totalClients = totalClients; }
    public Integer getPeriod() { return period; }
    public void setPeriod(Integer period) { this.period = period; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public List<MonthlyBreakdown> getMonthlyBreakdown() { return monthlyBreakdown; }
    public void setMonthlyBreakdown(List<MonthlyBreakdown> monthlyBreakdown) { this.monthlyBreakdown = monthlyBreakdown; }
}
