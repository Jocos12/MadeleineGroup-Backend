package rw.madeleinegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.madeleinegroup.entity.Branch;

import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    Optional<Branch> findByCode(String code);
    boolean existsByCode(String code);
}
