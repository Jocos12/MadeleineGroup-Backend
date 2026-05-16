package rw.madeleinegroup.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.madeleinegroup.dto.InvoiceEligibleRowDto;
import rw.madeleinegroup.dto.InvoicePaymentLineDto;
import rw.madeleinegroup.dto.InvoicePdfPayload;
import rw.madeleinegroup.dto.InvoiceSendResultDto;
import rw.madeleinegroup.entity.Booking;
import rw.madeleinegroup.entity.DebtPayment;
import rw.madeleinegroup.entity.Payment;
import rw.madeleinegroup.entity.PaymentType;
import rw.madeleinegroup.entity.User;
import rw.madeleinegroup.common.enums.PaymentMethod;
import rw.madeleinegroup.repository.BookingRepository;
import rw.madeleinegroup.repository.DebtPaymentRepository;
import rw.madeleinegroup.repository.PaymentRepository;
import rw.madeleinegroup.repository.UserRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class InvoiceService {

    private final BookingRepository bookingRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final InvoicePdfService invoicePdfService;
    private final PaymentRepository paymentRepository;
    private final DebtPaymentRepository debtPaymentRepository;

    public InvoiceService(BookingRepository bookingRepository, EmailService emailService,
                          NotificationService notificationService, UserRepository userRepository,
                          InvoicePdfService invoicePdfService,
                          PaymentRepository paymentRepository,
                          DebtPaymentRepository debtPaymentRepository) {
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.invoicePdfService = invoicePdfService;
        this.paymentRepository = paymentRepository;
        this.debtPaymentRepository = debtPaymentRepository;
    }

    @Transactional(readOnly = true)
    public List<InvoiceEligibleRowDto> listFullyPaidEligible() {
        List<Booking> bookings = bookingRepository.findFullyPaidBookingsWithDetails();
        if (bookings.isEmpty()) {
            return List.of();
        }
        List<Long> ids = bookings.stream().map(Booking::getId).toList();
        List<Payment> incomeRows = paymentRepository.findIncomeByBookingIds(ids);
        List<DebtPayment> debtRows = debtPaymentRepository.findByBookingIdsWithBooking(ids);
        Map<Long, List<Payment>> byIncome = incomeRows.stream()
                .filter(p -> p.getBooking() != null)
                .collect(Collectors.groupingBy(p -> p.getBooking().getId()));
        Map<Long, List<DebtPayment>> byDebt = debtRows.stream()
                .filter(d -> d.getBooking() != null)
                .collect(Collectors.groupingBy(d -> d.getBooking().getId()));
        return bookings.stream()
                .map(b -> toRow(b, byIncome.getOrDefault(b.getId(), List.of()), byDebt.getOrDefault(b.getId(), List.of())))
                .collect(Collectors.toList());
    }

    @Transactional
    public InvoiceSendResultDto sendInvoices(List<Long> bookingIds, String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        InvoiceSendResultDto out = new InvoiceSendResultDto();
        List<String> errors = new ArrayList<>();
        int sent = 0;
        for (Long id : bookingIds) {
            if (id == null) {
                continue;
            }
            try {
                Booking b = bookingRepository.findByIdWithDetails(id)
                        .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
                if (!isFullyPaid(b)) {
                    throw new IllegalArgumentException("Booking is not fully paid");
                }
                emailService.sendInvoiceEmailSync(b);
                sent++;
            } catch (Exception e) {
                errors.add("#" + id + ": " + e.getMessage());
            }
        }
        out.setSent(sent);
        out.setFailed(errors.size());
        out.setErrors(errors);
        notificationService.notifyInvoiceBatchSent(sent, bookingIds.size(), sender.getFullName() != null ? sender.getFullName() : sender.getEmail());
        return out;
    }

    /**
     * PDF for a fully paid booking (same eligibility as email invoices).
     */
    @Transactional(readOnly = true)
    public InvoicePdfPayload getInvoicePdfPayload(Long bookingId, String lang) {
        Booking b = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        byte[] pdf = pdfForFullyPaidBooking(b, lang);
        return new InvoicePdfPayload(pdf, buildInvoiceFilename(b));
    }

    /**
     * Zip of PDFs for multiple fully paid bookings; duplicate refs get numeric suffixes.
     */
    @Transactional(readOnly = true)
    public byte[] getInvoicesZip(List<Long> bookingIds, String lang) throws IOException {
        if (bookingIds == null || bookingIds.isEmpty()) {
            throw new IllegalArgumentException("No bookings selected");
        }
        List<Long> ids = bookingIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("No bookings selected");
        }
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        Set<String> used = new HashSet<>();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Long id : ids) {
                InvoicePdfPayload payload = getInvoicePdfPayload(id, lang);
                String name = payload.filename();
                int n = 2;
                while (used.contains(name)) {
                    int dot = name.lastIndexOf('.');
                    String base = dot > 0 ? name.substring(0, dot) : name;
                    String ext = dot > 0 ? name.substring(dot) : ".pdf";
                    name = base + "-" + n + ext;
                    n++;
                }
                used.add(name);
                zos.putNextEntry(new ZipEntry(name));
                zos.write(payload.bytes());
                zos.closeEntry();
            }
            zos.finish();
        }
        return baos.toByteArray();
    }

    private byte[] pdfForFullyPaidBooking(Booking b, String lang) {
        if (!isFullyPaid(b)) {
            throw new IllegalArgumentException("Booking is not fully paid");
        }
        List<InvoicePaymentLineDto> lines = buildPaymentLinesForBooking(b.getId());
        return invoicePdfService.buildPdf(b, lines, lang);
    }

    private List<InvoicePaymentLineDto> buildPaymentLinesForBooking(Long bookingId) {
        List<Payment> income = paymentRepository.findByBooking_IdAndTypeOrderByRecordedAtAsc(bookingId, PaymentType.INCOME);
        List<DebtPayment> debt = debtPaymentRepository.findByBooking_IdOrderByPaymentDateAsc(bookingId);
        List<InvoicePaymentLineDto> lines = new ArrayList<>();
        for (Payment p : income) {
            lines.add(paymentToLine(p));
        }
        for (DebtPayment d : debt) {
            lines.add(debtToLine(d));
        }
        lines.sort(Comparator.comparing(InvoicePaymentLineDto::getRecordedAt, Comparator.nullsLast(String::compareTo)));
        return lines;
    }

    private static InvoicePaymentLineDto paymentToLine(Payment p) {
        InvoicePaymentLineDto dto = new InvoicePaymentLineDto();
        dto.setRecordedAt(p.getRecordedAt() != null ? p.getRecordedAt().toString() : "");
        dto.setAmount(p.getAmount());
        dto.setMethodLabel(formatPaymentMethod(p.getPaymentMethod()));
        dto.setDescription(p.getDescription() != null ? p.getDescription() : "");
        dto.setSource("Finance");
        return dto;
    }

    private static InvoicePaymentLineDto debtToLine(DebtPayment d) {
        InvoicePaymentLineDto dto = new InvoicePaymentLineDto();
        java.time.LocalDateTime when = d.getPaymentDate() != null
                ? d.getPaymentDate().atStartOfDay()
                : (d.getCreatedAt() != null ? d.getCreatedAt() : java.time.LocalDateTime.now());
        dto.setRecordedAt(when.toString());
        dto.setAmount(d.getAmount());
        String m = d.getPaymentMethod();
        dto.setMethodLabel(m != null && !m.isBlank() ? m : "—");
        dto.setDescription(d.getNote() != null ? d.getNote() : "");
        dto.setSource("Installment");
        return dto;
    }

    private static String formatPaymentMethod(PaymentMethod m) {
        if (m == null) {
            return "—";
        }
        return switch (m) {
            case CASH -> "Cash";
            case BANK_TRANSFER -> "Bank transfer";
            case MOBILE_MONEY -> "Mobile money";
            case OTHER -> "Other";
        };
    }

    private static String buildInvoiceFilename(Booking b) {
        String ref = b.getBookingReference();
        String base = ref != null && !ref.isBlank() ? sanitizeFileSegment(ref) : "booking-" + b.getId();
        return "Madeleine-Invoice-" + base + ".pdf";
    }

    private static String sanitizeFileSegment(String s) {
        String t = s.replaceAll("[^a-zA-Z0-9._-]+", "_");
        if (t.isBlank()) {
            return "invoice";
        }
        return t;
    }

    private static boolean isFullyPaid(Booking b) {
        BigDecimal est = b.getEstimatedAmount() != null ? b.getEstimatedAmount() : BigDecimal.ZERO;
        BigDecimal paid = b.getPaidAmount() != null ? b.getPaidAmount() : BigDecimal.ZERO;
        return est.compareTo(BigDecimal.ZERO) > 0 && paid.compareTo(est) >= 0;
    }

    private InvoiceEligibleRowDto toRow(Booking b, List<Payment> income, List<DebtPayment> debt) {
        InvoiceEligibleRowDto r = new InvoiceEligibleRowDto();
        r.setBookingId(b.getId());
        r.setBookingReference(b.getBookingReference());
        if (b.getClient() != null) {
            r.setClientName(b.getClient().getFullName());
            r.setClientEmail(b.getClient().getEmail());
        }
        if (b.getBranch() != null) {
            r.setBranchName(b.getBranch().getName());
        }
        r.setEventDate(b.getEventDate());
        r.setEstimatedAmount(b.getEstimatedAmount());
        r.setPaidAmount(b.getPaidAmount());
        List<InvoicePaymentLineDto> lines = new ArrayList<>();
        for (Payment p : income) {
            lines.add(paymentToLine(p));
        }
        for (DebtPayment d : debt) {
            lines.add(debtToLine(d));
        }
        lines.sort(Comparator.comparing(InvoicePaymentLineDto::getRecordedAt, Comparator.nullsLast(String::compareTo)));
        r.setPaymentLines(lines);
        return r;
    }
}
