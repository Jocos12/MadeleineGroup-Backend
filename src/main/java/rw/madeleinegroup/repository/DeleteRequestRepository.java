package rw.madeleinegroup.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.madeleinegroup.entity.DeleteRequest;
import rw.madeleinegroup.entity.DeleteRequestStatus;

import java.util.List;
import java.util.Optional;

public interface DeleteRequestRepository extends JpaRepository<DeleteRequest, Long> {

    @EntityGraph(attributePaths = {"userToDelete", "requestedBy", "reviewedBy"})
    List<DeleteRequest> findAllByOrderByRequestedAtDesc();

    @EntityGraph(attributePaths = {"userToDelete", "requestedBy", "reviewedBy"})
    List<DeleteRequest> findByStatusOrderByRequestedAtDesc(DeleteRequestStatus status);

    boolean existsByUserToDeleteIdAndStatus(Long userId, DeleteRequestStatus status);

    long countByStatus(DeleteRequestStatus status);

    @EntityGraph(attributePaths = {"userToDelete", "requestedBy", "reviewedBy"})
    Optional<DeleteRequest> findTopByUserToDeleteIdOrderByRequestedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"userToDelete", "requestedBy", "reviewedBy"})
    Optional<DeleteRequest> findByIdAndUserToDeleteId(Long id, Long userId);
}
