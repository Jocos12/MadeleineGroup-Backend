package rw.madeleinegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.madeleinegroup.common.enums.ExpenseStatus;
import rw.madeleinegroup.entity.Branch;
import rw.madeleinegroup.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@org.springframework.stereotype.Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByBranch(Branch branch);
    List<Expense> findByBranch_Id(Long branchId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.branch.id = :branchId AND e.expenseDate BETWEEN :start AND :end AND (e.status IS NULL OR e.status IN (rw.madeleinegroup.common.enums.ExpenseStatus.FIRST_APPROVED, rw.madeleinegroup.common.enums.ExpenseStatus.PAID))")
    BigDecimal sumByBranchAndDateRange(@Param("branchId") Long branchId,
                                       @Param("start") LocalDate start,
                                       @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.expenseDate BETWEEN :start AND :end AND (e.status IS NULL OR e.status IN (rw.madeleinegroup.common.enums.ExpenseStatus.FIRST_APPROVED, rw.madeleinegroup.common.enums.ExpenseStatus.PAID))")
    BigDecimal sumTotalByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    /** All expense-module rows; matches Finance overview "System Expenses (all expenses)" total. */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
    BigDecimal sumAllExpenseAmounts();

    List<Expense> findAllByOrderByCreatedAtDesc();
    List<Expense> findByBranch_IdOrderByCreatedAtDesc(Long branchId);

    long countByStatus(ExpenseStatus status);

    @Query("SELECT e FROM Expense e WHERE (:branchId IS NULL OR e.branch.id = :branchId) AND (:status IS NULL OR e.status = :status) ORDER BY e.createdAt DESC")
    List<Expense> findForList(@Param("branchId") Long branchId, @Param("status") ExpenseStatus status);

    @Query("SELECT FUNCTION('MONTH', e.expenseDate), SUM(e.amount) FROM Expense e WHERE FUNCTION('YEAR', e.expenseDate) = :year AND (e.status IS NULL OR e.status IN (rw.madeleinegroup.common.enums.ExpenseStatus.FIRST_APPROVED, rw.madeleinegroup.common.enums.ExpenseStatus.PAID)) GROUP BY FUNCTION('MONTH', e.expenseDate)")
    List<Object[]> findMonthlyExpenses(@Param("year") int year);

    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE (:year IS NULL OR FUNCTION('YEAR', e.expenseDate) = :year) AND (e.status IS NULL OR e.status IN (rw.madeleinegroup.common.enums.ExpenseStatus.FIRST_APPROVED, rw.madeleinegroup.common.enums.ExpenseStatus.PAID)) GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> findExpensesByCategory(@Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.expenseDate BETWEEN :start AND :end AND (e.status IS NULL OR e.status IN (rw.madeleinegroup.common.enums.ExpenseStatus.FIRST_APPROVED, rw.madeleinegroup.common.enums.ExpenseStatus.PAID))")
    BigDecimal sumExpensesByPeriod(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT e.category, COALESCE(SUM(e.amount), 0) FROM Expense e WHERE FUNCTION('YEAR', e.expenseDate) = :year AND (:month IS NULL OR FUNCTION('MONTH', e.expenseDate) = :month) AND (e.status IS NULL OR e.status IN (rw.madeleinegroup.common.enums.ExpenseStatus.FIRST_APPROVED, rw.madeleinegroup.common.enums.ExpenseStatus.PAID)) GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> getExpensesByCategory(@Param("year") int year, @Param("month") Integer month);

    @Query("SELECT e FROM Expense e WHERE e.expenseDate BETWEEN :start AND :end AND e.amount >= :threshold ORDER BY e.amount DESC")
    List<Expense> findLargeExpenses(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("threshold") BigDecimal threshold);

    @Query("SELECT e FROM Expense e WHERE e.status IN (rw.madeleinegroup.common.enums.ExpenseStatus.PENDING_APPROVAL, rw.madeleinegroup.common.enums.ExpenseStatus.PENDING_FIRST_APPROVAL) ORDER BY e.createdAt ASC")
    List<Expense> findAllPendingApproval();
}
