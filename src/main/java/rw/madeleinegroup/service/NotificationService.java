package rw.madeleinegroup.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import rw.madeleinegroup.entity.Booking;
import rw.madeleinegroup.entity.ClientExperience;
import rw.madeleinegroup.entity.Notification;
import rw.madeleinegroup.entity.User;
import rw.madeleinegroup.repository.NotificationRepository;
import rw.madeleinegroup.repository.UserRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository,
                              EmailService emailService, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.messagingTemplate = messagingTemplate;
    }

    public void createAndBroadcast(String title, String message, Notification.NotificationType type) {
        Notification n = Notification.builder()
                .title(title)
                .message(message)
                .type(type)
                .read(false)
                .build();
        n = notificationRepository.save(n);
        broadcastNotification(n);
        sendEmailToCEO(title, message);
    }

    public void notifyNewBooking(Booking booking) {
        String msg = "New booking " + booking.getBookingReference() + " for " + booking.getEventType() + " on " + booking.getEventDate();
        Notification n = Notification.builder()
                .title("New Booking")
                .message(msg)
                .type(Notification.NotificationType.BOOKING_CREATED)
                .read(false)
                .build();
        n = notificationRepository.save(n);
        broadcastNotification(n);

        if (booking.getClient() != null && booking.getClient().getEmail() != null) {
            String clientName = booking.getClient().getFullName();
            String packagesText = buildPackagesText(booking);
            String totalRwf = booking.getEstimatedAmount() != null ? booking.getEstimatedAmount().toString() : "0";
            emailService.sendBilingualBookingSubmissionEmail(
                    booking.getClient().getEmail(),
                    clientName,
                    booking.getBookingReference(),
                    booking.getEventDate() != null ? booking.getEventDate().toString() : "",
                    booking.getEventType() != null ? booking.getEventType() : "",
                    booking.getGuestCount() != null ? booking.getGuestCount() : 0,
                    packagesText,
                    totalRwf
            );
        }
        userRepository.findByRole(rw.madeleinegroup.entity.Role.CEO)
                .forEach(ceo -> emailService.sendInternalNewBookingAlert(
                        ceo.getEmail(),
                        booking.getClient() != null ? booking.getClient().getFullName() : "Unknown",
                        booking.getBookingReference(),
                        booking.getEventDate() != null ? booking.getEventDate().toString() : "",
                        buildPackagesSummary(booking),
                        booking.getEstimatedAmount() != null ? booking.getEstimatedAmount().toString() : "0"
                ));
        userRepository.findByRole(rw.madeleinegroup.entity.Role.ADMIN)
                .forEach(admin -> emailService.sendInternalNewBookingAlert(
                        admin.getEmail(),
                        booking.getClient() != null ? booking.getClient().getFullName() : "Unknown",
                        booking.getBookingReference(),
                        booking.getEventDate() != null ? booking.getEventDate().toString() : "",
                        buildPackagesSummary(booking),
                        booking.getEstimatedAmount() != null ? booking.getEstimatedAmount().toString() : "0"
                ));
    }

    public void notifyNewClientExperience(ClientExperience exp) {
        String msg = "New client experience from " + exp.getAuthorName() + " - pending approval";
        createAndBroadcast("New Client Experience", msg, Notification.NotificationType.CLIENT_EXPERIENCE_SUBMITTED);
    }

    public void notifyExperienceApproved(ClientExperience exp) {
        createAndBroadcast("Experience Approved", "Client experience from " + exp.getAuthorName() + " has been approved", Notification.NotificationType.CLIENT_EXPERIENCE_APPROVED);
    }

    public void notifyDeleteRequest(User targetUser, User requestedBy) {
        String msg = "Delete request for user " + targetUser.getEmail() + " by " + requestedBy.getEmail();
        createAndBroadcast("Delete Request", msg, Notification.NotificationType.USER_DELETE_REQUEST);
    }

    private void broadcastNotification(Notification n) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", n.getId());
            payload.put("title", n.getTitle());
            payload.put("message", n.getMessage());
            payload.put("type", n.getType());
            payload.put("createdAt", n.getCreatedAt());
            messagingTemplate.convertAndSend("/topic/notifications", payload);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(NotificationService.class).warn("WebSocket broadcast failed: {}", e.getMessage());
        }
    }

    private void sendEmailToCEO(String title, String message) {
        userRepository.findByRole(rw.madeleinegroup.entity.Role.CEO)
                .forEach(ceo -> emailService.sendNotificationEmail(ceo.getEmail(), title, message));
    }

    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findByReadFalseOrderByCreatedAtDesc();
    }

    public List<Notification> getNotificationsForUser(User user, int limit) {
        var pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        if (user == null) {
            return notificationRepository.findFirst50ByOrderByCreatedAtDesc();
        }
        return notificationRepository.findForUserOrderByCreatedAtDesc(user, pageable);
    }

    public long getUnreadCountForUser(User user) {
        if (user == null) {
            return notificationRepository.countByReadFalse();
        }
        return notificationRepository.countUnreadForUser(user);
    }

    public void markAllAsReadForUser(User user) {
        notificationRepository.markAllAsReadByUser(user);
    }

    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    public void notifyUserCreated(User user, User creator) {
        createAndBroadcast("User Created", "New user " + user.getEmail() + " was created by " + creator.getEmail(), Notification.NotificationType.SYSTEM_ALERT);
    }

    public void notifyUserUpdated(User user, User updater) {
        createAndBroadcast("User Updated", "User " + user.getEmail() + " was updated by " + updater.getEmail(), Notification.NotificationType.SYSTEM_ALERT);
    }

    public void notifyUserDeleted(User user, User deleter) {
        createAndBroadcast("User Deleted", "User " + user.getEmail() + " was deleted by " + deleter.getEmail(), Notification.NotificationType.USER_DELETE_APPROVED);
    }

    public void notifyDeleteRequested(rw.madeleinegroup.entity.DeleteRequest deleteRequest) {
        createAndBroadcast("Delete Requested", "Delete request for user " + deleteRequest.getUserToDelete().getEmail(), Notification.NotificationType.USER_DELETE_REQUEST);
    }

    public void notifyDeleteRejected(rw.madeleinegroup.entity.DeleteRequest dr) {
        createAndBroadcast("Delete Rejected", "Delete request for " + dr.getUserToDelete().getEmail() + " was rejected", Notification.NotificationType.SYSTEM_ALERT);
    }

    public void notifyPaymentRecorded(rw.madeleinegroup.entity.Payment payment, User recordedBy) {
        createAndBroadcast("Payment Recorded", "Payment of " + payment.getAmount() + " recorded by " + recordedBy.getEmail(), Notification.NotificationType.PAYMENT_RECORDED);
    }

    /** Called when booking is confirmed via the payment flow. Sends custom full/partial payment emails. */
    public void notifyBookingConfirmed(Booking booking, boolean fullPayment, BigDecimal paidAmount, BigDecimal remainingBalance) {
        String msg = fullPayment
                ? "Booking " + booking.getBookingReference() + " confirmed. Client paid full amount: " + paidAmount + " RWF."
                : "Booking " + booking.getBookingReference() + " confirmed. Partial payment: " + paidAmount + " RWF. Remaining: " + remainingBalance + " RWF.";
        createAndBroadcast("Booking Confirmed", msg, Notification.NotificationType.BOOKING_CONFIRMED);

        if (booking.getClient() != null && booking.getClient().getEmail() != null && !booking.getClient().getEmail().isBlank()) {
            String clientName = booking.getClient().getFullName() != null ? booking.getClient().getFullName() : "Client";
            String packagesText = buildPackagesText(booking);
            String totalRwf = booking.getEstimatedAmount() != null ? booking.getEstimatedAmount().toString() : "0";
            if (fullPayment) {
                emailService.sendConfirmationEmailFullPayment(
                        booking.getClient().getEmail(),
                        clientName,
                        booking.getBookingReference(),
                        booking.getEventDate(),
                        booking.getEventType() != null ? booking.getEventType() : "",
                        booking.getGuestCount() != null ? booking.getGuestCount() : 0,
                        packagesText,
                        totalRwf);
            } else {
                emailService.sendConfirmationEmailPartialPayment(
                        booking.getClient().getEmail(),
                        clientName,
                        booking.getBookingReference(),
                        booking.getEventDate(),
                        booking.getEventType() != null ? booking.getEventType() : "",
                        booking.getGuestCount() != null ? booking.getGuestCount() : 0,
                        packagesText,
                        totalRwf,
                        paidAmount != null ? paidAmount.toString() : "0",
                        remainingBalance != null ? remainingBalance.toString() : "0");
            }
        }
    }

    public void notifyBookingStatusUpdated(Booking booking) {
        createAndBroadcast("Booking Status Updated", "Booking " + booking.getBookingReference() + " status changed to " + booking.getStatus(), Notification.NotificationType.BOOKING_CONFIRMED);

        if (booking.getClient() != null && booking.getClient().getEmail() != null && !booking.getClient().getEmail().isBlank()) {
            String packagesText = buildPackagesText(booking);
            String totalRwf = booking.getEstimatedAmount() != null ? booking.getEstimatedAmount().toString() : "0";
            String modifiedBy = booking.getLastModifiedBy();
            String modifiedDate = booking.getUpdatedAt() != null
                    ? booking.getUpdatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
                    : "";
            String statusStr = booking.getStatus() != null ? booking.getStatus().name() : "";
            emailService.sendBilingualBookingStatusChangedEmail(
                    booking.getClient().getEmail(),
                    booking.getClient().getFullName() != null ? booking.getClient().getFullName() : "Client",
                    booking.getBookingReference(),
                    booking.getEventDate() != null ? booking.getEventDate().toString() : "",
                    booking.getEventType() != null ? booking.getEventType() : "",
                    booking.getGuestCount() != null ? booking.getGuestCount() : 0,
                    packagesText,
                    totalRwf,
                    statusStr,
                    modifiedBy,
                    modifiedDate
            );
        }
    }

    private String buildPackagesText(rw.madeleinegroup.entity.Booking booking) {
        if (booking.getBookingPackages() == null || booking.getBookingPackages().isEmpty()) {
            return "No packages selected";
        }
        StringBuilder sb = new StringBuilder();
        for (rw.madeleinegroup.entity.BookingPackage bp : booking.getBookingPackages()) {
            String name = bp.getPackageItem() != null ? bp.getPackageItem().getName() : "Package";
            String price = bp.getTotalPrice() != null ? bp.getTotalPrice().toString() : bp.getUnitPrice() != null ? bp.getUnitPrice().toString() : "0";
            sb.append("- ").append(name).append(": ").append(price).append(" RWF\n");
        }
        return sb.toString();
    }

    private String buildPackagesSummary(rw.madeleinegroup.entity.Booking booking) {
        if (booking.getBookingPackages() == null || booking.getBookingPackages().isEmpty()) {
            return "None";
        }
        StringBuilder sb = new StringBuilder();
        for (rw.madeleinegroup.entity.BookingPackage bp : booking.getBookingPackages()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(bp.getPackageItem() != null ? bp.getPackageItem().getName() : "Package");
        }
        return sb.toString();
    }

    public void notifyBookingUpdated(Booking booking) {
        createAndBroadcast("Booking Updated", "Booking " + booking.getBookingReference() + " was updated (packages or details changed)", Notification.NotificationType.BOOKING_CONFIRMED);
    }

    public void notifyBookingCancelled(Booking booking) {
        createAndBroadcast("Booking Cancelled", "Booking " + booking.getBookingReference() + " was cancelled", Notification.NotificationType.SYSTEM_ALERT);
    }
}
