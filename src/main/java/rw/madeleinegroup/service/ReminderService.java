package rw.madeleinegroup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rw.madeleinegroup.dto.*;
import rw.madeleinegroup.entity.Booking;
import rw.madeleinegroup.entity.Client;
import rw.madeleinegroup.repository.AiEmailActionRepository;
import rw.madeleinegroup.repository.BookingRepository;
import rw.madeleinegroup.repository.ClientRepository;
import rw.madeleinegroup.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final BigDecimal HIGH_THRESHOLD = new BigDecimal("1000000");
    private static final BigDecimal MEDIUM_THRESHOLD = new BigDecimal("300000");

    private final BookingRepository bookingRepository;
    private final ClientRepository clientRepository;
    private final GroqAiService groqAiService;
    private final EmailService emailService;
    private final AiEmailActionRepository aiEmailActionRepository;
    private final UserRepository userRepository;

    public ReminderService(BookingRepository bookingRepository,
                           ClientRepository clientRepository,
                           GroqAiService groqAiService,
                           EmailService emailService,
                           AiEmailActionRepository aiEmailActionRepository,
                           UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.clientRepository = clientRepository;
        this.groqAiService = groqAiService;
        this.emailService = emailService;
        this.aiEmailActionRepository = aiEmailActionRepository;
        this.userRepository = userRepository;
    }

    public List<EligibleClientDto> getEligibleClients() {
        LocalDate today = LocalDate.now();
        LocalDateTime cutoff = LocalDateTime.now().minusDays(3);

        List<Booking> withBalance = bookingRepository.findBookingsWithPendingBalanceOrderByRemainingDesc();
        List<Booking> overdue = bookingRepository.findOverdueBookingsWithDetailsOrderByEventDateAsc(today);
        List<Booking> pendingOld = bookingRepository.findPendingBookingsCreatedBefore(cutoff);

        Map<Long, EligibleClientDto> byClientId = new LinkedHashMap<>();

        for (Booking b : withBalance) {
            if (b.getClient() == null) continue;
            Long cid = b.getClient().getId();
            EligibleClientDto dto = byClientId.computeIfAbsent(cid, id -> buildClientDto(b.getClient()));
            addBookingToDto(dto, b, "PENDING_PAYMENT");
        }
        for (Booking b : overdue) {
            if (b.getClient() == null) continue;
            Long cid = b.getClient().getId();
            EligibleClientDto dto = byClientId.computeIfAbsent(cid, id -> buildClientDto(b.getClient()));
            addBookingToDto(dto, b, "OVERDUE");
        }
        for (Booking b : pendingOld) {
            if (b.getClient() == null) continue;
            Long cid = b.getClient().getId();
            EligibleClientDto dto = byClientId.computeIfAbsent(cid, id -> buildClientDto(b.getClient()));
            addBookingToDto(dto, b, "PENDING_BOOKING");
        }

        List<EligibleClientDto> list = new ArrayList<>(byClientId.values());
        for (EligibleClientDto dto : list) {
            BigDecimal total = dto.getBookings().stream()
                .map(EligibleBookingDto::getRemainingBalance)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setTotalRemainingBalance(total);
            String reason = dto.getEligibilityReason(); // already set to highest priority reason in addBookingToDto
            if (reason == null) dto.setEligibilityReason("PENDING_PAYMENT");
            if (total.compareTo(HIGH_THRESHOLD) > 0) dto.setPriority("HIGH");
            else if (total.compareTo(MEDIUM_THRESHOLD) > 0) dto.setPriority("MEDIUM");
            else dto.setPriority("LOW");
        }
        return list;
    }

    private EligibleClientDto buildClientDto(Client c) {
        EligibleClientDto dto = new EligibleClientDto();
        dto.setId(c.getId());
        dto.setFullName(c.getFullName() != null ? c.getFullName() : "");
        dto.setEmail(c.getEmail() != null ? c.getEmail() : "");
        dto.setPhone(c.getPhone());
        dto.setProfilePhotoUrl(c.getProfilePhotoUrl());
        dto.setBookings(new ArrayList<>());
        dto.setEligibilityReason("PENDING_PAYMENT");
        return dto;
    }

    private void addBookingToDto(EligibleClientDto dto, Booking b, String reason) {
        if (dto.getBookings().stream().anyMatch(eb -> b.getBookingReference() != null && b.getBookingReference().equals(eb.getBookingReference())))
            return;
        EligibleBookingDto eb = new EligibleBookingDto();
        eb.setBookingReference(b.getBookingReference());
        eb.setEventType(b.getEventType());
        eb.setEventDate(b.getEventDate());
        eb.setEstimatedAmount(b.getEstimatedAmount());
        eb.setPaidAmount(b.getPaidAmount());
        BigDecimal rem = (b.getEstimatedAmount() != null ? b.getEstimatedAmount() : BigDecimal.ZERO)
            .subtract(b.getPaidAmount() != null ? b.getPaidAmount() : BigDecimal.ZERO);
        eb.setRemainingBalance(rem);
        dto.getBookings().add(eb);
        if ("OVERDUE".equals(reason)) dto.setEligibilityReason("OVERDUE");
        else if ("PENDING_BOOKING".equals(reason) && !"OVERDUE".equals(dto.getEligibilityReason())) dto.setEligibilityReason("PENDING_BOOKING");
    }

    public GenerateEmailResponse generateEmail(GenerateEmailRequest request) {
        return groqAiService.generateEmailTemplate(
            request.getEmailType(),
            request.getLanguage(),
            request.getUserInstruction(),
            request.getTone()
        );
    }

    public SendRemindersResponse sendReminders(SendRemindersRequest request, Long triggeredByUserId) {
        SendRemindersResponse response = new SendRemindersResponse();
        response.setSentCount(0);
        response.setFailedCount(0);
        response.setContactedClientNames(new ArrayList<>());

        List<Long> clientIds = request.getClientIds() != null ? request.getClientIds() : Collections.emptyList();
        String subjectTemplate = request.getSubject() != null ? request.getSubject() : "";
        String bodyTemplate = request.getBodyTemplate() != null ? request.getBodyTemplate() : "";

        BigDecimal totalAmountRwf = BigDecimal.ZERO;

        for (Long clientId : clientIds) {
            Optional<Client> clientOpt = clientRepository.findById(clientId);
            if (clientOpt.isEmpty()) {
                response.setFailedCount(response.getFailedCount() + 1);
                continue;
            }
            Client client = clientOpt.get();
            String email = client.getEmail();
            if (email == null || email.isBlank()) {
                response.setFailedCount(response.getFailedCount() + 1);
                continue;
            }

            List<Booking> clientBookings = bookingRepository.findByClientIdWithDetails(clientId);
            BigDecimal remaining = BigDecimal.ZERO;
            String bookingRef = "";
            String eventDateStr = "";
            for (Booking b : clientBookings) {
                BigDecimal rem = (b.getEstimatedAmount() != null ? b.getEstimatedAmount() : BigDecimal.ZERO)
                    .subtract(b.getPaidAmount() != null ? b.getPaidAmount() : BigDecimal.ZERO);
                if (rem.compareTo(BigDecimal.ZERO) > 0) {
                    remaining = remaining.add(rem);
                    if (bookingRef.isEmpty()) {
                        bookingRef = b.getBookingReference() != null ? b.getBookingReference() : "";
                        eventDateStr = b.getEventDate() != null ? b.getEventDate().format(DATE_FMT) : "";
                    }
                }
            }
            String remainingStr = String.format("%,.0f", remaining.doubleValue());
            String clientName = client.getFullName() != null ? client.getFullName() : "Client";

            String subject = replacePlaceholders(subjectTemplate, clientName, bookingRef, remainingStr, eventDateStr);
            String body = replacePlaceholders(bodyTemplate, clientName, bookingRef, remainingStr, eventDateStr);
            String lang = request.getLanguage() != null && !request.getLanguage().isBlank()
                ? request.getLanguage().trim().toUpperCase() : "FR";
            if (!"EN".equals(lang)) lang = "FR";

            try {
                emailService.sendReminderEmailHtmlSync(email, subject, clientName, bookingRef, remainingStr, eventDateStr, body, lang);
                response.setSentCount(response.getSentCount() + 1);
                response.getContactedClientNames().add(clientName);
                totalAmountRwf = totalAmountRwf.add(remaining);
            } catch (Exception e) {
                log.warn("Failed to send reminder to {} ({}): {}", clientName, email, e.getMessage());
                response.setFailedCount(response.getFailedCount() + 1);
            }
        }

        if (response.getSentCount() > 0 && triggeredByUserId != null) {
            try {
                rw.madeleinegroup.entity.AiEmailAction audit = new rw.madeleinegroup.entity.AiEmailAction();
                audit.setActionType("REMINDER_MODAL");
                audit.setEmailsSent(response.getSentCount());
                audit.setTotalAmountRwf(totalAmountRwf);
                String json = "[\"" + response.getContactedClientNames().stream()
                    .map(s -> s.replace("\\", "\\\\").replace("\"", "\\\""))
                    .collect(Collectors.joining("\",\"")) + "\"]";
                audit.setClientsContacted(json);
                userRepository.findById(triggeredByUserId).ifPresent(audit::setTriggeredBy);
                audit.setNotes("Subject: " + (request.getSubject() != null ? request.getSubject().substring(0, Math.min(200, request.getSubject().length())) : ""));
                aiEmailActionRepository.save(audit);
            } catch (Exception e) {
                log.warn("Failed to save reminder audit: {}", e.getMessage());
            }
        }

        return response;
    }

    private static String replacePlaceholders(String text, String clientName, String bookingRef, String remainingAmount, String eventDate) {
        if (text == null) return "";
        return text
            .replace("CLIENT_NAME", clientName != null ? clientName : "")
            .replace("BOOKING_REFERENCE", bookingRef != null ? bookingRef : "")
            .replace("REMAINING_AMOUNT", remainingAmount != null ? remainingAmount : "")
            .replace("EVENT_DATE", eventDate != null ? eventDate : "");
    }
}
