package rw.madeleinegroup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rw.madeleinegroup.entity.Booking;
import rw.madeleinegroup.entity.OtpVerification;
import rw.madeleinegroup.repository.BookingRepository;
import rw.madeleinegroup.repository.OtpVerificationRepository;
import rw.madeleinegroup.service.EmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskService.class);

    private final OtpVerificationRepository otpRepository;
    private final NotificationService notificationService;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    public ScheduledTaskService(OtpVerificationRepository otpRepository,
                                NotificationService notificationService,
                                BookingRepository bookingRepository,
                                EmailService emailService) {
        this.otpRepository = otpRepository;
        this.notificationService = notificationService;
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
    }

    /** Clean expired OTPs every hour */
    @Scheduled(fixedRate = 3600000)
    public void cleanExpiredOtps() {
        try {
            List<rw.madeleinegroup.entity.OtpVerification> expired = otpRepository.findAll().stream()
                    .filter(o -> o.getExpiresAt().isBefore(LocalDateTime.now()))
                    .toList();
            otpRepository.deleteAll(expired);
            if (!expired.isEmpty()) {
                log.info("Cleaned {} expired OTPs", expired.size());
            }
        } catch (Exception e) {
            log.error("Error cleaning expired OTPs: {}", e.getMessage());
        }
    }

    /** Daily at 8:00 - Send reminders for bookings in 24h */
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendBookingReminders() {
        try {
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
            LocalDateTime tomorrowEnd = tomorrow.plusDays(1);
            // Notify clients and managers for bookings in next 24h
            notificationService.createAndBroadcast("Booking Reminder",
                    "You have a booking in the next 24 hours. Please confirm your attendance.",
                    rw.madeleinegroup.entity.Notification.NotificationType.SYSTEM_ALERT);
            log.info("Booking reminders sent");
        } catch (Exception e) {
            log.error("Error sending booking reminders: {}", e.getMessage());
        }
    }

    /** Daily at 8:00 - Send "Joyeux anniversaire" emails to clients on the anniversary of their Birthday ceremony */
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendBirthdayAnniversaryEmails() {
        try {
            LocalDate today = LocalDate.now();
            int month = today.getMonthValue();
            int day = today.getDayOfMonth();
            java.util.List<Booking> anniversaries = bookingRepository.findBirthdayAnniversariesToday(month, day, today);
            for (Booking b : anniversaries) {
                var client = b.getClient();
                if (client == null || client.getEmail() == null || client.getEmail().isBlank()) continue;
                int years = (int) ChronoUnit.YEARS.between(b.getEventDate(), today);
                if (years < 1) continue;
                emailService.sendBirthdayAnniversaryEmail(client.getEmail(), client.getFullName(), years);
            }
            if (!anniversaries.isEmpty()) {
                log.info("Sent {} birthday anniversary email(s)", anniversaries.size());
            }
        } catch (Exception e) {
            log.error("Error sending birthday anniversary emails: {}", e.getMessage());
        }
    }

    /** Every 6 hours - Notify managers about PENDING bookings older than 48h */
    @Scheduled(fixedRate = 21600000)
    public void notifyPendingBookings() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(48);
            long pendingCount = bookingRepository.findAll().stream()
                    .filter(b -> b.getStatus() == rw.madeleinegroup.entity.BookingStatus.PENDING)
                    .filter(b -> b.getCreatedAt() != null && b.getCreatedAt().isBefore(cutoff))
                    .count();
            if (pendingCount > 0) {
                notificationService.createAndBroadcast("Pending Bookings",
                        pendingCount + " booking(s) have been pending for more than 48 hours. Please review.",
                        rw.madeleinegroup.entity.Notification.NotificationType.SYSTEM_ALERT);
                log.info("Notified about {} pending bookings", pendingCount);
            }
        } catch (Exception e) {
            log.error("Error notifying pending bookings: {}", e.getMessage());
        }
    }
}
