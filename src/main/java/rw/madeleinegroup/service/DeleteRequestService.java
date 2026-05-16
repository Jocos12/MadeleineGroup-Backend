package rw.madeleinegroup.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.madeleinegroup.dto.DeleteRequestDto;
import rw.madeleinegroup.entity.DeleteRequest;
import rw.madeleinegroup.entity.DeleteRequestStatus;
import rw.madeleinegroup.entity.Role;
import rw.madeleinegroup.entity.User;
import rw.madeleinegroup.repository.DeleteRequestRepository;
import rw.madeleinegroup.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DeleteRequestService {

    private final DeleteRequestRepository deleteRequestRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public DeleteRequestService(DeleteRequestRepository deleteRequestRepository,
                                UserRepository userRepository,
                                EmailService emailService) {
        this.deleteRequestRepository = deleteRequestRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public DeleteRequestDto submitRequest(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (deleteRequestRepository.existsByUserToDeleteIdAndStatus(userId, DeleteRequestStatus.PENDING)) {
            throw new IllegalStateException("You already have a pending deletion request");
        }
        DeleteRequest r = new DeleteRequest();
        r.setUserToDelete(user);
        r.setRequestedBy(user);
        r.setReason(reason != null ? reason.trim() : null);
        r.setStatus(DeleteRequestStatus.PENDING);
        DeleteRequest saved = deleteRequestRepository.save(r);
        notifyAdminsNewRequest(saved);
        return DeleteRequestDto.from(saved);
    }

    private void notifyAdminsNewRequest(DeleteRequest r) {
        User u = r.getRequestedBy();
        String name = u != null && u.getFullName() != null && !u.getFullName().isBlank()
                ? u.getFullName()
                : (u != null ? u.getEmail() : "User");
        List<String> emails = Stream.concat(
                        userRepository.findByRole(Role.CEO).stream(),
                        userRepository.findByRole(Role.ADMIN).stream())
                .map(User::getEmail)
                .filter(e -> e != null && !e.isBlank())
                .distinct()
                .collect(Collectors.toList());
        emailService.sendNewAccountDeletionRequestAlert(emails, name, u != null ? u.getEmail() : "", r.getReason());
    }

    @Transactional(readOnly = true)
    public List<DeleteRequestDto> getAll(DeleteRequestStatus status) {
        List<DeleteRequest> list = status == null
                ? deleteRequestRepository.findAllByOrderByRequestedAtDesc()
                : deleteRequestRepository.findByStatusOrderByRequestedAtDesc(status);
        return list.stream().map(DeleteRequestDto::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeleteRequestDto getById(Long id) {
        DeleteRequest r = deleteRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Delete request not found"));
        return DeleteRequestDto.from(r);
    }

    @Transactional(readOnly = true)
    public Optional<DeleteRequestDto> getMyRequest(Long userId) {
        return deleteRequestRepository.findTopByUserToDeleteIdOrderByRequestedAtDesc(userId)
                .map(DeleteRequestDto::from);
    }

    @Transactional
    public void approve(Long requestId, Long reviewedById, String reviewNote) {
        DeleteRequest r = deleteRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Delete request not found"));
        if (r.getStatus() != DeleteRequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }
        User reviewer = userRepository.findById(reviewedById)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found"));
        User toDelete = r.getUserToDelete();
        if (toDelete == null) {
            throw new IllegalStateException("Invalid request");
        }
        String email = toDelete.getEmail();
        String userName = toDelete.getFullName() != null && !toDelete.getFullName().isBlank()
                ? toDelete.getFullName()
                : email;

        r.setStatus(DeleteRequestStatus.APPROVED);
        r.setReviewedBy(reviewer);
        r.setReviewNote(reviewNote != null ? reviewNote.trim() : null);
        r.setReviewedAt(LocalDateTime.now());
        deleteRequestRepository.save(r);

        emailService.sendAccountDeletedEmail(email, userName, r.getReviewNote());

        userRepository.deleteById(toDelete.getId());
    }

    @Transactional
    public DeleteRequestDto reject(Long requestId, Long reviewedById, String reviewNote) {
        DeleteRequest r = deleteRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Delete request not found"));
        if (r.getStatus() != DeleteRequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }
        String note = reviewNote != null ? reviewNote.trim() : "";
        if (note.isEmpty()) {
            throw new IllegalArgumentException("A rejection reason is required");
        }
        User reviewer = userRepository.findById(reviewedById)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found"));
        User u = r.getUserToDelete();
        r.setStatus(DeleteRequestStatus.REJECTED);
        r.setReviewedBy(reviewer);
        r.setReviewNote(note);
        r.setReviewedAt(LocalDateTime.now());
        DeleteRequest saved = deleteRequestRepository.save(r);
        if (u != null && u.getEmail() != null) {
            String name = u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : u.getEmail();
            emailService.sendDeleteRequestRejectedEmail(u.getEmail(), name, note);
        }
        return DeleteRequestDto.from(saved);
    }

    @Transactional
    public void cancel(Long requestId, Long userId) {
        DeleteRequest r = deleteRequestRepository.findByIdAndUserToDeleteId(requestId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (r.getStatus() != DeleteRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be cancelled");
        }
        deleteRequestRepository.delete(r);
    }

    @Transactional
    public void deleteRecord(Long id) {
        if (!deleteRequestRepository.existsById(id)) {
            throw new IllegalArgumentException("Delete request not found");
        }
        deleteRequestRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getCounts() {
        Map<String, Long> m = new HashMap<>();
        m.put("pending", deleteRequestRepository.countByStatus(DeleteRequestStatus.PENDING));
        m.put("approved", deleteRequestRepository.countByStatus(DeleteRequestStatus.APPROVED));
        m.put("rejected", deleteRequestRepository.countByStatus(DeleteRequestStatus.REJECTED));
        return m;
    }
}
