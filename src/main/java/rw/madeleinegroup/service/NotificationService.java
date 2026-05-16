package rw.madeleinegroup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.madeleinegroup.entity.Booking;
import rw.madeleinegroup.entity.ClientExperience;
import rw.madeleinegroup.entity.Expense;
import rw.madeleinegroup.entity.Notification;
import rw.madeleinegroup.entity.Payment;
import rw.madeleinegroup.entity.PaymentType;
import rw.madeleinegroup.entity.Role;
import rw.madeleinegroup.entity.User;
import rw.madeleinegroup.repository.BookingRepository;
import rw.madeleinegroup.repository.NotificationRepository;
import rw.madeleinegroup.repository.UserRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.EnumSet;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository,
                              BookingRepository bookingRepository,
                              EmailService emailService, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
        this.messagingTemplate = messagingTemplate;
    }

    public void createAndBroadcast(String title, String message, Notification.NotificationType type) {
        createAndBroadcast(title, message, type, true);
    }

    /**
     * Creates in-app notifications for CEO/ADMIN/MANAGER. Optionally sends the legacy plain-text email to CEOs.
     */
    public void createAndBroadcast(String title, String message, Notification.NotificationType type, boolean sendCeoPlainEmail) {
        createForRolesAndBroadcast(title, message, type, EnumSet.of(Role.CEO, Role.ADMIN, Role.MANAGER));
        if (sendCeoPlainEmail) {
            sendEmailToCEO(title, message);
        }
    }

    private void createForRolesAndBroadcast(String title, String message, Notification.NotificationType type, EnumSet<Role> roles) {
        for (Role role : roles) {
            userRepository.findByRole(role).forEach(user -> {
                Notification n = Notification.builder()
                        .user(user)
                        .title(title)
                        .message(message)
                        .type(type)
                        .read(false)
                        .build();
                n = notificationRepository.save(n);
                broadcastNotification(n);
            });
        }
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
        String msg = "[Account delete request] User " + targetUser.getEmail() + " — requested by " + requestedBy.getEmail() + ".";
        createAndBroadcast("Delete Request", msg, Notification.NotificationType.SYSTEM_ALERT);
    }

    /** Large expense recorded by a manager — needs CEO / admin / another approver. */
    public void notifyExpensePendingApproval(Expense expense) {
        String amount = expense.getAmount() != null ? expense.getAmount().toPlainString() : "?";
        String desc = expense.getDescription() != null ? expense.getDescription() : "—";
        String branch = expense.getBranch() != null && expense.getBranch().getName() != null
                ? expense.getBranch().getName() : "—";
        String statusLabel = expense.getEffectiveStatus() != null ? expense.getEffectiveStatus().name() : "PENDING";
        String msg = String.format(
                "[Expense] #%d of %s RWF (%s) — branch %s — status %s — pending approval (auto-approve limit %s RWF).",
                expense.getId(), amount, desc, branch, statusLabel, Expense.CEO_AUTO_APPROVE_MAX_RWF.toPlainString());
        createAndBroadcast("Expense approval required", msg, Notification.NotificationType.SYSTEM_ALERT, false);
        userRepository.findByRole(Role.CEO).forEach(ceo ->
                emailService.sendExpenseApprovalRequiredEmail(ceo.getEmail(), expense, amount, desc, branch, statusLabel));
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
        return getNotificationsForUser(user, limit, "desc", "all");
    }

    /**
     * @param sort "asc" or "desc" (createdAt)
     * @param filter "all", "unread", or "read"
     */
    public List<Notification> getNotificationsForUser(User user, int limit, String sort, String filter) {
        if (user == null) {
            return List.of();
        }
        int cap = Math.min(Math.max(limit, 5), 300);
        org.springframework.data.domain.Sort.Direction dir =
                "asc".equalsIgnoreCase(sort)
                        ? org.springframework.data.domain.Sort.Direction.ASC
                        : org.springframework.data.domain.Sort.Direction.DESC;
        var pageable = org.springframework.data.domain.PageRequest.of(0, cap,
                org.springframework.data.domain.Sort.by(dir, "createdAt"));
        List<Notification> list = notificationRepository.findByUser(user, pageable);
        if ("unread".equalsIgnoreCase(filter)) {
            return list.stream().filter(n -> Boolean.FALSE.equals(n.getRead())).toList();
        }
        if ("read".equalsIgnoreCase(filter)) {
            return list.stream().filter(n -> Boolean.TRUE.equals(n.getRead())).toList();
        }
        return list;
    }

    public long getUnreadCountForUser(User user) {
        if (user == null) return 0;
        return notificationRepository.countByUserAndReadFalse(user);
    }

    @Transactional
    public void markAllAsReadForUser(User user) {
        notificationRepository.markAllAsReadByUser(user);
    }

    public void markAsRead(Long id, User user) {
        if (user == null) return;
        notificationRepository.findByIdAndUser(id, user).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void deleteForUser(Long id, User user) {
        if (user == null) return;
        notificationRepository.deleteByIdAndUser(id, user);
    }

    @Transactional
    public void deleteReadForUser(User user) {
        if (user == null) return;
        notificationRepository.deleteByUserAndReadTrue(user);
    }

    public void notifyUserCreated(User user, User creator) {
        createAndBroadcast("User Created", "New user " + user.getEmail() + " was created by " + creator.getEmail(), Notification.NotificationType.SYSTEM_ALERT);
    }

    public void notifyUserUpdated(User user, User updater) {
        createAndBroadcast("User Updated", "User " + user.getEmail() + " was updated by " + updater.getEmail(), Notification.NotificationType.SYSTEM_ALERT);
    }

    public void notifyUserDeleted(User user, User deleter) {
        String msg = "[User deleted] " + user.getEmail() + " — deleted by " + deleter.getEmail() + ".";
        createAndBroadcast("User Deleted", msg, Notification.NotificationType.SYSTEM_ALERT);
    }

    public void notifyDeleteRequested(rw.madeleinegroup.entity.DeleteRequest deleteRequest) {
        String email = deleteRequest.getUserToDelete() != null ? deleteRequest.getUserToDelete().getEmail() : "—";
        String msg = "[Account delete requested] User " + email + " — awaiting review.";
        createAndBroadcast("Delete Requested", msg, Notification.NotificationType.SYSTEM_ALERT);
    }

    public void notifyDeleteRejected(rw.madeleinegroup.entity.DeleteRequest dr) {
        createAndBroadcast("Delete Rejected", "Delete request for " + dr.getUserToDelete().getEmail() + " was rejected", Notification.NotificationType.SYSTEM_ALERT);
    }

    public void notifyPaymentRecorded(Payment payment, User recordedBy) {
        Booking b = null;
        if (payment.getBooking() != null && payment.getBooking().getId() != null) {
            b = bookingRepository.findByIdWithDetails(payment.getBooking().getId()).orElse(null);
        }
        String ref = b != null && b.getBookingReference() != null ? b.getBookingReference() : "—";
        StringBuilder detail = new StringBuilder();
        detail.append("Income ").append(payment.getAmount().toPlainString()).append(" RWF — booking ").append(ref);
        detail.append(" — by ").append(recordedBy.getEmail());
        if (payment.getRemainingBalance() != null) {
            detail.append(". Remaining: ").append(payment.getRemainingBalance().toPlainString()).append(" RWF");
        }
        createAndBroadcast("Payment received", detail.toString(), Notification.NotificationType.PAYMENT_RECORDED);

        if (payment.getType() == PaymentType.INCOME && b != null && b.getClient() != null) {
            String email = b.getClient().getEmail();
            if (email != null && !email.isBlank()) {
                try {
                    java.time.LocalDate pdate = payment.getRecordedAt() != null
                            ? payment.getRecordedAt().toLocalDate() : java.time.LocalDate.now();
                    BigDecimal remaining = payment.getRemainingBalance() != null
                            ? payment.getRemainingBalance() : BigDecimal.ZERO;
                    boolean fullyPaid = remaining.compareTo(BigDecimal.ZERO) <= 0;
                    String pm = payment.getPaymentMethod() != null ? payment.getPaymentMethod().name().replace('_', ' ') : "";
                    emailService.sendPaymentReceivedAcknowledgmentSync(email.trim(),
                            b.getClient().getFullName(), ref, payment.getAmount(), pm, pdate, remaining, fullyPaid);
                } catch (Exception e) {
                    log.warn("Could not send payment acknowledgement email to client: {}", e.getMessage());
                }
            }
        }
    }

    /** After a debt installment on the Debts dashboard — in-app + CEO email + client acknowledgement. */
    public void notifyDebtPaymentRecorded(rw.madeleinegroup.entity.DebtPayment dp, Booking booking, User recordedBy) {
        String ref = booking.getBookingReference() != null ? booking.getBookingReference() : "—";
        String clientName = booking.getClient() != null && booking.getClient().getFullName() != null
                ? booking.getClient().getFullName() : "Client";
        BigDecimal est = booking.getEstimatedAmount() != null ? booking.getEstimatedAmount() : BigDecimal.ZERO;
        BigDecimal paid = booking.getPaidAmount() != null ? booking.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal remaining = est.subtract(paid).max(BigDecimal.ZERO);
        boolean fullyPaid = est.compareTo(BigDecimal.ZERO) > 0 && paid.compareTo(est) >= 0;

        String msg = String.format("Debt payment %s RWF — %s (%s) — by %s. Remaining: %s RWF.",
                dp.getAmount().toPlainString(), ref, clientName, recordedBy.getEmail(), remaining.toPlainString());
        createAndBroadcast("Debt payment received", msg, Notification.NotificationType.PAYMENT_RECORDED);

        if (booking.getClient() != null) {
            String email = booking.getClient().getEmail();
            if (email != null && !email.isBlank()) {
                try {
                    String pm = dp.getPaymentMethod() != null ? dp.getPaymentMethod() : "";
                    emailService.sendPaymentReceivedAcknowledgmentSync(email.trim(), clientName, ref, dp.getAmount(),
                            pm, dp.getPaymentDate(), remaining, fullyPaid);
                } catch (Exception e) {
                    log.warn("Could not send debt payment acknowledgement email: {}", e.getMessage());
                }
            }
        }
    }

    /** Summary after sending invoice emails from the Invoices page. */
    public void notifyInvoiceBatchSent(int sent, int requested, String senderLabel) {
        createAndBroadcast("Invoices sent",
                String.format("%d of %d invoice email(s) delivered. Sent by %s.", sent, requested, senderLabel),
                Notification.NotificationType.SYSTEM_ALERT);
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
