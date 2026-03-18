package rw.madeleinegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.madeleinegroup.entity.Branch;
import rw.madeleinegroup.entity.Department;
import rw.madeleinegroup.entity.PackageItem;

import java.util.List;

public interface PackageItemRepository extends JpaRepository<PackageItem, Long> {

    List<PackageItem> findByBranch(Branch branch);

    List<PackageItem> findByBranchAndCategory(Branch branch, String category);

    List<PackageItem> findByDepartment(Department department);

    List<PackageItem> findByDepartmentId(Long departmentId);

    /** Packages valid for guestCount: (minGuests is null or guestCount >= minGuests) AND (maxGuests is null or guestCount <= maxGuests) */
    @Query("SELECT p FROM PackageItem p WHERE p.department.id = :departmentId " +
            "AND (p.minGuests IS NULL OR :guestCount >= p.minGuests) " +
            "AND (p.maxGuests IS NULL OR :guestCount <= p.maxGuests)")
    List<PackageItem> findByDepartmentIdAndGuestCount(@Param("departmentId") Long departmentId, @Param("guestCount") int guestCount);
}
