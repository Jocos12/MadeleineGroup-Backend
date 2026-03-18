package rw.madeleinegroup.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import rw.madeleinegroup.ai.*;
import rw.madeleinegroup.dto.AiAdvisorRequest;
import rw.madeleinegroup.dto.AiAdvisorResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements a strict two-tier AI architecture.
 *
 * Tier 1 — Local processing (Spring Boot): Handles all queries involving personal data (client names,
 * amounts, booking references), all email sending with real recipient data, all data list responses,
 * all confirmation flows, and all audit logging. No personal data ever leaves the server.
 *
 * Tier 2 — Groq AI: Handles only general language generation using anonymous aggregated statistics,
 * optional email template text generation with no personal data, and conversational responses to
 * general financial questions.
 *
 * This separation guarantees that no personal data ever reaches external AI services while still
 * providing intelligent natural language responses for general questions.
 */
@Service
public class AiAdvisorService {

    private static final String PENDING_PAYMENT = "PAYMENT_REMINDER";
    private static final String PENDING_OVERDUE = "OVERDUE_REMINDER";
    private static final String PENDING_FOLLOWUP = "PENDING_FOLLOWUP";

    private final FinancialDataService dataService;
    private final DeepAnalysisEngine analysisEngine;
    private final MasterResponseBuilder responseBuilder;
    private final LocalResponseBuilder localResponseBuilder;
    private final IntentClassifier intentClassifier;
    private final LanguageDetector languageDetector;
    private final PeriodExtractor periodExtractor;
    private final GroqAiService groqAiService;
    private final EmailActionService emailActionService;

    /** userId -> pending email action type (PAYMENT_REMINDER, OVERDUE_REMINDER, PENDING_FOLLOWUP) */
    private final ConcurrentHashMap<Long, String> pendingEmailActionByUser = new ConcurrentHashMap<>();

    public AiAdvisorService(
            FinancialDataService dataService,
            DeepAnalysisEngine analysisEngine,
            MasterResponseBuilder responseBuilder,
            LocalResponseBuilder localResponseBuilder,
            IntentClassifier intentClassifier,
            LanguageDetector languageDetector,
            PeriodExtractor periodExtractor,
            GroqAiService groqAiService,
            EmailActionService emailActionService) {
        this.dataService = dataService;
        this.analysisEngine = analysisEngine;
        this.responseBuilder = responseBuilder;
        this.localResponseBuilder = localResponseBuilder;
        this.intentClassifier = intentClassifier;
        this.languageDetector = languageDetector;
        this.periodExtractor = periodExtractor;
        this.groqAiService = groqAiService;
        this.emailActionService = emailActionService;
    }

    public boolean isConfigured() {
        return groqAiService.isConfigured();
    }

    public String getProviderName() {
        return groqAiService.isConfigured() ? "Groq" : "Madeleine Group Internal AI";
    }

    public Map<String, Object> getStatus() {
        boolean groqConfigured = groqAiService.isConfigured();
        return Map.of(
            "provider", "Groq",
            "model", groqAiService.getModel(),
            "configured", groqConfigured,
            "dataStaysLocal", false,
            "dataAnonymized", true
        );
    }

    public AiAdvisorResult chat(AiAdvisorRequest request) {
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            throw new IllegalArgumentException("Messages cannot be empty");
        }

        List<Map<String, String>> messages = request.getMessages();
        String lastMessage = messages.get(messages.size() - 1).get("content");
        if (lastMessage == null) lastMessage = "";
        Long userId = request.getUserId();
        String intent = intentClassifier.resolveIntent(lastMessage, messages);
        boolean isFrench = languageDetector.isFrench(lastMessage);

        // ——— LOCAL intents: Spring Boot handles entirely, Groq is never called ———
        if (intentClassifier.isLocalIntent(intent)) {
            switch (intent) {
                case "SHOW_CLIENTS_PENDING":
                    return new AiAdvisorResult(localResponseBuilder.buildClientsPendingResponse(isFrench), null);
                case "SHOW_BOOKINGS":
                    return new AiAdvisorResult(localResponseBuilder.buildBookingsListResponse(isFrench), null);
                case "SHOW_OVERDUE":
                    return new AiAdvisorResult(localResponseBuilder.buildOverdueDetailsResponse(isFrench), null);
                case "SHOW_PAYMENTS":
                    return new AiAdvisorResult(localResponseBuilder.buildPaymentsListResponse(isFrench), null);
                case "DATA_EXPORT":
                    return new AiAdvisorResult(localResponseBuilder.buildDataExportResponse(isFrench), null);
                default:
                    break; // CONFIRM_EMAIL_ACTION, CANCEL_EMAIL_ACTION, SEND_EMAIL_REMINDER handled below
            }
        }

