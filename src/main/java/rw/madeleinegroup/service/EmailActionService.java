package rw.madeleinegroup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rw.madeleinegroup.entity.Booking;
import rw.madeleinegroup.repository.AiEmailActionRepository;
import rw.madeleinegroup.repository.BookingRepository;
import rw.madeleinegroup.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailActionService {

    private static final Logger log = LoggerFactory.getLogger(EmailActionService.class);
    private static final String CONTACT_INFO = "Contact Madeleine Group for any questions.";
    private static final String CONTACT_FR = "Contactez Madeleine Group pour toute question.";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BookingRepository bookingRepository;
    private final EmailService emailService;
    private final AiEmailActionRepository aiEmailActionRepository;
    private final UserRepository userRepository;

    public EmailActionService(BookingRepository bookingRepository,
                              EmailService emailService,
                              AiEmailActionRepository aiEmailActionRepository,
                              UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
        this.aiEmailActionRepository = aiEmailActionRepository;
        this.userRepository = userRepository;
    }

    /** Summary for payment reminders: emailsSent, totalAmountRwf, clientNames. */
    public static class PaymentReminderSummary {
        public int emailsSent;
        public BigDecimal totalAmountRwf;
        public List<String> clientNames = new ArrayList<>();
    }

    /** Summary for overdue or pending follow-up: emailsSent only. */
    public static class SimpleSummary {
        public int emailsSent;
    }

    public PaymentReminderSummary sendPaymentReminders(Long triggeredByUserId) {
        PaymentReminderSummary summary = new PaymentReminderSummary();
        summary.totalAmountRwf = BigDecimal.ZERO;
        List<Booking> list = bookingRepository.findConfirmedOrInProgressWithDetails();
        for (Booking b : list) {
            BigDecimal remaining = (b.getEstimatedAmount() != null ? b.getEstimatedAmount() : BigDecimal.ZERO)
                .subtract(b.getPaidAmount() != null ? b.getPaidAmount() : BigDecimal.ZERO);
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) continue;
            String email = b.getClient() != null ? b.getClient().getEmail() : null;
            if (email == null || email.isBlank()) continue;
            String clientName = b.getClient().getFullName() != null ? b.getClient().getFullName() : "Client";
            String ref = b.getBookingReference() != null ? b.getBookingReference() : "";
            String rwf = String.format("%,.0f", remaining.doubleValue());
            String subject = "Payment Reminder — Madeleine Group | Rappel de Paiement — Madeleine Group";
            String en = "Dear " + clientName + ",\n\n" +
                "This is a friendly reminder that your booking (reference: " + ref + ") has an outstanding balance of " + rwf + " RWF.\n\n" +
                "Please settle this balance within 7 days so we can finalize your event arrangements.\n\n" +
                CONTACT_INFO + "\n\nWarm regards,\nThe Madeleine Group Team";
            String fr = "Cher/Chere " + clientName + ",\n\n" +
                "Ceci est un rappel amical : votre réservation (référence : " + ref + ") a un solde restant de " + rwf + " RWF.\n\n" +
                "Veuillez régler ce solde sous 7 jours afin que nous puissions finaliser l'organisation de votre événement.\n\n" +
                CONTACT_FR + "\n\nCordialement,\nL'équipe Madeleine Group";
            emailService.sendNotificationEmail(email, subject, en + "\n\n---\n\n" + fr);
            summary.emailsSent++;
            summary.totalAmountRwf = summary.totalAmountRwf.add(remaining);
            summary.clientNames.add(clientName + " (" + rwf + " RWF)");
        }
        if (summary.emailsSent > 0 && triggeredByUserId != null) {
            saveAudit("PAYMENT_REMINDER", summary.emailsSent, summary.totalAmountRwf,
                summary.clientNames, triggeredByUserId, null);
        }
        return summary;
    }

    public SimpleSummary sendOverdueReminders(Long triggeredByUserId) {
        SimpleSummary summary = new SimpleSummary();
        LocalDate today = LocalDate.now();
        List<Booking> list = bookingRepository.findOverdueBookingsWithDetails(today);
        for (Booking b : list) {
            String email = b.getClient() != null ? b.getClient().getEmail() : null;
            if (email == null || email.isBlank()) continue;
            String clientName = b.getClient().getFullName() != null ? b.getClient().getFullName() : "Client";
            String ref = b.getBookingReference() != null ? b.getBookingReference() : "";
            String eventDate = b.getEventDate() != null ? b.getEventDate().format(DATE_FMT) : "";
            String subject = "Important: Your Booking Needs Attention | Important: Votre Réservation Nécessite Votre Attention";
            String en = "Dear " + clientName + ",\n\n" +
                "Your booking (reference: " + ref + ") had an event date of " + eventDate + ", which has now passed.\n\n" +
                "We kindly ask you to contact Madeleine Group to finalize the status of your booking (e.g. complete payment or update the booking). " +
                CONTACT_INFO + "\n\nWarm regards,\nThe Madeleine Group Team";
            String fr = "Cher/Chere " + clientName + ",\n\n" +
                "Votre réservation (référence : " + ref + ") avait une date d'événement le " + eventDate + ", qui est désormais passée.\n\n" +
                "Nous vous invitons à contacter Madeleine Group pour finaliser le statut de votre réservation (paiement ou mise à jour). " +
                CONTACT_FR + "\n\nCordialement,\nL'équipe Madeleine Group";
            emailService.sendNotificationEmail(email, subject, en + "\n\n---\n\n" + fr);
            summary.emailsSent++;
        }
        if (summary.emailsSent > 0 && triggeredByUserId != null) {
            saveAudit("OVERDUE_REMINDER", summary.emailsSent, null,
                list.stream().map(b -> b.getClient() != null ? b.getClient().getFullName() : "?").collect(Collectors.toList()),
                triggeredByUserId, null);
        }
        return summary;
    }

    public SimpleSummary sendPendingFollowup(Long triggeredByUserId) {
        SimpleSummary summary = new SimpleSummary();
        LocalDateTime cutoff = LocalDateTime.now().minusDays(3);
        List<Booking> list = bookingRepository.findPendingBookingsCreatedBefore(cutoff);
        for (Booking b : list) {
            String email = b.getClient() != null ? b.getClient().getEmail() : null;
            if (email == null || email.isBlank()) continue;
            String clientName = b.getClient().getFullName() != null ? b.getClient().getFullName() : "Client";
            String subject = "Still interested in booking with us? | Toujours intéressé par nos services?";
            String en = "Dear " + clientName + ",\n\n" +
                "You previously submitted a booking inquiry with Madeleine Group that is still pending.\n\n" +
                "We would love to hear from you — would you like to confirm your booking or need any assistance? " +
                CONTACT_INFO + "\n\nWarm regards,\nThe Madeleine Group Team";
            String fr = "Cher/Chere " + clientName + ",\n\n" +
                "Vous avez précédemment soumis une demande de réservation chez Madeleine Group qui est toujours en attente.\n\n" +
                "Nous serions ravis de vous répondre — souhaitez-vous confirmer votre réservation ou avez-vous besoin d'aide? " +
                CONTACT_FR + "\n\nCordialement,\nL'équipe Madeleine Group";
            emailService.sendNotificationEmail(email, subject, en + "\n\n---\n\n" + fr);
            summary.emailsSent++;
        }
        if (summary.emailsSent > 0 && triggeredByUserId != null) {
            saveAudit("PENDING_FOLLOWUP", summary.emailsSent, null,
                list.stream().map(b -> b.getClient() != null ? b.getClient().getFullName() : "?").collect(Collectors.toList()),
                triggeredByUserId, null);
        }
        return summary;
    }

    private void saveAudit(String actionType, int emailsSent, BigDecimal totalAmountRwf,
                           List<String> clientNames, Long triggeredByUserId, String notes) {
        try {
            rw.madeleinegroup.entity.AiEmailAction audit = new rw.madeleinegroup.entity.AiEmailAction();
            audit.setActionType(actionType);
            audit.setEmailsSent(emailsSent);
            audit.setTotalAmountRwf(totalAmountRwf);
            String json = clientNames != null && !clientNames.isEmpty()
                ? "[\"" + clientNames.stream().map(s -> s.replace("\"", "\\\"")).collect(Collectors.joining("\",\"")) + "\"]"
                : "[]";
            audit.setClientsContacted(json);
            if (triggeredByUserId != null) {
                userRepository.findById(triggeredByUserId).ifPresent(audit::setTriggeredBy);
            }
            audit.setNotes(notes);
            aiEmailActionRepository.save(audit);
        } catch (Exception e) {
            log.warn("Failed to save AiEmailAction audit: {}", e.getMessage());
        }
    }

    /** Build preview for confirmation: payment reminder. Returns list of "ClientName (X RWF)" and total. */
    public PaymentReminderSummary previewPaymentReminders() {
        PaymentReminderSummary summary = new PaymentReminderSummary();
        summary.totalAmountRwf = BigDecimal.ZERO;
        List<Booking> list = bookingRepository.findConfirmedOrInProgressWithDetails();
        for (Booking b : list) {
            BigDecimal remaining = (b.getEstimatedAmount() != null ? b.getEstimatedAmount() : BigDecimal.ZERO)
                .subtract(b.getPaidAmount() != null ? b.getPaidAmount() : BigDecimal.ZERO);
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) continue;
            if (b.getClient() == null || b.getClient().getEmail() == null || b.getClient().getEmail().isBlank()) continue;
            String clientName = b.getClient().getFullName() != null ? b.getClient().getFullName() : "Client";
            String rwf = String.format("%,.0f", remaining.doubleValue());
            summary.clientNames.add(clientName + " (" + rwf + " RWF)");
            summary.totalAmountRwf = summary.totalAmountRwf.add(remaining);
            summary.emailsSent++;
        }
        return summary;
    }

    public int previewOverdueCount() {
        return bookingRepository.findOverdueBookingsWithDetails(LocalDate.now()).size();
    }

    public int previewPendingFollowupCount() {
        return bookingRepository.findPendingBookingsCreatedBefore(LocalDateTime.now().minusDays(3)).size();
    }
}
