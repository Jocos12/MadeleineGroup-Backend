package rw.madeleinegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.madeleinegroup.entity.AiEmailAction;

import java.util.List;

@Repository
public interface AiEmailActionRepository extends JpaRepository<AiEmailAction, Long> {

    List<AiEmailAction> findTop20ByOrderByTriggeredAtDesc();
}
