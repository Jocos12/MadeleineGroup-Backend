package rw.madeleinegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.madeleinegroup.entity.DeleteRequest;
import rw.madeleinegroup.entity.DeleteRequest.DeleteRequestStatus;

import java.util.List;
import java.util.Optional;

public interface DeleteRequestRepository extends JpaRepository<DeleteRequest, Long> {

    List<DeleteRequest> findByStatus(DeleteRequestStatus status);

    Optional<DeleteRequest> findByUserToDeleteIdAndStatus(Long userId, DeleteRequestStatus status);
}
