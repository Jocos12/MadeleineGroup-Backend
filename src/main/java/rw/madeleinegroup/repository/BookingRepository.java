package rw.madeleinegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.madeleinegroup.entity.Booking;
import rw.madeleinegroup.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {
    List<Booking> findByBranchId(Long branchId);
    List<Booking> findByClient_Id(Long clientId);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByBranchIdAndStatus(Long branchId, BookingStatus status);

    @Query("SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN FETCH b.client " +
            "LEFT JOIN FETCH b.branch " +
            "LEFT JOIN FETCH b.createdBy " +
            "LEFT JOIN FETCH b.bookingPackages bp " +
            "LEFT JOIN FETCH bp.packageItem")
    List<Booking> findAllWithDetails();

    @Query("SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN FETCH b.client " +
            "LEFT JOIN FETCH b.branch " +
            "LEFT JOIN FETCH b.createdBy " +
            "LEFT JOIN FETCH b.bookingPackages bp " +
            "LEFT JOIN FETCH bp.packageItem " +
            "WHERE b.branch.id = :branchId")
    List<Booking> findByBranchIdWithDetails(@Param("branchId") Long branchId);

    @Query("SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN FETCH b.client " +
            "LEFT JOIN FETCH b.branch " +
            "LEFT JOIN FETCH b.createdBy " +
            "LEFT JOIN FETCH b.bookingPackages bp " +
            "LEFT JOIN FETCH bp.packageItem " +
            "WHERE b.status = :status")
    List<Booking> findByStatusWithDetails(@Param("status") BookingStatus status);

    @Query("SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN FETCH b.client " +
            "LEFT JOIN FETCH b.branch " +
            "LEFT JOIN FETCH b.createdBy " +
            "LEFT JOIN FETCH b.bookingPackages bp " +
            "LEFT JOIN FETCH bp.packageItem " +
            "WHERE b.branch.id = :branchId AND b.status = :status")
    List<Booking> findByBranchIdAndStatusWithDetails(@Param("branchId") Long branchId, @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b " +
            "LEFT JOIN FETCH b.client " +
            "LEFT JOIN FETCH b.branch " +
            "LEFT JOIN FETCH b.createdBy " +
            "LEFT JOIN FETCH b.bookingPackages bp " +
            "LEFT JOIN FETCH bp.packageItem " +
            "WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT b FROM Booking b WHERE b.eventDate BETWEEN :start AND :end")
    List<Booking> findByEventDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT b FROM Booking b WHERE b.branch.id = :branchId AND b.eventDate BETWEEN :start AND :end")
    List<Booking> findByBranchAndDateRange(Long branchId, LocalDate start, LocalDate end);

    @Query("SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN FETCH b.client " +
            "LEFT JOIN FETCH b.branch " +
            "LEFT JOIN FETCH b.createdBy " +
            "LEFT JOIN FETCH b.bookingPackages bp " +
            "LEFT JOIN FETCH bp.packageItem " +
            "WHERE b.client.id = :clientId")
    List<Booking> findByClientIdWithDetails(@Param("clientId") Long clientId);

    @Query("SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN FETCH b.client " +
            "LEFT JOIN FETCH b.branch " +
            "LEFT JOIN FETCH b.createdBy " +
            "LEFT JOIN FETCH b.bookingPackages bp " +
            "LEFT JOIN FETCH bp.packageItem " +
            "WHERE b.eventDate BETWEEN :start AND :end")
    List<Booking> findByEventDateBetweenWithDetails(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT b.eventDate FROM Booking b WHERE b.eventDate BETWEEN :start AND :end AND b.status IN ('CONFIRMED', 'IN_PROGRESS', 'COMPLETED')")
    List<LocalDate> findEventDatesBetween(LocalDate start, LocalDate end);

    long countByStatus(BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.eventDate BETWEEN :start AND :end")
    long countByEventDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.eventDate BETWEEN :start AND :end AND b.status = :status")
    long countByEventDateBetweenAndStatus(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("status") BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.estimatedAmount - COALESCE(b.paidAmount, 0)), 0) FROM Booking b WHERE b.eventDate BETWEEN :start AND :end AND b.status IN ('PENDING', 'CONFIRMED')")
    BigDecimal sumPendingAmountByEventDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(b.estimatedAmount), 0) FROM Booking b WHERE b.status IN ('CONFIRMED', 'COMPLETED')")
    BigDecimal sumEstimatedAmountByConfirmedAndCompleted();

    /** Find Birthday bookings whose event date anniversary is today (same month and day, event in the past) */
    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.client WHERE b.eventType = 'Birthday' " +
            "AND FUNCTION('MONTH', b.eventDate) = :month AND FUNCTION('DAY', b.eventDate) = :day AND b.eventDate < :today")
    List<Booking> findBirthdayAnniversariesToday(@Param("month") int month, @Param("day") int day, @Param("today") LocalDate today);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.eventDate BETWEEN :start AND :end")
    int countByPeriod(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status AND b.eventDate BETWEEN :start AND :end")
    int countByStatusAndPeriod(@Param("status") BookingStatus status, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.eventDate < :today AND b.status IN ('CONFIRMED', 'IN_PROGRESS')")
    int countOverdueBookings(@Param("today") LocalDate today);

    /** Overdue bookings (event date in the past, CONFIRMED or IN_PROGRESS) with client loaded. */
    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.client LEFT JOIN FETCH b.branch " +
           "WHERE b.eventDate < :today AND b.status IN ('CONFIRMED', 'IN_PROGRESS')")
    List<Booking> findOverdueBookingsWithDetails(@Param("today") LocalDate today);

    /** Pending bookings created before the given cutoff (e.g. 3+ days ago). */
    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.client LEFT JOIN FETCH b.branch " +
           "WHERE b.status = 'PENDING' AND b.createdAt < :cutoff")
    List<Booking> findPendingBookingsCreatedBefore(@Param("cutoff") LocalDateTime cutoff);

    /** CONFIRMED or IN_PROGRESS with client loaded (filter in service for remaining balance > 0). */
    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.client LEFT JOIN FETCH b.branch " +
           "WHERE b.status IN ('CONFIRMED', 'IN_PROGRESS')")
    List<Booking> findConfirmedOrInProgressWithDetails();

    /** Bookings with remaining balance > 0, CONFIRMED or IN_PROGRESS, client+branch loaded, ordered by remaining balance descending. */
    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.client LEFT JOIN FETCH b.branch " +
           "WHERE b.status IN ('CONFIRMED', 'IN_PROGRESS') AND (b.estimatedAmount - COALESCE(b.paidAmount, 0)) > 0 " +
           "ORDER BY (b.estimatedAmount - COALESCE(b.paidAmount, 0)) DESC")
    List<Booking> findBookingsWithPendingBalanceOrderByRemainingDesc();

    /** Overdue bookings (event date in the past), ordered by event date ascending (oldest first). */
    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.client LEFT JOIN FETCH b.branch " +
           "WHERE b.eventDate < :today AND b.status IN ('CONFIRMED', 'IN_PROGRESS') ORDER BY b.eventDate ASC")
    List<Booking> findOverdueBookingsWithDetailsOrderByEventDateAsc(@Param("today") LocalDate today);

    /** Most recent bookings with client and branch, limit via Pageable (e.g. PageRequest.of(0, 10)). */
    @Query(value = "SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.client LEFT JOIN FETCH b.branch ORDER BY b.createdAt DESC",
           countQuery = "SELECT COUNT(b) FROM Booking b")
    org.springframework.data.domain.Page<Booking> findRecentBookingsWithDetails(org.springframework.data.domain.Pageable pageable);
}