        // Confirm email action (user said OUI / YES / confirme)
        if ("CONFIRM_EMAIL_ACTION".equals(intent) && userId != null) {
            String pendingType = pendingEmailActionByUser.remove(userId);
            if (pendingType != null) {
                return executeEmailActionAndRespond(pendingType, userId, languageDetector.isFrench(lastMessage));
            }
        }

        // Cancel email action (user said NON / NO / cancel)
        if ("CANCEL_EMAIL_ACTION".equals(intent) && userId != null) {
            pendingEmailActionByUser.remove(userId);
            boolean fr = languageDetector.isFrench(lastMessage);
            String reply = fr ? "Envoi annulé. Aucun email n'a été envoyé." : "Send cancelled. No emails were sent.";
            return new AiAdvisorResult(reply, null);
        }

        // New email reminder request: show confirmation and store pending (still LOCAL, no Groq)
        if ("SEND_EMAIL_REMINDER".equals(intent)) {
            String subtype = detectEmailSubtype(lastMessage);
            boolean fr = isFrench;
            if ("OVERDUE_REMINDER".equals(subtype)) {
                int count = emailActionService.previewOverdueCount();
                if (count == 0) {
                    String reply = fr ? "Aucune réservation en retard à ce jour." : "No overdue bookings at this time.";
                    return new AiAdvisorResult(reply, null);
                }
                if (userId != null) pendingEmailActionByUser.put(userId, PENDING_OVERDUE);
                String reply = fr
                    ? "Je suis prêt à envoyer " + count + " email(s) de rappel urgent aux clients dont la réservation est en retard. Confirmez-vous l'envoi? Répondez OUI pour confirmer."
                    : "I am ready to send " + count + " urgent reminder email(s) to clients with overdue bookings. Do you confirm? Reply YES to confirm.";
                return AiAdvisorResult.confirmation(reply, null, "OVERDUE_REMINDER", List.of(count + " client(s)"), null);
            }
            if ("PENDING_FOLLOWUP".equals(subtype)) {
                int count = emailActionService.previewPendingFollowupCount();
                if (count == 0) {
                    String reply = fr ? "Aucune réservation en attente de plus de 3 jours." : "No pending bookings older than 3 days.";
                    return new AiAdvisorResult(reply, null);
                }
                if (userId != null) pendingEmailActionByUser.put(userId, PENDING_FOLLOWUP);
                String reply = fr
                    ? "Je suis prêt à envoyer " + count + " email(s) de suivi aux clients avec réservation en attente. Confirmez-vous l'envoi? Répondez OUI pour confirmer."
                    : "I am ready to send " + count + " follow-up email(s) to clients with pending bookings. Do you confirm? Reply YES to confirm.";
                return AiAdvisorResult.confirmation(reply, null, "PENDING_FOLLOWUP", List.of(count + " client(s)"), null);
            }
            // Default: payment reminders
            EmailActionService.PaymentReminderSummary preview = emailActionService.previewPaymentReminders();
            if (preview.emailsSent == 0) {
                String reply = fr ? "Aucun client avec solde restant à ce jour." : "No clients with outstanding balance at this time.";
                return new AiAdvisorResult(reply, null);
            }
            if (userId != null) pendingEmailActionByUser.put(userId, PENDING_PAYMENT);
            String totalStr = String.format("%,.0f", preview.totalAmountRwf.doubleValue());
            String reply = fr
                ? "Je suis prêt à envoyer " + preview.emailsSent + " email(s) de rappel de paiement aux clients suivants: " + String.join(", ", preview.clientNames) + ". Total à récupérer: " + totalStr + " RWF. Confirmez-vous l'envoi? Répondez OUI pour confirmer."
                : "I am ready to send " + preview.emailsSent + " payment reminder email(s) to the following clients: " + String.join(", ", preview.clientNames) + ". Total to collect: " + totalStr + " RWF. Do you confirm? Reply YES to confirm.";
            return AiAdvisorResult.confirmation(reply, null, PENDING_PAYMENT, preview.clientNames, totalStr + " RWF");
        }

        int[] period = periodExtractor.extractPeriod(lastMessage, messages);
        int year = period[0];
        int month = period[1];
        Map<String, Object> ctx = request.getFinancialContext();
        if (ctx != null) {
            if (ctx.get("year") != null) {
                try { year = Integer.parseInt(ctx.get("year").toString()); } catch (Exception ignored) { }
            }
            if (ctx.get("month") != null) {
                try { month = Integer.parseInt(ctx.get("month").toString()); } catch (Exception ignored) { }
            }
        }

        LiveFinancialSnapshot snapshot = dataService.getSnapshot(year, month);
        DeepReport report = analysisEngine.analyze(snapshot);

