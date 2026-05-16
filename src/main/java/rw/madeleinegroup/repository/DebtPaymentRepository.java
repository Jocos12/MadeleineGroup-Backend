package rw.madeleinegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.madeleinegroup.entity.DebtPayment;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DebtPaymentRepository extends JpaRepository<DebtPayment, Long> {

    List<DebtPayment> findByBookingIdOrderByCreatedAtDesc(Long bookingId);

    List<DebtPayment> findByBooking_IdOrderByPaymentDateAsc(Long bookingId);

    @Query("SELECT d FROM DebtPayment d JOIN FETCH d.booking b WHERE b.id IN :ids ORDER BY b.id ASC, d.paymentDate ASC, d.createdAt ASC")
    List<DebtPayment> findByBookingIdsWithBooking(@Param("ids") java.util.Collection<Long> ids);

    List<DebtPayment> findByClientIdOrderByCreatedAtDesc(Long clientId);
}