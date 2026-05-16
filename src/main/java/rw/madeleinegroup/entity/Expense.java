package rw.madeleinegroup.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import rw.madeleinegroup.common.enums.ExpensePaymentMethod;
import rw.madeleinegroup.common.enums.ExpenseStatus;
import rw.madeleinegroup.common.enums.ExpenseType;

@Entity
@Table(name = "expenses")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Expense {

    /** CEO/ADMIN expenses at or below this amount are recorded as {@link rw.madeleinegroup.common.enums.ExpenseStatus#PAID} without a second-approver flow. */
    public static final BigDecimal CEO_AUTO_APPROVE_MAX_RWF = new BigDecimal("500000");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recorded_by_id")
    private User recordedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    /** Persisted as {@code expenses.category} (VARCHAR). API/JSON field: {@code category}. */
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "category", length = 64, nullable = true)
    private ExpenseType category;
    @Column(columnDefinition = "TEXT")
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String receiptUrl;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 32)
    private ExpensePaymentMethod paymentMethod;

    @Column(name = "reference_number")
    private String referenceNumber;

    /** Supplier, employee name, or payee label */
    @Column(name = "paid_to")
    private String paidTo;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Persist as VARCHAR so schema matches Flyway/DB; avoids MySQL native ENUM vs VARCHAR validation mismatch (Hibernate 6). */
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 64)
    private ExpenseStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "first_approved_by_id")
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private User firstApprovedBy;

    @Column(name = "first_approved_at")
    private LocalDateTime firstApprovedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by_id")
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /** Optional: room / asset reference for maintenance-style expenses */
    @Column(name = "room_number")
    private String roomNumber;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private User updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "update_reason")
    private String updateReason;

    public Expense() {}

    /** Null-safe status for legacy rows */
    public ExpenseStatus getEffectiveStatus() {
        return status != null ? status : ExpenseStatus.PAID;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getRecordedBy() { return recordedBy; }
    public void setRecordedBy(User recordedBy) { this.recordedBy = recordedBy; }
    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }
    public ExpenseType getCategory() { return category; }
    public void setCategory(ExpenseType category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
    public ExpensePaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(ExpensePaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public String getPaidTo() { return paidTo; }
    public void setPaidTo(String paidTo) { this.paidTo = paidTo; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public ExpenseStatus getStatus() { return status; }
    public void setStatus(ExpenseStatus status) { this.status = status; }
    public User getFirstApprovedBy() { return firstApprovedBy; }
    public void setFirstApprovedBy(User firstApprovedBy) { this.firstApprovedBy = firstApprovedBy; }
    public LocalDateTime getFirstApprovedAt() { return firstApprovedAt; }
    public void setFirstApprovedAt(LocalDateTime firstApprovedAt) { this.firstApprovedAt = firstApprovedAt; }
    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(User updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getUpdateReason() { return updateReason; }
    public void setUpdateReason(String updateReason) { this.updateReason = updateReason; }
}
