package rw.madeleinegroup.ai;

import java.util.List;

public class LiveFinancialSnapshot {
    private int year;
    private Integer month;
    private double totalIncome;
    private double totalExpenses;
    private double netProfit;
    private double profitMargin;
    private double pendingAmount;
    /** Sum of all INCOME payment amounts (lifetime / all recorded). */
    private double systemWideIncomePaymentsTotal;
    /** Sum of all expense-module rows (lifetime). */
    private double systemWideExpenseModuleTotal;
    /** Income payments minus expense-module total = dashboard "What We Keep (Net)". */
    private double whatWeKeepNet;
    private int totalBookings;
    private int confirmedBookings;
    private int completedBookings;
    private int pendingBookings;
    private int cancelledBookings;
    private int overdueBookings;
    private int totalClients;
    private int newClientsThisPeriod;
    private List<MonthlyData> monthlyTrend;
    private List<Object[]> categoryBreakdown;
    private List<Object[]> branchPerformance;
    private List<Object[]> topClients;
    private List<?> recentLargeExpenses;

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    public double getTotalIncome() { return totalIncome; }
    public void setTotalIncome(double v) { this.totalIncome = v; }
    public double getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(double v) { this.totalExpenses = v; }
    public double getNetProfit() { return netProfit; }
    public void setNetProfit(double v) { this.netProfit = v; }
    public double getProfitMargin() { return profitMargin; }
    public void setProfitMargin(double v) { this.profitMargin = v; }
    public double getPendingAmount() { return pendingAmount; }
    public void setPendingAmount(double v) { this.pendingAmount = v; }
    public double getSystemWideIncomePaymentsTotal() { return systemWideIncomePaymentsTotal; }
    public void setSystemWideIncomePaymentsTotal(double v) { this.systemWideIncomePaymentsTotal = v; }
    public double getSystemWideExpenseModuleTotal() { return systemWideExpenseModuleTotal; }
    public void setSystemWideExpenseModuleTotal(double v) { this.systemWideExpenseModuleTotal = v; }
    public double getWhatWeKeepNet() { return whatWeKeepNet; }
    public void setWhatWeKeepNet(double v) { this.whatWeKeepNet = v; }
    public int getTotalBookings() { return totalBookings; }
    public void setTotalBookings(int v) { this.totalBookings = v; }
    public int getConfirmedBookings() { return confirmedBookings; }
    public void setConfirmedBookings(int v) { this.confirmedBookings = v; }
    public int getCompletedBookings() { return completedBookings; }
    public void setCompletedBookings(int v) { this.completedBookings = v; }
    public int getPendingBookings() { return pendingBookings; }
    public void setPendingBookings(int v) { this.pendingBookings = v; }
    public int getCancelledBookings() { return cancelledBookings; }
    public void setCancelledBookings(int v) { this.cancelledBookings = v; }
    public int getOverdueBookings() { return overdueBookings; }
    public void setOverdueBookings(int v) { this.overdueBookings = v; }
    public int getTotalClients() { return totalClients; }
    public void setTotalClients(int v) { this.totalClients = v; }
    public int getNewClientsThisPeriod() { return newClientsThisPeriod; }
    public void setNewClientsThisPeriod(int v) { this.newClientsThisPeriod = v; }
    public List<MonthlyData> getMonthlyTrend() { return monthlyTrend; }
    public void setMonthlyTrend(List<MonthlyData> v) { this.monthlyTrend = v; }
    public List<Object[]> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(List<Object[]> v) { this.categoryBreakdown = v; }
    public List<Object[]> getBranchPerformance() { return branchPerformance; }
    public void setBranchPerformance(List<Object[]> v) { this.branchPerformance = v; }
    public List<Object[]> getTopClients() { return topClients; }
    public void setTopClients(List<Object[]> v) { this.topClients = v; }
    public List<?> getRecentLargeExpenses() { return recentLargeExpenses; }
    public void setRecentLargeExpenses(List<?> v) { this.recentLargeExpenses = v; }
}
