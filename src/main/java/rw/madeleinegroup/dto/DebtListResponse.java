package rw.madeleinegroup.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DebtListResponse {
    private BigDecimal totalOutstandingRwf = BigDecimal.ZERO;
    private BigDecimal totalEstimatedRwf = BigDecimal.ZERO;
    private BigDecimal totalPaidRwf = BigDecimal.ZERO;
    private int bookingCount;
    /** Event date strictly before today, among listed rows */
    private int overdueEventCount;
    /** Estimated > 0 and 0 < paid < estimated */
    private int partialPaymentCount;
    private List<DebtBookingRowDto> rows = new ArrayList<>();

    public BigDecimal getTotalOutstandingRwf() { return totalOutstandingRwf; }
    public void setTotalOutstandingRwf(BigDecimal totalOutstandingRwf) { this.totalOutstandingRwf = totalOutstandingRwf; }
    public BigDecimal getTotalEstimatedRwf() { return totalEstimatedRwf; }
    public void setTotalEstimatedRwf(BigDecimal totalEstimatedRwf) { this.totalEstimatedRwf = totalEstimatedRwf; }
    public BigDecimal getTotalPaidRwf() { return totalPaidRwf; }
    public void setTotalPaidRwf(BigDecimal totalPaidRwf) { this.totalPaidRwf = totalPaidRwf; }
    public int getBookingCount() { return bookingCount; }
    public void setBookingCount(int bookingCount) { this.bookingCount = bookingCount; }
    public int getOverdueEventCount() { return overdueEventCount; }
    public void setOverdueEventCount(int overdueEventCount) { this.overdueEventCount = overdueEventCount; }
    public int getPartialPaymentCount() { return partialPaymentCount; }
    public void setPartialPaymentCount(int partialPaymentCount) { this.partialPaymentCount = partialPaymentCount; }
    public List<DebtBookingRowDto> getRows() { return rows; }
    public void setRows(List<DebtBookingRowDto> rows) { this.rows = rows != null ? rows : new ArrayList<>(); }
}
