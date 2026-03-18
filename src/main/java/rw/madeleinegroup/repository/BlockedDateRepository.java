package rw.madeleinegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.madeleinegroup.entity.BlockedDate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlockedDateRepository extends JpaRepository<BlockedDate, Long> {
    List<BlockedDate> findAllByOrderByBlockedDateAsc();
    Optional<BlockedDate> findByBlockedDate(LocalDate date);
    boolean existsByBlockedDate(LocalDate date);
}