        // ——— GROQ intents: anonymous data only, Groq for language ———
        if (groqAiService.isConfigured()) {
            try {
                String reply = groqAiService.chat(lastMessage, messages, snapshot, report.getHealthScore());
                return new AiAdvisorResult(reply, report.getHealthScore());
            } catch (Exception e) {
                int[] prev = periodExtractor.previousMonth(year, month);
                LiveFinancialSnapshot snapshotPrevious = dataService.getSnapshot(prev[0], prev[1]);
                String fallbackReply = responseBuilder.build(intent, lastMessage, report, snapshot, isFrench, messages, snapshotPrevious);
                return new AiAdvisorResult(fallbackReply, report.getHealthScore());
            }
        }

        return chatLocal(request, year, month, snapshot, report);
    }

    private String detectEmailSubtype(String message) {
        if (message == null) return PENDING_PAYMENT;
        String m = message.toLowerCase();
        if (m.contains("en retard") || m.contains("overdue")) return "OVERDUE_REMINDER";
        if (m.contains("en attente") || m.contains("pending")) return "PENDING_FOLLOWUP";
        return PENDING_PAYMENT;
    }

    private AiAdvisorResult executeEmailActionAndRespond(String pendingType, Long userId, boolean isFrench) {
        int emailsSent = 0;
        String totalRwf = null;
        if (PENDING_PAYMENT.equals(pendingType)) {
            EmailActionService.PaymentReminderSummary r = emailActionService.sendPaymentReminders(userId);
            emailsSent = r.emailsSent;
            totalRwf = r.totalAmountRwf != null ? String.format("%,.0f", r.totalAmountRwf.doubleValue()) : null;
        } else if (PENDING_OVERDUE.equals(pendingType)) {
            EmailActionService.SimpleSummary r = emailActionService.sendOverdueReminders(userId);
            emailsSent = r.emailsSent;
        } else if (PENDING_FOLLOWUP.equals(pendingType)) {
            EmailActionService.SimpleSummary r = emailActionService.sendPendingFollowup(userId);
            emailsSent = r.emailsSent;
        }
        String reply;
        if (emailsSent == 0) {
            reply = isFrench ? "Aucun email envoyé (pas de destinataires éligibles)." : "No emails sent (no eligible recipients).";
        } else {
            reply = isFrench
                ? emailsSent + " email(s) envoyé(s) avec succès."
                : emailsSent + " email(s) sent successfully.";
            if (totalRwf != null) reply += isFrench ? " Total à récupérer: " + totalRwf + " RWF." : " Total to collect: " + totalRwf + " RWF.";
        }
        return new AiAdvisorResult(reply, null);
    }

    /** Local-only response (no Groq). Used when Groq is unavailable or as fallback endpoint. */
    public AiAdvisorResult chatLocal(AiAdvisorRequest request) {
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            throw new IllegalArgumentException("Messages cannot be empty");
        }
        List<Map<String, String>> messages = request.getMessages();
        String lastMessage = messages.get(messages.size() - 1).get("content");
        if (lastMessage == null) lastMessage = "";
        int[] period = periodExtractor.extractPeriod(lastMessage, messages);
        int year = period[0];
        int month = period[1];
        Map<String, Object> ctx = request.getFinancialContext();
        if (ctx != null) {
            if (ctx.get("year") != null) try { year = Integer.parseInt(ctx.get("year").toString()); } catch (Exception ignored) { }
            if (ctx.get("month") != null) try { month = Integer.parseInt(ctx.get("month").toString()); } catch (Exception ignored) { }
        }
        LiveFinancialSnapshot snapshot = dataService.getSnapshot(year, month);
        DeepReport report = analysisEngine.analyze(snapshot);
        return chatLocal(request, year, month, snapshot, report);
    }

    private AiAdvisorResult chatLocal(AiAdvisorRequest request, int year, int month,
                                      LiveFinancialSnapshot snapshot, DeepReport report) {
        List<Map<String, String>> messages = request.getMessages();
        String lastMessage = messages.get(messages.size() - 1).get("content");
        if (lastMessage == null) lastMessage = "";
        int[] prev = periodExtractor.previousMonth(year, month);
        LiveFinancialSnapshot snapshotPrevious = dataService.getSnapshot(prev[0], prev[1]);
        String intent = intentClassifier.resolveIntent(lastMessage, messages);
        boolean isFrench = languageDetector.isFrench(lastMessage);
        String reply = responseBuilder.build(intent, lastMessage, report, snapshot, isFrench, messages, snapshotPrevious);
        return new AiAdvisorResult(reply, report.getHealthScore());
    }

    @Async("aiTaskExecutor")
    public CompletableFuture<AiAdvisorResult> chatAsync(AiAdvisorRequest request) {
        return CompletableFuture.completedFuture(chat(request));
    }
}
