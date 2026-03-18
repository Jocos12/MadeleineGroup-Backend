package rw.madeleinegroup.dto;

import java.math.BigDecimal;

public class BookingPackageResponse {
    private Long id;
    private Long packageItemId;
    private String packageName;
    private Integer quantity;
    private BigDecimal originalPrice;
    private BigDecimal unitPrice;
    private BigDecimal negotiatedPrice;
    private Boolean priceNegotiated;
    private String negotiationReason;
    private BigDecimal totalPrice;

    public BookingPackageResponse() {}

    public BookingPackageResponse(Long id,
                                  Long packageItemId,
                                  String packageName,
                                  Integer quantity,
                                  BigDecimal originalPrice,
                                  BigDecimal unitPrice,
                                  BigDecimal negotiatedPrice,
                                  Boolean priceNegotiated,
                                  String negotiationReason,
                                  BigDecimal totalPrice) {
        this.id = id;
        this.packageItemId = packageItemId;
        this.packageName = packageName;
        this.quantity = quantity;
        this.originalPrice = originalPrice;
        this.unitPrice = unitPrice;
        this.negotiatedPrice = negotiatedPrice;
        this.priceNegotiated = priceNegotiated;
        this.negotiationReason = negotiationReason;
        this.totalPrice = totalPrice;
    }

    public static BookingPackageResponseBuilder builder() { return new BookingPackageResponseBuilder(); }

    public static class BookingPackageResponseBuilder {
        private Long id;
        private Long packageItemId;
        private String packageName;
        private Integer quantity;
        private BigDecimal originalPrice;
        private BigDecimal unitPrice;
        private BigDecimal negotiatedPrice;
        private Boolean priceNegotiated;
        private String negotiationReason;
        private BigDecimal totalPrice;

        public BookingPackageResponseBuilder id(Long v) { id = v; return this; }
        public BookingPackageResponseBuilder packageItemId(Long v) { packageItemId = v; return this; }
        public BookingPackageResponseBuilder packageName(String v) { packageName = v; return this; }
        public BookingPackageResponseBuilder quantity(Integer v) { quantity = v; return this; }
        public BookingPackageResponseBuilder originalPrice(BigDecimal v) { originalPrice = v; return this; }
        public BookingPackageResponseBuilder unitPrice(BigDecimal v) { unitPrice = v; return this; }
        public BookingPackageResponseBuilder negotiatedPrice(BigDecimal v) { negotiatedPrice = v; return this; }
        public BookingPackageResponseBuilder priceNegotiated(Boolean v) { priceNegotiated = v; return this; }
        public BookingPackageResponseBuilder negotiationReason(String v) { negotiationReason = v; return this; }
        public BookingPackageResponseBuilder totalPrice(BigDecimal v) { totalPrice = v; return this; }

        public BookingPackageResponse build() {
            return new BookingPackageResponse(
                    id, packageItemId, packageName, quantity,
                    originalPrice, unitPrice, negotiatedPrice,
                    priceNegotiated, negotiationReason, totalPrice
            );
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPackageItemId() { return packageItemId; }
    public void setPackageItemId(Long v) { this.packageItemId = v; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String v) { this.packageName = v; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer v) { this.quantity = v; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal v) { this.unitPrice = v; }
    public BigDecimal getNegotiatedPrice() { return negotiatedPrice; }
    public void setNegotiatedPrice(BigDecimal negotiatedPrice) { this.negotiatedPrice = negotiatedPrice; }
    public Boolean getPriceNegotiated() { return priceNegotiated; }
    public void setPriceNegotiated(Boolean priceNegotiated) { this.priceNegotiated = priceNegotiated; }
    public String getNegotiationReason() { return negotiationReason; }
    public void setNegotiationReason(String negotiationReason) { this.negotiationReason = negotiationReason; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal v) { this.totalPrice = v; }
}
