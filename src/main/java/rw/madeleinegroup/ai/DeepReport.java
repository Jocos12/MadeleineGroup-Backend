package rw.madeleinegroup.ai;

import java.util.List;

public class DeepReport {
    private int healthScore;
    private String riskLevel;
    private String profitMarginGrade;
    private String trend;
    private String momentum;
    private String bestMonth;
    private String worstMonth;
    private double projectedNextMonthIncome;
    private double expenseRatio;
    private String topExpenseCategory;
    private List<String> expenseAlerts;
    private double wastedSpend;
    private double bookingCompletionRate;
    private double cancellationRate;
    private double averageRevenuePerBooking;
    private int clientAcquisitionRate;
    private double revenuePerClient;
    private double pendingRatio;
    private String cashFlowStatus;
    private List<String> urgentActions;
    private List<String> strategicRecommendations;
    private List<String> quickWins;
    private String topBranch;
    private String underperformingBranch;
    /** Exact RWF to cut from expenses to reach 60% margin (when margin &lt; 60). */
    private double rwfToSaveForTargetMargin;
    /** Exact RWF to earn (extra revenue) to reach 60% margin (when margin &lt; 60). */
    private double rwfToEarnForTargetMargin;
    /** Estimated financial impact of overdue bookings (e.g. at-risk revenue). */
    private double overdueBookingsImpactRwf;
    /** Suggested reduction target in RWF for the top expense category. */
    private double topExpenseCategoryReductionTargetRwf;

    public int getHealthScore() { return healthScore; }
    public void setHealthScore(int v) { this.healthScore = v; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String v) { this.riskLevel = v; }
    public String getProfitMarginGrade() { return profitMarginGrade; }
    public void setProfitMarginGrade(String v) { this.profitMarginGrade = v; }
    public String getTrend() { return trend; }
    public void setTrend(String v) { this.trend = v; }
    public String getMomentum() { return momentum; }
    public void setMomentum(String v) { this.momentum = v; }
    public String getBestMonth() { return bestMonth; }
    public void setBestMonth(String v) { this.bestMonth = v; }
    public String getWorstMonth() { return worstMonth; }
    public void setWorstMonth(String v) { this.worstMonth = v; }
    public double getProjectedNextMonthIncome() { return projectedNextMonthIncome; }
    public void setProjectedNextMonthIncome(double v) { this.projectedNextMonthIncome = v; }
    public double getExpenseRatio() { return expenseRatio; }
    public void setExpenseRatio(double v) { this.expenseRatio = v; }
    public String getTopExpenseCategory() { return topExpenseCategory; }
    public void setTopExpenseCategory(String v) { this.topExpenseCategory = v; }
    public List<String> getExpenseAlerts() { return expenseAlerts; }
    public void setExpenseAlerts(List<String> v) { this.expenseAlerts = v; }
    public double getWastedSpend() { return wastedSpend; }
    public void setWastedSpend(double v) { this.wastedSpend = v; }
    public double getBookingCompletionRate() { return bookingCompletionRate; }
    public void setBookingCompletionRate(double v) { this.bookingCompletionRate = v; }
    public double getCancellationRate() { return cancellationRate; }
    public void setCancellationRate(double v) { this.cancellationRate = v; }
    public double getAverageRevenuePerBooking() { return averageRevenuePerBooking; }
    public void setAverageRevenuePerBooking(double v) { this.averageRevenuePerBooking = v; }
    public int getClientAcquisitionRate() { return clientAcquisitionRate; }
    public void setClientAcquisitionRate(int v) { this.clientAcquisitionRate = v; }
    public double getRevenuePerClient() { return revenuePerClient; }
    public void setRevenuePerClient(double v) { this.revenuePerClient = v; }
    public double getPendingRatio() { return pendingRatio; }
    public void setPendingRatio(double v) { this.pendingRatio = v; }
    public String getCashFlowStatus() { return cashFlowStatus; }
    public void setCashFlowStatus(String v) { this.cashFlowStatus = v; }
    public List<String> getUrgentActions() { return urgentActions; }
    public void setUrgentActions(List<String> v) { this.urgentActions = v; }
    public List<String> getStrategicRecommendations() { return strategicRecommendations; }
    public void setStrategicRecommendations(List<String> v) { this.strategicRecommendations = v; }
    public List<String> getQuickWins() { return quickWins; }
    public void setQuickWins(List<String> v) { this.quickWins = v; }
    public String getTopBranch() { return topBranch; }
    public void setTopBranch(String v) { this.topBranch = v; }
    public String getUnderperformingBranch() { return underperformingBranch; }
    public void setUnderperformingBranch(String v) { this.underperformingBranch = v; }
    public double getRwfToSaveForTargetMargin() { return rwfToSaveForTargetMargin; }
    public void setRwfToSaveForTargetMargin(double v) { this.rwfToSaveForTargetMargin = v; }
    public double getRwfToEarnForTargetMargin() { return rwfToEarnForTargetMargin; }
    public void setRwfToEarnForTargetMargin(double v) { this.rwfToEarnForTargetMargin = v; }
    public double getOverdueBookingsImpactRwf() { return overdueBookingsImpactRwf; }
    public void setOverdueBookingsImpactRwf(double v) { this.overdueBookingsImpactRwf = v; }
    public double getTopExpenseCategoryReductionTargetRwf() { return topExpenseCategoryReductionTargetRwf; }
    public void setTopExpenseCategoryReductionTargetRwf(double v) { this.topExpenseCategoryReductionTargetRwf = v; }
}
