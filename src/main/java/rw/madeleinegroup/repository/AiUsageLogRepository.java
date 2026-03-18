package rw.madeleinegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.madeleinegroup.entity.AiUsageLog;

import java.time.LocalDateTime;
import java.util.List;

public interface AiUsageLogRepository extends JpaRepository<AiUsageLog, Long> {

    List<AiUsageLog> findTop50ByOrderByCreatedAtDesc();

    long countByUser_IdAndCreatedAtAfter(Long userId, LocalDateTime after);
}
