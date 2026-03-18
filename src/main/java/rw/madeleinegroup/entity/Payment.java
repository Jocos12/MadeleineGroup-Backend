package rw.madeleinegroup.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import rw.madeleinegroup.common.enums.PaymentMethod;
import rw.madeleinegroup.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "remaining_balance", precision = 15, scale = 2)
    private BigDecimal remainingBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_id", nullable = false)
    private User recordedBy;

    @CreationTimestamp
    private LocalDateTime recordedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "update_reason")
    private String updateReason;

    public enum PaymentType {
        INCOME,
        EXPENSE
    }

    public Payment() {
    }

    public Payment(Long id, Branch branch, Booking booking, PaymentType type, BigDecimal amount,
                  String description, User recordedBy, LocalDateTime recordedAt) {
        this.id = id;
        this.branch = branch;
        this.booking = booking;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.recordedBy = recordedBy;
        this.recordedAt = recordedAt;
    }

    public static PaymentBuilder builder() {
        return new PaymentBuilder();
    }

    public static class PaymentBuilder {
        private Long id;
        private Branch branch;
        private Booking booking;
        private Client client;
        private PaymentType type;
        private BigDecimal amount;
        private BigDecimal remainingBalance;
        private rw.madeleinegroup.common.enums.PaymentMethod paymentMethod;
        private rw.madeleinegroup.common.enums.PaymentStatus paymentStatus;
        private String description;
        private User recordedBy;
        private LocalDateTime recordedAt;

        public PaymentBuilder id(Long id) { this.id = id; return this; }
        public PaymentBuilder branch(Branch branch) { this.branch = branch; return this; }
        public PaymentBuilder booking(Booking booking) { this.booking = booking; return this; }
        public PaymentBuilder client(Client client) { this.client = client; return this; }
        public PaymentBuilder type(PaymentType type) { this.type = type; return this; }
        public PaymentBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public PaymentBuilder remainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; return this; }
        public PaymentBuilder paymentMethod(rw.madeleinegroup.common.enums.PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public PaymentBuilder paymentStatus(rw.madeleinegroup.common.enums.PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; return this; }
        public PaymentBuilder description(String description) { this.description = description; return this; }
        public PaymentBuilder recordedBy(User recordedBy) { this.recordedBy = recordedBy; return this; }
        public PaymentBuilder recordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; return this; }

        public Payment build() {
            Payment p = new Payment(id, branch, booking, type, amount, description, recordedBy, recordedAt);
            if (client != null) p.setClient(client);
            if (remainingBalance != null) p.setRemainingBalance(remainingBalance);
            if (paymentMethod != null) p.setPaymentMethod(paymentMethod);
            if (paymentStatus != null) p.setPaymentStatus(paymentStatus);
            return p;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public PaymentType getType() { return type; }
    public void setType(PaymentType type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public User getRecordedBy() { return recordedBy; }
    public void setRecordedBy(User recordedBy) { this.recordedBy = recordedBy; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public User getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(User updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getUpdateReason() { return updateReason; }
    public void setUpdateReason(String updateReason) { this.updateReason = updateReason; }
}
