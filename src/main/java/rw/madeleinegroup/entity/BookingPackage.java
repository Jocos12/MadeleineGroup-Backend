package rw.madeleinegroup.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "booking_packages")
public class BookingPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private PackageItem packageItem;

    private Integer quantity;

    /**
     * Original catalog-based unit price at the time of booking.
     * This never changes once recorded so we can audit discounts reliably.
     */
    @Column(name = "original_price", precision = 15, scale = 2)
    private BigDecimal originalPrice;

    /**
     * Effective unit price used for this booking line.
     * When a negotiatedPrice is present, this reflects the negotiated value.
     */
    @Column(precision = 15, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Optional negotiated unit price when it differs from the catalog price.
     */
    @Column(name = "negotiated_price", precision = 15, scale = 2)
    private BigDecimal negotiatedPrice;

    /**
     * Optional free-text note explaining why the price was negotiated.
     */
    @Column(name = "negotiation_reason", columnDefinition = "TEXT")
    private String negotiationReason;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalPrice;

    public BookingPackage() {
    }

    public static BookingPackageBuilder builder() {
        return new BookingPackageBuilder();
    }

    public static class BookingPackageBuilder {
        private Booking booking;
        private PackageItem packageItem;
        private Integer quantity;
        private BigDecimal originalPrice;
        private BigDecimal unitPrice;
        private BigDecimal negotiatedPrice;
        private String negotiationReason;

        public BookingPackageBuilder booking(Booking b) { this.booking = b; return this; }
        public BookingPackageBuilder packageItem(PackageItem p) { this.packageItem = p; return this; }
        public BookingPackageBuilder quantity(Integer q) { this.quantity = q; return this; }
        public BookingPackageBuilder originalPrice(BigDecimal p) { this.originalPrice = p; return this; }
        public BookingPackageBuilder unitPrice(BigDecimal p) { this.unitPrice = p; return this; }
        public BookingPackageBuilder negotiatedPrice(BigDecimal p) { this.negotiatedPrice = p; return this; }
        public BookingPackageBuilder negotiationReason(String reason) { this.negotiationReason = reason; return this; }

        public BookingPackage build() {
            BookingPackage bp = new BookingPackage();
            bp.setBooking(booking);
            bp.setPackageItem(packageItem);
            bp.setQuantity(quantity != null ? quantity : 1);
            bp.setOriginalPrice(originalPrice);
            bp.setNegotiatedPrice(negotiatedPrice);
            bp.setNegotiationReason(negotiationReason);
            bp.setUnitPrice(unitPrice);
            if (bp.getUnitPrice() != null && bp.getQuantity() != null) {
                bp.setTotalPrice(bp.getUnitPrice().multiply(BigDecimal.valueOf(bp.getQuantity())));
            }
            return bp;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public PackageItem getPackageItem() { return packageItem; }
    public void setPackageItem(PackageItem packageItem) { this.packageItem = packageItem; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getNegotiatedPrice() { return negotiatedPrice; }
    public void setNegotiatedPrice(BigDecimal negotiatedPrice) { this.negotiatedPrice = negotiatedPrice; }
    public String getNegotiationReason() { return negotiationReason; }
    public void setNegotiationReason(String negotiationReason) { this.negotiationReason = negotiationReason; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    @Transient
    public boolean isPriceNegotiated() {
        if (negotiatedPrice == null) return false;
        if (originalPrice == null) return true;
        return negotiatedPrice.compareTo(originalPrice) != 0;
    }
}
