package rw.madeleinegroup.ai;

public class YearComparison {
    private double currentYearIncome;
    private double previousYearIncome;
    private double growthAmount;
    private double growthPercent;
    private boolean isGrowing;

    public double getCurrentYearIncome() { return currentYearIncome; }
    public void setCurrentYearIncome(double v) { this.currentYearIncome = v; }
    public double getPreviousYearIncome() { return previousYearIncome; }
    public void setPreviousYearIncome(double v) { this.previousYearIncome = v; }
    public double getGrowthAmount() { return growthAmount; }
    public void setGrowthAmount(double v) { this.growthAmount = v; }
    public double getGrowthPercent() { return growthPercent; }
    public void setGrowthPercent(double v) { this.growthPercent = v; }
    public boolean isGrowing() { return isGrowing; }
    public void setIsGrowing(boolean v) { this.isGrowing = v; }
}
