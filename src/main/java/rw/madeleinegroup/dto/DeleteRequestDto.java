package rw.madeleinegroup.dto;

import rw.madeleinegroup.entity.DeleteRequest;
import rw.madeleinegroup.entity.DeleteRequestStatus;
import rw.madeleinegroup.entity.Role;

import java.time.LocalDateTime;

public record DeleteRequestDto(
        Long id,
        Long requestedById,
        String requestedByName,
        String requestedByEmail,
        String requestedByRole,
        String reason,
        DeleteRequestStatus status,
        Long reviewedById,
        String reviewedByName,
        String reviewNote,
        LocalDateTime requestedAt,
        LocalDateTime reviewedAt
) {
    public static DeleteRequestDto from(DeleteRequest r) {
        Role role = r.getRequestedBy() != null ? r.getRequestedBy().getRole() : null;
        return new DeleteRequestDto(
                r.getId(),
                r.getRequestedBy() != null ? r.getRequestedBy().getId() : null,
                r.getRequestedBy() != null ? r.getRequestedBy().getFullName() : null,
                r.getRequestedBy() != null ? r.getRequestedBy().getEmail() : null,
                role != null ? role.name() : null,
                r.getReason(),
                r.getStatus(),
                r.getReviewedBy() != null ? r.getReviewedBy().getId() : null,
                r.getReviewedBy() != null ? r.getReviewedBy().getFullName() : null,
                r.getReviewNote(),
                r.getRequestedAt(),
                r.getReviewedAt()
        );
    }
}
