package rw.madeleinegroup.dto;



import com.fasterxml.jackson.annotation.JsonProperty;



import java.math.BigDecimal;



public class PaymentTopBranchDto {

    @JsonProperty("id")

    private Long branchId;

    private String name;

    /** Sum of income + expense amounts (total volume). */

    private BigDecimal totalAmount;

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal netBalance;

    private long paymentCount;



    public Long getBranchId() {

        return branchId;

    }



    public void setBranchId(Long branchId) {

        this.branchId = branchId;

    }



    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public BigDecimal getTotalAmount() { return totalAmount; }

    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getTotalIncome() { return totalIncome; }

    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getTotalExpense() { return totalExpense; }

    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }

    public BigDecimal getNetBalance() { return netBalance; }

    public void setNetBalance(BigDecimal netBalance) { this.netBalance = netBalance; }

    public long getPaymentCount() { return paymentCount; }

    public void setPaymentCount(long paymentCount) { this.paymentCount = paymentCount; }

}

