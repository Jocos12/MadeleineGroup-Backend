package rw.madeleinegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.madeleinegroup.entity.Department;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findAllByOrderByNameAsc();
    Optional<Department> findByCode(String code);
}
