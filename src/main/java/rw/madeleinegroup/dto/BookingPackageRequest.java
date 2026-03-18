package rw.madeleinegroup.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class BookingPackageRequest {
    @NotNull
    private Long packageId;

    @Positive
    private Integer quantity = 1;

    /**
     * Optional explicit unit price sent from the client.
     * When a negotiatedPrice is provided, that value is preferred for calculations.
     */
    private BigDecimal unitPrice;

    /**
     * Optional negotiated price that overrides the official catalog price
     * for this package line when present.
     */
    private BigDecimal negotiatedPrice;

    /**
     * Optional free-text reason describing why the price was negotiated/changed.
     */
    private String negotiationReason;

    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getNegotiatedPrice() { return negotiatedPrice; }
    public void setNegotiatedPrice(BigDecimal negotiatedPrice) { this.negotiatedPrice = negotiatedPrice; }
    public String getNegotiationReason() { return negotiationReason; }
    public void setNegotiationReason(String negotiationReason) { this.negotiationReason = negotiationReason; }
}
