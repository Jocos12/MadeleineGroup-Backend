package rw.madeleinegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.madeleinegroup.entity.Client;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Client> findByBranchId(Long branchId);
    List<Client> findByCreatedById(Long userId);

    @Query("SELECT COUNT(c) FROM Client c WHERE c.createdAt BETWEEN :start AND :end")
    long countNewByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
