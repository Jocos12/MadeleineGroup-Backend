package rw.madeleinegroup.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Account-deletion request workflow. The acting user (CEO/admin) who approves or rejects
 * is stored as {@link #reviewedBy} / {@code reviewed_by_id} and {@link #reviewedAt}.
 * There is no separate {@code approved_by} column in the schema; {@link #setApprovedBy}
 * is a convenience alias that sets {@link #reviewedBy} only.
 */
@Entity
@Table(name = "delete_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Account to remove (always populated). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_to_delete_id", nullable = false)
    private User userToDelete;

    /** User who submitted the request (same as {@link #userToDelete} for self-service; may differ for admin/manager requests). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DeleteRequestStatus status = DeleteRequestStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    @CreationTimestamp
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public void setApprovedBy(User approver) {
        this.reviewedBy = approver;
    }
}
