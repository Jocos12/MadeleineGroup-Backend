package rw.madeleinegroup.dto;

import java.math.BigDecimal;

/**
 * One line on the invoice (finance {@link rw.madeleinegroup.entity.Payment} or debt installment).
 */
public class InvoicePaymentLineDto {

    /** ISO-8601 date/time for sorting and display */
    private String recordedAt;
    private BigDecimal amount;
    private String methodLabel;
    private String description;
    /** e.g. "Finance" or "Installment" */
    private String source;

    public String getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(String recordedAt) {
        this.recordedAt = recordedAt;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethodLabel() {
        return methodLabel;
    }

    public void setMethodLabel(String methodLabel) {
        this.methodLabel = methodLabel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
