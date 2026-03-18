package rw.madeleinegroup.dto;

import rw.madeleinegroup.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BookingResponse {
    private Long id;
    private String bookingReference;
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private Long branchId;
    private String branchName;
    private BookingStatus status;
    private String eventType;
    private LocalDate eventDate;
    private Integer guestCount;
    private String notes;
    private String adminNotes;
    private BigDecimal estimatedAmount;
    private BigDecimal paidAmount;
    /**
     * Total discount across all packages compared to the official catalog prices.
     */
    private BigDecimal totalDiscount;
    private Long createdById;
    private String createdByName;
    private String source;
    private String lastModifiedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BookingPackageResponse> packages;

    public BookingResponse() {}
    public BookingResponse(Long id, String bookingReference, Long clientId, String clientName, String clientEmail,
                           Long branchId, String branchName, BookingStatus status, String eventType, LocalDate eventDate,
                           Integer guestCount, String notes, String adminNotes, BigDecimal estimatedAmount, BigDecimal paidAmount,
                           BigDecimal totalDiscount, Long createdById, String createdByName, String source, String lastModifiedBy,
                           LocalDateTime createdAt, LocalDateTime updatedAt, List<BookingPackageResponse> packages) {
        this.id = id;
        this.bookingReference = bookingReference;
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientEmail = clientEmail;
        this.branchId = branchId;
        this.branchName = branchName;
        this.status = status;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.guestCount = guestCount;
        this.notes = notes;
        this.adminNotes = adminNotes;
        this.estimatedAmount = estimatedAmount;
        this.paidAmount = paidAmount;
        this.totalDiscount = totalDiscount;
        this.createdById = createdById;
        this.createdByName = createdByName;
        this.source = source;
        this.lastModifiedBy = lastModifiedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.packages = packages;
    }

    public static BookingResponseBuilder builder() { return new BookingResponseBuilder(); }
    public static class BookingResponseBuilder {
        private Long id; private String bookingReference; private Long clientId; private String clientName; private String clientEmail;
        private Long branchId; private String branchName; private BookingStatus status; private String eventType; private LocalDate eventDate;
        private Integer guestCount; private String notes; private String adminNotes; private BigDecimal estimatedAmount; private BigDecimal paidAmount;
        private BigDecimal totalDiscount;
        private Long createdById; private String createdByName; private String source; private String lastModifiedBy;
        private LocalDateTime createdAt; private LocalDateTime updatedAt;
        private List<BookingPackageResponse> packages;
        public BookingResponseBuilder id(Long id) { this.id = id; return this; }
        public BookingResponseBuilder bookingReference(String v) { this.bookingReference = v; return this; }
        public BookingResponseBuilder clientId(Long v) { this.clientId = v; return this; }
        public BookingResponseBuilder clientName(String v) { this.clientName = v; return this; }
        public BookingResponseBuilder clientEmail(String v) { this.clientEmail = v; return this; }
        public BookingResponseBuilder branchId(Long v) { this.branchId = v; return this; }
        public BookingResponseBuilder branchName(String v) { this.branchName = v; return this; }
        public BookingResponseBuilder status(BookingStatus v) { this.status = v; return this; }
        public BookingResponseBuilder eventType(String v) { this.eventType = v; return this; }
        public BookingResponseBuilder eventDate(LocalDate v) { this.eventDate = v; return this; }
        public BookingResponseBuilder guestCount(Integer v) { this.guestCount = v; return this; }
        public BookingResponseBuilder notes(String v) { this.notes = v; return this; }
        public BookingResponseBuilder adminNotes(String v) { this.adminNotes = v; return this; }
        public BookingResponseBuilder estimatedAmount(BigDecimal v) { this.estimatedAmount = v; return this; }
        public BookingResponseBuilder paidAmount(BigDecimal v) { this.paidAmount = v; return this; }
        public BookingResponseBuilder totalDiscount(BigDecimal v) { this.totalDiscount = v; return this; }
        public BookingResponseBuilder createdById(Long v) { this.createdById = v; return this; }
        public BookingResponseBuilder createdByName(String v) { this.createdByName = v; return this; }
        public BookingResponseBuilder source(String v) { this.source = v; return this; }
        public BookingResponseBuilder lastModifiedBy(String v) { this.lastModifiedBy = v; return this; }
        public BookingResponseBuilder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public BookingResponseBuilder updatedAt(LocalDateTime v) { this.updatedAt = v; return this; }
        public BookingResponseBuilder packages(List<BookingPackageResponse> v) { this.packages = v; return this; }
        public BookingResponse build() { return new BookingResponse(id, bookingReference, clientId, clientName, clientEmail, branchId, branchName, status, eventType, eventDate, guestCount, notes, adminNotes, estimatedAmount, paidAmount, totalDiscount, createdById, createdByName, source, lastModifiedBy, createdAt, updatedAt, packages); }
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getBookingReference() { return bookingReference; } public void setBookingReference(String v) { this.bookingReference = v; }
    public Long getClientId() { return clientId; } public void setClientId(Long v) { this.clientId = v; }
    public String getClientName() { return clientName; } public void setClientName(String v) { this.clientName = v; }
    public String getClientEmail() { return clientEmail; } public void setClientEmail(String v) { this.clientEmail = v; }
    public Long getBranchId() { return branchId; } public void setBranchId(Long v) { this.branchId = v; }
    public String getBranchName() { return branchName; } public void setBranchName(String v) { this.branchName = v; }
    public BookingStatus getStatus() { return status; } public void setStatus(BookingStatus v) { this.status = v; }
    public String getEventType() { return eventType; } public void setEventType(String v) { this.eventType = v; }
    public LocalDate getEventDate() { return eventDate; } public void setEventDate(LocalDate v) { this.eventDate = v; }
    public Integer getGuestCount() { return guestCount; } public void setGuestCount(Integer v) { this.guestCount = v; }
    public String getNotes() { return notes; } public void setNotes(String v) { this.notes = v; }
    public String getAdminNotes() { return adminNotes; } public void setAdminNotes(String v) { this.adminNotes = v; }
    public BigDecimal getEstimatedAmount() { return estimatedAmount; } public void setEstimatedAmount(BigDecimal v) { this.estimatedAmount = v; }
    public BigDecimal getPaidAmount() { return paidAmount; } public void setPaidAmount(BigDecimal v) { this.paidAmount = v; }
    public BigDecimal getTotalDiscount() { return totalDiscount; } public void setTotalDiscount(BigDecimal totalDiscount) { this.totalDiscount = totalDiscount; }
    public Long getCreatedById() { return createdById; } public void setCreatedById(Long v) { this.createdById = v; }
    public String getCreatedByName() { return createdByName; } public void setCreatedByName(String v) { this.createdByName = v; }
    public String getSource() { return source; } public void setSource(String v) { this.source = v; }
    public String getLastModifiedBy() { return lastModifiedBy; } public void setLastModifiedBy(String v) { this.lastModifiedBy = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
    public List<BookingPackageResponse> getPackages() { return packages; } public void setPackages(List<BookingPackageResponse> v) { this.packages = v; }
}
