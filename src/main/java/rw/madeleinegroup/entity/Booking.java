package rw.madeleinegroup.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bookingReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private LocalDate eventDate;

    private Integer guestCount;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Internal notes for staff only, not visible to client */
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(precision = 15, scale = 2)
    private BigDecimal estimatedAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal paidAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    /** Source of booking: "Client - Online" or "Name - Role" (e.g. "Sarah - Commercial") */
    @Column(name = "source", length = 100)
    private String source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;

    /** Display string for who last modified: "Name - Role" */
    @Column(name = "last_modified_by", length = 100)
    private String lastModifiedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingPackage> bookingPackages = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Payment> payments = new ArrayList<>();

    public Booking() {
    }

    public static BookingBuilder builder() {
        return new BookingBuilder();
    }

    public static class BookingBuilder {
        private String bookingReference;
        private Client client;
        private Branch branch;
        private BookingStatus status;
        private String eventType;
        private LocalDate eventDate;
        private Integer guestCount;
        private String notes;
        private BigDecimal estimatedAmount;
        private BigDecimal paidAmount;
        private User createdBy;

        public BookingBuilder bookingReference(String ref) { this.bookingReference = ref; return this; }
        public BookingBuilder client(Client c) { this.client = c; return this; }
        public BookingBuilder branch(Branch b) { this.branch = b; return this; }
        public BookingBuilder status(BookingStatus s) { this.status = s; return this; }
        public BookingBuilder eventType(String t) { this.eventType = t; return this; }
        public BookingBuilder eventDate(LocalDate d) { this.eventDate = d; return this; }
        public BookingBuilder guestCount(Integer c) { this.guestCount = c; return this; }
        public BookingBuilder notes(String n) { this.notes = n; return this; }
        public BookingBuilder estimatedAmount(BigDecimal a) { this.estimatedAmount = a; return this; }
        public BookingBuilder paidAmount(BigDecimal a) { this.paidAmount = a; return this; }
        public BookingBuilder createdBy(User u) { this.createdBy = u; return this; }

        public Booking build() {
            Booking b = new Booking();
            b.setBookingReference(bookingReference);
            b.setClient(client);
            b.setBranch(branch);
            b.setStatus(status);
            b.setEventType(eventType);
            b.setEventDate(eventDate);
            b.setGuestCount(guestCount);
            b.setNotes(notes);
            b.setEstimatedAmount(estimatedAmount);
            b.setPaidAmount(paidAmount != null ? paidAmount : BigDecimal.ZERO);
            b.setCreatedBy(createdBy);
            return b;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public Integer getGuestCount() { return guestCount; }
    public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    public BigDecimal getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(BigDecimal estimatedAmount) { this.estimatedAmount = estimatedAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public User getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(User updatedBy) { this.updatedBy = updatedBy; }
    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<BookingPackage> getBookingPackages() { return bookingPackages; }
    public void setBookingPackages(List<BookingPackage> bookingPackages) { this.bookingPackages = bookingPackages != null ? bookingPackages : new ArrayList<>(); }
    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments != null ? payments : new ArrayList<>(); }
}
