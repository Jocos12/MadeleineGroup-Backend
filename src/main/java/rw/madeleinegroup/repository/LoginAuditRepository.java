package rw.madeleinegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.madeleinegroup.entity.LoginAudit;

import java.util.List;

public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {

    List<LoginAudit> findFirst10ByOrderByLoggedAtDesc();
}
