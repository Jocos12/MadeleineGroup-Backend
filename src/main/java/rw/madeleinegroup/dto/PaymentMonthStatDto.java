package rw.madeleinegroup.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class PaymentMonthStatDto {
    private int year;
    private int month;
    /** ISO month key, e.g. {@code 2026-05} — JSON property {@code month}. */
    private String monthKey;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal net;

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    @JsonIgnore
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    @JsonProperty("month")
    public String getMonthKey() { return monthKey; }
    public void setMonthKey(String monthKey) { this.monthKey = monthKey; }
    public BigDecimal getIncome() { return income; }
    public void setIncome(BigDecimal income) { this.income = income; }
    public BigDecimal getExpense() { return expense; }
    public void setExpense(BigDecimal expense) { this.expense = expense; }
    public BigDecimal getNet() { return net; }
    public void setNet(BigDecimal net) { this.net = net; }
}
