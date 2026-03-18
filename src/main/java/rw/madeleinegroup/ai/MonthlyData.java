package rw.madeleinegroup.ai;

public class MonthlyData {
    private int month;
    private String monthName;
    private double income;
    private double expenses;
    private double netProfit;
    private double profitMargin;
    private int bookingCount;

    public int getMonth() { return month; }
    public void setMonth(int v) { this.month = v; }
    public String getMonthName() { return monthName; }
    public void setMonthName(String v) { this.monthName = v; }
    public double getIncome() { return income; }
    public void setIncome(double v) { this.income = v; }
    public double getExpenses() { return expenses; }
    public void setExpenses(double v) { this.expenses = v; }
    public double getNetProfit() { return netProfit; }
    public void setNetProfit(double v) { this.netProfit = v; }
    public double getProfitMargin() { return profitMargin; }
    public void setProfitMargin(double v) { this.profitMargin = v; }
    public int getBookingCount() { return bookingCount; }
    public void setBookingCount(int v) { this.bookingCount = v; }
}
