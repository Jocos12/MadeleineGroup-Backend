package rw.madeleinegroup.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.madeleinegroup.common.enums.PaymentStatus;
import rw.madeleinegroup.entity.Branch;
import rw.madeleinegroup.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    List<Payment> findByBranch(Branch branch);

    List<Payment> findByBranchAndType(Branch branch, Payment.PaymentType type);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.branch.id = :branchId AND p.type = :type AND p.recordedAt BETWEEN :start AND :end")
    BigDecimal sumByBranchAndTypeAndDateRange(@Param("branchId") Long branchId, @Param("type") Payment.PaymentType type,
                                             @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.type = :type AND p.recordedAt BETWEEN :start AND :end")
    BigDecimal sumTotalByTypeAndDateRange(@Param("type") Payment.PaymentType type,
                                          @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Payment> findAllByOrderByRecordedAtDesc();
    List<Payment> findByBranch_IdOrderByRecordedAtDesc(Long branchId);
    List<Payment> findByBooking_IdOrderByRecordedAtDesc(Long bookingId);

    @Query("SELECT FUNCTION('MONTH', p.recordedAt), SUM(p.amount) FROM Payment p WHERE p.type = 'INCOME' AND FUNCTION('YEAR', p.recordedAt) = :year GROUP BY FUNCTION('MONTH', p.recordedAt)")
    List<Object[]> findMonthlyIncome(@Param("year") int year);

    @Query("SELECT DISTINCT p FROM Payment p LEFT JOIN FETCH p.booking b LEFT JOIN FETCH b.client LEFT JOIN FETCH p.client LEFT JOIN FETCH p.branch LEFT JOIN FETCH p.recordedBy LEFT JOIN FETCH p.updatedBy WHERE (:branchId IS NULL OR p.branch.id = :branchId) ORDER BY p.recordedAt DESC")
    List<Payment> findAllWithDetails(@Param("branchId") Long branchId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.type = 'INCOME' AND p.recordedAt BETWEEN :start AND :end")
    BigDecimal sumIncomeByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(p.remainingBalance), 0) FROM Payment p WHERE p.paymentStatus IN ('PENDING', 'PARTIAL')")
    BigDecimal sumPendingAmount();

    @Query("SELECT COALESCE(p.branch.name, 'N/A'), COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.type = 'INCOME' AND p.recordedAt BETWEEN :start AND :end GROUP BY p.branch.id, p.branch.name ORDER BY SUM(p.amount) DESC")
    List<Object[]> getIncomeByBranch(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT p.client.fullName, COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.type = 'INCOME' AND p.recordedAt BETWEEN :start AND :end AND p.client IS NOT NULL GROUP BY p.client.id, p.client.fullName ORDER BY SUM(p.amount) DESC")
    List<Object[]> getTopClientsByRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    default List<Object[]> getTopClientsByRevenue(LocalDateTime start, LocalDateTime end, int limit) {
        return getTopClientsByRevenue(start, end, org.springframework.data.domain.PageRequest.of(0, limit));
    }

    /** Payments with status PENDING or PARTIAL, with booking and client loaded, ordered by remaining balance descending. */
    @Query("SELECT p FROM Payment p LEFT JOIN FETCH p.booking LEFT JOIN FETCH p.client LEFT JOIN FETCH p.branch " +
           "WHERE p.paymentStatus IN (:s1, :s2) ORDER BY COALESCE(p.remainingBalance, 0) DESC")
    List<Payment> findPaymentsWithPendingOrPartialWithDetails(@Param("s1") PaymentStatus s1, @Param("s2") PaymentStatus s2);
}
