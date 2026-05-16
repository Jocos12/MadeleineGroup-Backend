package rw.madeleinegroup.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.madeleinegroup.common.enums.PaymentStatus;
import rw.madeleinegroup.entity.Branch;
import rw.madeleinegroup.entity.Payment;
import rw.madeleinegroup.entity.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    List<Payment> findByBranch(Branch branch);

    List<Payment> findByBranchAndType(Branch branch, PaymentType type);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.branch.id = :branchId AND p.type = :type AND p.recordedAt BETWEEN :start AND :end")
    BigDecimal sumByBranchAndTypeAndDateRange(@Param("branchId") Long branchId, @Param("type") PaymentType type,
                                             @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.type = :type AND p.recordedAt BETWEEN :start AND :end")
    BigDecimal sumTotalByTypeAndDateRange(@Param("type") PaymentType type,
                                          @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.type = :type")
    BigDecimal sumTotalAmountByType(@Param("type") PaymentType type);

    List<Payment> findAllByOrderByRecordedAtDesc();
    List<Payment> findByBranch_IdOrderByRecordedAtDesc(Long branchId);
    List<Payment> findByBooking_IdOrderByRecordedAtDesc(Long bookingId);

    List<Payment> findByBooking_IdAndTypeOrderByRecordedAtAsc(Long bookingId, PaymentType type);

    @Query("SELECT p FROM Payment p WHERE p.booking.id IN :ids AND p.type = 'INCOME' ORDER BY p.booking.id ASC, p.recordedAt ASC")
    List<Payment> findIncomeByBookingIds(@Param("ids") java.util.Collection<Long> ids);

    @Query("SELECT FUNCTION('MONTH', p.recordedAt), SUM(p.amount) FROM Payment p WHERE p.type = 'INCOME' AND FUNCTION('YEAR', p.recordedAt) = :year GROUP BY FUNCTION('MONTH', p.recordedAt)")
    List<Object[]> findMonthlyIncome(@Param("year") int year);

    /**
     * Loads payments with associations (no DISTINCT): multiple {@code JOIN FETCH} on {@code ManyToOne}
     * paths do not multiply rows, and avoiding DISTINCT keeps MySQL happy with {@code ORDER BY recordedAt}.
     */
    @Query("SELECT p FROM Payment p LEFT JOIN FETCH p.booking b LEFT JOIN FETCH b.client LEFT JOIN FETCH p.client LEFT JOIN FETCH p.branch LEFT JOIN FETCH p.recordedBy LEFT JOIN FETCH p.updatedBy WHERE (:branchId IS NULL OR p.branch.id = :branchId) ORDER BY p.recordedAt DESC")
    List<Payment> findAllWithDetails(@Param("branchId") Long branchId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.type = 'INCOME' AND p.recordedAt BETWEEN :start AND :end")
    BigDecimal sumIncomeByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** Sum of all INCOME payment amounts (system-wide), matches dashboard "System Income (all payments)". */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.type = 'INCOME'")
    BigDecimal sumAllIncomePaymentAmounts();

    @Query("SELECT COALESCE(SUM(p.remainingBalance), 0) FROM Payment p WHERE p.paymentStatus IN ('PENDING', 'PARTIAL')")
    BigDecimal sumPendingAmount();

    /**
     * Matches Finance overview "Still to receive": for each booking, take the minimum {@code remainingBalance}
     * across INCOME payment rows (same logic as {@code FinancePage} client-side), then sum positive totals.
     * Not filtered by period — global outstanding receivable.
     */
    @Query(value = """
        SELECT COALESCE(SUM(agg.min_rem), 0)
        FROM (
            SELECT MIN(COALESCE(p.remaining_balance, 0)) AS min_rem
            FROM payments p
            WHERE p.type = 'INCOME' AND p.booking_id IS NOT NULL
            GROUP BY p.booking_id
        ) agg
        WHERE agg.min_rem > 0
        """, nativeQuery = true)
    BigDecimal sumOutstandingReceivableMinPerBooking();

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

    @Query("""
            SELECT b.id, b.name,
                COALESCE(SUM(CASE WHEN p.type = rw.madeleinegroup.entity.PaymentType.INCOME THEN p.amount ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN p.type = rw.madeleinegroup.entity.PaymentType.EXPENSE THEN p.amount ELSE 0 END), 0),
                COUNT(p)
            FROM Payment p JOIN p.branch b
            GROUP BY b.id, b.name
            ORDER BY (COALESCE(SUM(CASE WHEN p.type = rw.madeleinegroup.entity.PaymentType.INCOME THEN p.amount ELSE 0 END), 0)
                + COALESCE(SUM(CASE WHEN p.type = rw.madeleinegroup.entity.PaymentType.EXPENSE THEN p.amount ELSE 0 END), 0)) DESC
            """)
    List<Object[]> aggregatePaymentsByBranch();

    @Query("""
            SELECT FUNCTION('YEAR', p.recordedAt), FUNCTION('MONTH', p.recordedAt),
                COALESCE(SUM(CASE WHEN p.type = rw.madeleinegroup.entity.PaymentType.INCOME THEN p.amount ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN p.type = rw.madeleinegroup.entity.PaymentType.EXPENSE THEN p.amount ELSE 0 END), 0)
            FROM Payment p
            GROUP BY FUNCTION('YEAR', p.recordedAt), FUNCTION('MONTH', p.recordedAt)
            ORDER BY FUNCTION('YEAR', p.recordedAt) ASC, FUNCTION('MONTH', p.recordedAt) ASC
            """)
    List<Object[]> aggregatePaymentsByYearMonth();

    @Query("SELECT COALESCE(SUM(CASE WHEN p.type = rw.madeleinegroup.entity.PaymentType.INCOME THEN p.amount ELSE 0 END), 0) " +
           "- COALESCE(SUM(CASE WHEN p.type = rw.madeleinegroup.entity.PaymentType.EXPENSE THEN p.amount ELSE 0 END), 0) " +
           "FROM Payment p WHERE p.branch.id = :branchId")
    BigDecimal netBalanceForBranch(@Param("branchId") Long branchId);

    @Query("SELECT u.id, u.fullName, COUNT(p), COALESCE(SUM(p.amount), 0) FROM Payment p JOIN p.recordedBy u " +
           "GROUP BY u.id, u.fullName ORDER BY COUNT(p) DESC")
    List<Object[]> aggregatePaymentsByRecorder(org.springframework.data.domain.Pageable pageable);
}
