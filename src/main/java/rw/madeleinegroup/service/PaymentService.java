package rw.madeleinegroup.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import rw.madeleinegroup.common.enums.PaymentStatus;
import rw.madeleinegroup.dto.PaymentRequest;
import rw.madeleinegroup.entity.*;
import rw.madeleinegroup.repository.BookingRepository;
import rw.madeleinegroup.repository.BranchRepository;
import rw.madeleinegroup.repository.PaymentRepository;
import rw.madeleinegroup.repository.UserRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public PaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository,
                          BranchRepository branchRepository, UserRepository userRepository,
                          NotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    @CacheEvict(value = {"financeKpis", "monthlyTrend", "categoryBreakdown", "branchRevenue", "aiLiveSnapshot"}, allEntries = true)
    public Payment recordPayment(PaymentRequest request, String currentUserEmail) {
        User recordedBy = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));
        Booking booking = request.getBookingId() != null ?
                bookingRepository.findById(request.getBookingId()).orElse(null) : null;

        Payment.PaymentType ptype = Payment.PaymentType.valueOf(request.getType());
        if (booking != null && ptype == Payment.PaymentType.INCOME) {
            BigDecimal estimated = booking.getEstimatedAmount() != null ? booking.getEstimatedAmount() : BigDecimal.ZERO;
            BigDecimal alreadyPaid = booking.getPaidAmount() != null ? booking.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal remaining = estimated.subtract(alreadyPaid);
            if (request.getAmount().compareTo(remaining) > 0) {
                throw new IllegalArgumentException("Le montant (" + request.getAmount() + " RWF) dépasse le solde restant pour cette réservation (" + remaining + " RWF).");
            }
        }

        Payment payment = Payment.builder()
                .branch(branch)
                .booking(booking)
                .type(ptype)
                .amount(request.getAmount())
                .description(request.getDescription())
                .recordedBy(recordedBy)
                .build();
        if (booking != null && booking.getClient() != null) {
            payment.setClient(booking.getClient());
        }
        if (request.getPaymentMethod() != null) payment.setPaymentMethod(request.getPaymentMethod());
        payment = paymentRepository.save(payment);

        if (booking != null && ptype == Payment.PaymentType.INCOME) {
            BigDecimal newPaid = booking.getPaidAmount() != null ?
                    booking.getPaidAmount().add(request.getAmount()) : request.getAmount();
            booking.setPaidAmount(newPaid);
            bookingRepository.save(booking);
            BigDecimal estimated = booking.getEstimatedAmount() != null ? booking.getEstimatedAmount() : BigDecimal.ZERO;
            BigDecimal remaining = estimated.subtract(newPaid);
            if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;
            payment.setRemainingBalance(remaining);
            payment.setPaymentStatus(request.getPaymentStatus() != null ? request.getPaymentStatus() :
                    (remaining.compareTo(BigDecimal.ZERO) == 0 ? PaymentStatus.PAID : PaymentStatus.PARTIAL));
            payment = paymentRepository.save(payment);
        }

        notificationService.notifyPaymentRecorded(payment, recordedBy);
        return payment;
    }
}
