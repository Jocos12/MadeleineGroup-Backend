package rw.madeleinegroup.dto;

import java.math.BigDecimal;

/** Aggregated payments recorded by a user (for dashboard "top recorders"). */
public class PaymentRecorderStatDto {
    private Long userId;
    private String fullName;
    private long paymentCount;
    private BigDecimal totalVolume;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public long getPaymentCount() { return paymentCount; }
    public void setPaymentCount(long paymentCount) { this.paymentCount = paymentCount; }
    public BigDecimal getTotalVolume() { return totalVolume; }
    public void setTotalVolume(BigDecimal totalVolume) { this.totalVolume = totalVolume; }
}
