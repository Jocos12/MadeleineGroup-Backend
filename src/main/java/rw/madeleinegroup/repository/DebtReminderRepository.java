package rw.madeleinegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.madeleinegroup.entity.DebtReminder;

import java.util.List;

public interface DebtReminderRepository extends JpaRepository<DebtReminder, Long> {

    List<DebtReminder> findByBookingIdOrderBySentAtDesc(Long bookingId);

    List<DebtReminder> findByClientIdOrderBySentAtDesc(Long clientId);
}
