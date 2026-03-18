package rw.madeleinegroup.ai;

import org.springframework.stereotype.Component;
import rw.madeleinegroup.entity.Booking;
import rw.madeleinegroup.entity.Payment;
import rw.madeleinegroup.repository.BookingRepository;
import rw.madeleinegroup.repository.ClientRepository;
import rw.madeleinegroup.repository.ExpenseRepository;
import rw.madeleinegroup.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Builds AI advisor responses entirely from database data. Used for LOCAL intents only.
 * Groq is never called. All responses are built in Java with StringBuilder.
 */
@Component
public class LocalResponseBuilder {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ClientRepository clientRepository;
    private final ExpenseRepository expenseRepository;

    public LocalResponseBuilder(BookingRepository bookingRepository,
                               PaymentRepository paymentRepository,
                               ClientRepository clientRepository,
                               ExpenseRepository expenseRepository) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.clientRepository = clientRepository;
        this.expenseRepository = expenseRepository;
    }

    /**
     * Clients with pending balance (CONFIRMED/IN_PROGRESS, remaining > 0), ordered by remaining descending.
     * Format: title with count, each client with name, ref, balance RWF, event date, days since created, priority. Total, recommended action, note.
     */
    public String buildClientsPendingResponse(boolean isFrench) {
        List<Booking> list = bookingRepository.findBookingsWithPendingBalanceOrderByRemainingDesc();
        if (list.isEmpty()) {
            return isFrench
                ? "Aucun client avec un montant restant à régler pour des réservations confirmées ou en cours."
                : "No clients with outstanding balance for confirmed or in-progress bookings.";
        }
        BigDecimal total = BigDecimal.ZERO;
        StringBuilder sb = new StringBuilder();
        if (isFrench) {
            sb.append("**Clients avec montants en attente (").append(list.size()).append(")**\n\n");
        } else {
            sb.append("**Clients with pending amounts (").append(list.size()).append(")**\n\n");
        }
        String firstClientName = null;
        for (Booking b : list) {
            BigDecimal remaining = (b.getEstimatedAmount() != null ? b.getEstimatedAmount() : BigDecimal.ZERO)
                .subtract(b.getPaidAmount() != null ? b.getPaidAmount() : BigDecimal.ZERO);
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) continue;
            total = total.add(remaining);
            String name = b.getClient() != null && b.getClient().getFullName() != null ? b.getClient().getFullName() : "—";
            if (firstClientName == null) firstClientName = name;
            String ref = b.getBookingReference() != null ? b.getBookingReference() : "—";
            String eventDateStr = b.getEventDate() != null ? b.getEventDate().format(DATE_FMT) : "—";
            long daysSinceCreated = b.getCreatedAt() != null ? ChronoUnit.DAYS.between(b.getCreatedAt().toLocalDate(), LocalDate.now()) : 0;
            String priority = isFrench ? priorityFr(remaining) : priorityEn(remaining);
            sb.append("• **").append(name).append("** — ").append(ref).append(" — ").append(fmt(remaining)).append(" RWF — ").append(eventDateStr);
            sb.append(" — ").append(daysSinceCreated).append(isFrench ? " jours" : " days").append(" — ").append(priority).append("\n");
        }
        sb.append("\n");
        if (isFrench) {
            sb.append("**Total à récupérer : ").append(fmt(total)).append(" RWF**\n\n");
            sb.append("Contacter **").append(firstClientName != null ? firstClientName : "le premier client").append("** en priorité.\n\n");
            sb.append("Les coordonnées sont disponibles dans la section Clients du tableau de bord.");
        } else {
            sb.append("**Total to collect: ").append(fmt(total)).append(" RWF**\n\n");
            sb.append("Contact **").append(firstClientName != null ? firstClientName : "the first client").append("** first.\n\n");
            sb.append("Contact details are available in the Clients section of the dashboard.");
        }
        return sb.toString();
    }

    /**
     * Most recent 10 bookings as a clear table: reference, client name, event type, event date, status, estimated amount.
     */
    public String buildBookingsListResponse(boolean isFrench) {
        List<Booking> list = bookingRepository.findRecentBookingsWithDetails(org.springframework.data.domain.PageRequest.of(0, 10)).getContent();
        if (list.isEmpty()) {
            return isFrench ? "Aucune réservation." : "No bookings.";
        }
        StringBuilder sb = new StringBuilder();
        if (isFrench) sb.append("**Dernières réservations**\n\n");
        else sb.append("**Recent bookings**\n\n");
        sb.append("| ").append(isFrench ? "Référence" : "Reference").append(" | ").append(isFrench ? "Client" : "Client").append(" | ").append(isFrench ? "Type" : "Type").append(" | ").append(isFrench ? "Date" : "Date").append(" | ").append(isFrench ? "Statut" : "Status").append(" | ").append(isFrench ? "Montant" : "Amount").append(" |\n");
        sb.append("|---|---|---|---|---|---|\n");
        for (Booking b : list) {
            String ref = b.getBookingReference() != null ? b.getBookingReference() : "—";
            String client = b.getClient() != null && b.getClient().getFullName() != null ? b.getClient().getFullName() : "—";
            String type = b.getEventType() != null ? b.getEventType() : "—";
            String date = b.getEventDate() != null ? b.getEventDate().format(DATE_FMT) : "—";
            String status = b.getStatus() != null ? b.getStatus().name() : "—";
            String amount = b.getEstimatedAmount() != null ? fmt(b.getEstimatedAmount()) + " RWF" : "—";
            sb.append("| ").append(ref).append(" | ").append(client).append(" | ").append(type).append(" | ").append(date).append(" | ").append(status).append(" | ").append(amount).append(" |\n");
        }
        return sb.toString();
    }

    /**
     * All overdue bookings with full details: why overdue, financial risk, recommended action.
     */
    public String buildOverdueDetailsResponse(boolean isFrench) {
        List<Booking> list = bookingRepository.findOverdueBookingsWithDetailsOrderByEventDateAsc(LocalDate.now());
        if (list.isEmpty()) {
            return isFrench ? "Aucune réservation en retard." : "No overdue bookings.";
        }
        StringBuilder sb = new StringBuilder();
        if (isFrench) sb.append("**Réservations en retard (").append(list.size()).append(")**\n\n");
        else sb.append("**Overdue bookings (").append(list.size()).append(")**\n\n");
        for (Booking b : list) {
            String name = b.getClient() != null && b.getClient().getFullName() != null ? b.getClient().getFullName() : "—";
            String ref = b.getBookingReference() != null ? b.getBookingReference() : "—";
            String eventDateStr = b.getEventDate() != null ? b.getEventDate().format(DATE_FMT) : "—";
            BigDecimal estimated = b.getEstimatedAmount() != null ? b.getEstimatedAmount() : BigDecimal.ZERO;
            BigDecimal paid = b.getPaidAmount() != null ? b.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal remaining = estimated.subtract(paid);
            if (isFrench) {
                sb.append("• **").append(name).append("** — ").append(ref).append(" — Date événement : ").append(eventDateStr).append(" (passée). ");
                sb.append("Risque financier : ").append(fmt(remaining)).append(" RWF non récupérés. ");
                sb.append("Action : contacter le client pour finaliser le statut (paiement ou clôture).\n\n");
            } else {
                sb.append("• **").append(name).append("** — ").append(ref).append(" — Event date: ").append(eventDateStr).append(" (past). ");
                sb.append("Financial risk: ").append(fmt(remaining)).append(" RWF at risk. ");
                sb.append("Action: contact the client to finalize status (payment or closure).\n\n");
            }
        }
        return sb.toString();
    }

    /**
     * Recent payments with PENDING or PARTIAL status, formatted clearly.
     */
    public String buildPaymentsListResponse(boolean isFrench) {
        List<Payment> list = paymentRepository.findPaymentsWithPendingOrPartialWithDetails(
            rw.madeleinegroup.common.enums.PaymentStatus.PENDING,
            rw.madeleinegroup.common.enums.PaymentStatus.PARTIAL);
        if (list.isEmpty()) {
            return isFrench ? "Aucun paiement en attente ou partiel." : "No pending or partial payments.";
        }
        StringBuilder sb = new StringBuilder();
        if (isFrench) sb.append("**Paiements en attente ou partiels**\n\n");
        else sb.append("**Pending or partial payments**\n\n");
        sb.append("| ID | ").append(isFrench ? "Client" : "Client").append(" | ").append(isFrench ? "Montant" : "Amount").append(" | ").append(isFrench ? "Reste" : "Remaining").append(" | ").append(isFrench ? "Statut" : "Status").append(" |\n");
        sb.append("|---|---|---|---|---|\n");
        for (Payment p : list) {
            String client = p.getClient() != null && p.getClient().getFullName() != null ? p.getClient().getFullName() : "—";
            String amount = p.getAmount() != null ? fmt(p.getAmount()) + " RWF" : "—";
            String remaining = p.getRemainingBalance() != null ? fmt(p.getRemainingBalance()) + " RWF" : "—";
            String status = p.getPaymentStatus() != null ? p.getPaymentStatus().name() : "—";
            sb.append("| ").append(p.getId()).append(" | ").append(client).append(" | ").append(amount).append(" | ").append(remaining).append(" | ").append(status).append(" |\n");
        }
        return sb.toString();
    }

    /**
     * Email confirmation preview: who will be contacted and for how much. Real names and amounts, built locally.
     */
    public String buildEmailConfirmationResponse(boolean isFrench) {
        List<Booking> list = bookingRepository.findBookingsWithPendingBalanceOrderByRemainingDesc();
        BigDecimal total = BigDecimal.ZERO;
        StringBuilder lines = new StringBuilder();
        for (Booking b : list) {
            BigDecimal remaining = (b.getEstimatedAmount() != null ? b.getEstimatedAmount() : BigDecimal.ZERO)
                .subtract(b.getPaidAmount() != null ? b.getPaidAmount() : BigDecimal.ZERO);
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) continue;
            if (b.getClient() == null || b.getClient().getEmail() == null || b.getClient().getEmail().isBlank()) continue;
            String name = b.getClient().getFullName() != null ? b.getClient().getFullName() : "Client";
            total = total.add(remaining);
            lines.append("• ").append(name).append(" (").append(fmt(remaining)).append(" RWF)\n");
        }
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return isFrench ? "Aucun client à contacter pour rappel de paiement." : "No clients to contact for payment reminder.";
        }
        StringBuilder sb = new StringBuilder();
        if (isFrench) {
            sb.append("**Prévisualisation — Rappels de paiement**\n\n");
            sb.append("Les clients suivants seront contactés :\n\n");
        } else {
            sb.append("**Preview — Payment reminders**\n\n");
            sb.append("The following clients will be contacted:\n\n");
        }
        sb.append(lines);
        sb.append("\n");
        sb.append(isFrench ? "**Total à récupérer : " : "**Total to collect: ").append(fmt(total)).append(" RWF**\n\n");
        sb.append(isFrench ? "Confirmez-vous l'envoi des emails ? Répondez OUI pour confirmer." : "Do you confirm sending these emails? Reply YES to confirm.");
        return sb.toString();
    }

    /**
     * DATA_EXPORT: simple message that export is available in the dashboard (no actual export in chat).
     */
    public String buildDataExportResponse(boolean isFrench) {
        return isFrench
            ? "Les exportations de données sont disponibles depuis le tableau de bord (section Finance, Analytics). Vous pouvez y télécharger les rapports et données agrégées."
            : "Data export is available from the dashboard (Finance, Analytics section). You can download reports and aggregated data there.";
    }

    private static String priorityFr(BigDecimal remaining) {
        double v = remaining.doubleValue();
        if (v >= 1_000_000) return "PRIORITÉ HAUTE";
        if (v >= 500_000) return "PRIORITÉ MOYENNE";
        return "NORMALE";
    }

    private static String priorityEn(BigDecimal remaining) {
        double v = remaining.doubleValue();
        if (v >= 1_000_000) return "HIGH PRIORITY";
        if (v >= 500_000) return "MEDIUM PRIORITY";
        return "NORMAL";
    }

    private static String fmt(BigDecimal v) {
        if (v == null) return "0";
        return String.format("%,.0f", v.doubleValue());
    }
}
