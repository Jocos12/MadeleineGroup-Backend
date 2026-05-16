package rw.madeleinegroup.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.madeleinegroup.entity.LoginAudit;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {

    @Query("""
            SELECT l FROM LoginAudit l
            WHERE (:role IS NULL OR l.role = :role)
              AND (:from IS NULL OR l.loggedAt >= :from)
              AND (:to IS NULL OR l.loggedAt <= :to)
              AND (:q IS NULL
                   OR LOWER(COALESCE(l.email, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(COALESCE(l.fullName, '')) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY l.loggedAt DESC
            """)
    List<LoginAudit> findRecentFiltered(
            @Param("role") String role,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("q") String q,
            Pageable pageable
    );
}
