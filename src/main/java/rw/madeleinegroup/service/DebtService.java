package rw.madeleinegroup.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.madeleinegroup.dto.*;
import rw.madeleinegroup.entity.*;
import rw.madeleinegroup.repository.BookingRepository;
import rw.madeleinegroup.repository.DebtPaymentRepository;
import rw.madeleinegroup.repository.DebtReminderRepository;
import rw.madeleinegroup.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DebtService {

    private static final Set<String> EMAIL_LANGS = Set.of("en", "fr", "rw");

    private final BookingRepository bookingRepository;
    private final DebtPaymentRepository debtPaymentRepository;
    private final DebtReminderRepository debtReminderRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public DebtService(BookingRepository bookingRepository,
                       DebtPaymentRepository debtPaymentRepository,
                       DebtReminderRepository debtReminderRepository,
                       UserRepository userRepository,
                       EmailService emailService,
                       NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.debtPaymentRepository = debtPaymentRepository;
        this.debtReminderRepository = debtReminderRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public DebtListResponse listOutstanding(Long branchId) {
        List<Booking> list = bookingRepository.findOutstandingDebtBookings();
        if (branchId != null) {
            list = list.stream()
                    .filter(b -> b.getBranch() != null && branchId.equals(b.getBranch().getId()))
                    .collect(Collectors.toList());
        }
        list.sort(Comparator.comparing(DebtService::remaining).reversed());

        DebtListResponse out = new DebtListResponse();
        BigDecimal totalOut = BigDecimal.ZERO;
        BigDecimal totalEst = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();
        int overdueEvents = 0;
        int partialPayments = 0;

        for (Booking b : list) {
            DebtBookingRowDto row = toRow(b);
            out.getRows().add(row);
            totalOut = totalOut.add(row.getRemainingBalance() != null ? row.getRemainingBalance() : BigDecimal.ZERO);
            totalEst = totalEst.add(row.getEstimatedAmount() != null ? row.getEstimatedAmount() : BigDecimal.ZERO);
            totalPaid = totalPaid.add(row.getPaidAmount() != null ? row.getPaidAmount() : BigDecimal.ZERO);
            if (b.getEventDate() != null && b.getEventDate().isBefore(today)) {
                overdueEvents++;
            }
            BigDecimal est = b.getEstimatedAmount() != null ? b.getEstimatedAmount() : BigDecimal.ZERO;
            BigDecimal paid = b.getPaidAmount() != null ? b.getPaidAmount() : BigDecimal.ZERO;
            if (est.compareTo(BigDecimal.ZERO) > 0
                    && paid.compareTo(BigDecimal.ZERO) > 0
                    && paid.compareTo(est) < 0) {
                partialPayments++;
            }
        }
        out.setTotalOutstandingRwf(totalOut);
        out.setTotalEstimatedRwf(totalEst);
        out.setTotalPaidRwf(totalPaid);
        out.setBookingCount(out.getRows().size());
        out.setOverdueEventCount(overdueEvents);
        out.setPartialPaymentCount(partialPayments);
        return out;
    }

    @Transactional(readOnly = true)
    public DebtDetailResponse getBookingDebtDetail(Long bookingId) {
        Booking b = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        DebtDetailResponse d = new DebtDetailResponse();
        d.setBooking(toRow(b));
        for (DebtPayment p : debtPaymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId)) {
            d.getDebtPayments().add(toPaymentDto(p));
        }
        for (DebtReminder r : debtReminderRepository.findByBookingIdOrderBySentAtDesc(bookingId)) {
            d.getDebtReminders().add(toReminderDto(r));
        }
        return d;
    }

    @Transactional
    @CacheEvict(value = {"financeKpis", "monthlyTrend", "categoryBreakdown", "branchRevenue", "aiLiveSnapshot"}, allEntries = true)
    public DebtPaymentItemDto recordDebtPayment(DebtPaymentRequest req, String userEmail) {
        User u = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Booking booking = bookingRepository.findByIdWithDetails(req.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getClient() == null) {
            throw new IllegalArgumentException("Booking has no client");
        }

        BigDecimal estimated = booking.getEstimatedAmount() != null ? booking.getEstimatedAmount() : BigDecimal.ZERO;
        BigDecimal already = booking.getPaidAmount() != null ? booking.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal remaining = estimated.subtract(already);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("No remaining balance on this booking");
        }
        if (req.getAmount().compareTo(remaining) > 0) {
            throw new IllegalArgumentException("Amount exceeds remaining balance (" + remaining + " RWF)");
        }

        DebtPayment dp = new DebtPayment();
        dp.setBooking(booking);
        dp.setClient(booking.getClient());
        dp.setAmount(req.getAmount());
        dp.setPaymentDate(req.getPaymentDate());
        dp.setPaymentMethod(req.getPaymentMethod());
        dp.setNote(req.getNote());
        dp.setRecordedBy(u);
        dp = debtPaymentRepository.save(dp);

        BigDecimal newPaid = already.add(req.getAmount());
        booking.setPaidAmount(newPaid);
        if (req.getPaymentMethod() != null && !req.getPaymentMethod().isBlank()) {
            booking.setPaymentMethod(req.getPaymentMethod());
        }
        bookingRepository.save(booking);

        Booking refreshed = bookingRepository.findByIdWithDetails(booking.getId()).orElse(booking);
        notificationService.notifyDebtPaymentRecorded(dp, refreshed, u);

        return toPaymentDto(dp);
    }

    @Transactional
    public DebtReminderItemDto recordReminder(DebtReminderRequest req, String userEmail) {
        User u = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Booking booking = bookingRepository.findByIdWithDetails(req.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getClient() == null) {
            throw new IllegalArgumentException("Booking has no client");
        }

        String clientEmail = booking.getClient().getEmail();
        if (clientEmail == null || clientEmail.isBlank()) {
            throw new IllegalArgumentException("Client has no email address");
        }

        String lang = req.getEmailLanguage() != null ? req.getEmailLanguage().trim().toLowerCase(Locale.ROOT) : "en";
        if (!EMAIL_LANGS.contains(lang)) {
            lang = "en";
        }

        String message = req.getMessage();
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message body is required");
        }

        emailService.sendDebtReminderFromDashboardSync(clientEmail.trim(), lang, message.trim());

        DebtReminder dr = new DebtReminder();
        dr.setBooking(booking);
        dr.setClient(booking.getClient());
        dr.setSentBy(u);
        dr.setMethod("email");
        dr.setEmailLanguage(lang);
        dr.setMessage(req.getMessage());
        dr = debtReminderRepository.save(dr);

        booking.setLastReminderSent(LocalDateTime.now());
        bookingRepository.save(booking);

        return toReminderDto(dr);
    }

    @Transactional
    public DebtBookingRowDto patchBookingDebt(Long bookingId, DebtBookingDebtPatchRequest req) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (req.getDebtNotes() != null) {
            b.setDebtNotes(req.getDebtNotes());
        }
        if (req.getPaymentMethod() != null) {
            b.setPaymentMethod(req.getPaymentMethod());
        }
        b = bookingRepository.save(b);
        return toRow(b);
    }

    private static BigDecimal remaining(Booking b) {
        BigDecimal est = b.getEstimatedAmount() != null ? b.getEstimatedAmount() : BigDecimal.ZERO;
        BigDecimal paid = b.getPaidAmount() != null ? b.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal r = est.subtract(paid);
        return r.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : r;
    }

    private DebtBookingRowDto toRow(Booking b) {
        DebtBookingRowDto row = new DebtBookingRowDto();
        row.setBookingId(b.getId());
        row.setBookingReference(b.getBookingReference());
        row.setStatus(b.getStatus());
        row.setEventDate(b.getEventDate());
        row.setEstimatedAmount(b.getEstimatedAmount());
        row.setPaidAmount(b.getPaidAmount());
        row.setRemainingBalance(remaining(b));
        row.setDebtNotes(b.getDebtNotes());
        row.setPaymentMethod(b.getPaymentMethod());
        row.setLastReminderSent(b.getLastReminderSent());
        if (b.getClient() != null) {
            row.setClientId(b.getClient().getId());
            row.setClientName(b.getClient().getFullName());
            row.setClientEmail(b.getClient().getEmail());
        }
        if (b.getBranch() != null) {
            row.setBranchId(b.getBranch().getId());
            row.setBranchName(b.getBranch().getName());
        }
        return row;
    }

    private DebtPaymentItemDto toPaymentDto(DebtPayment p) {
        DebtPaymentItemDto d = new DebtPaymentItemDto();
        d.setId(p.getId());
        d.setAmount(p.getAmount());
        d.setPaymentDate(p.getPaymentDate());
        d.setPaymentMethod(p.getPaymentMethod());
        d.setNote(p.getNote());
        d.setCreatedAt(p.getCreatedAt());
        if (p.getRecordedBy() != null) {
            d.setRecordedByName(p.getRecordedBy().getFullName());
        }
        return d;
    }

    private DebtReminderItemDto toReminderDto(DebtReminder r) {
        DebtReminderItemDto d = new DebtReminderItemDto();
        d.setId(r.getId());
        d.setSentAt(r.getSentAt());
        d.setMethod(r.getMethod());
        d.setEmailLanguage(r.getEmailLanguage());
        d.setMessage(r.getMessage());
        if (r.getSentBy() != null) {
            d.setSentByName(r.getSentBy().getFullName());
        }
        return d;
    }
}
