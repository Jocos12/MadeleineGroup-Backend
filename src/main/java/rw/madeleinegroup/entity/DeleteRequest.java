package rw.madeleinegroup.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "delete_requests")
public class DeleteRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_to_delete_id", nullable = false)
    private User userToDelete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeleteRequestStatus status = DeleteRequestStatus.PENDING;

    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    private LocalDateTime approvedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum DeleteRequestStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    public DeleteRequest() {
    }

    public DeleteRequest(Long id, User userToDelete, User requestedBy, DeleteRequestStatus status,
                         String reason, User approvedBy, LocalDateTime approvedAt, LocalDateTime createdAt) {
        this.id = id;
        this.userToDelete = userToDelete;
        this.requestedBy = requestedBy;
        this.status = status != null ? status : DeleteRequestStatus.PENDING;
        this.reason = reason;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.createdAt = createdAt;
    }

    public static DeleteRequestBuilder builder() {
        return new DeleteRequestBuilder();
    }

    public static class DeleteRequestBuilder {
        private Long id;
        private User userToDelete;
        private User requestedBy;
        private DeleteRequestStatus status = DeleteRequestStatus.PENDING;
        private String reason;
        private User approvedBy;
        private LocalDateTime approvedAt;
        private LocalDateTime createdAt;

        public DeleteRequestBuilder id(Long id) { this.id = id; return this; }
        public DeleteRequestBuilder userToDelete(User userToDelete) { this.userToDelete = userToDelete; return this; }
        public DeleteRequestBuilder requestedBy(User requestedBy) { this.requestedBy = requestedBy; return this; }
        public DeleteRequestBuilder status(DeleteRequestStatus status) { this.status = status; return this; }
        public DeleteRequestBuilder reason(String reason) { this.reason = reason; return this; }
        public DeleteRequestBuilder approvedBy(User approvedBy) { this.approvedBy = approvedBy; return this; }
        public DeleteRequestBuilder approvedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; return this; }
        public DeleteRequestBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public DeleteRequest build() {
            return new DeleteRequest(id, userToDelete, requestedBy, status, reason, approvedBy, approvedAt, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUserToDelete() { return userToDelete; }
    public void setUserToDelete(User userToDelete) { this.userToDelete = userToDelete; }
    public User getRequestedBy() { return requestedBy; }
    public void setRequestedBy(User requestedBy) { this.requestedBy = requestedBy; }
    public DeleteRequestStatus getStatus() { return status; }
    public void setStatus(DeleteRequestStatus status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
