package rw.madeleinegroup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.madeleinegroup.common.ApiResponse;
import rw.madeleinegroup.dto.PaymentAnalyticsResponse;
import rw.madeleinegroup.dto.PaymentTopBranchDto;
import rw.madeleinegroup.entity.Booking;
import rw.madeleinegroup.exception.ResourceNotFoundException;
import rw.madeleinegroup.repository.BookingRepository;
import rw.madeleinegroup.service.EmailService;
import rw.madeleinegroup.service.PaymentService;

import java.math.BigDecimal;
import java.util.Map;

/**
 * REST controller for payment-related endpoints (confirmation emails, etc.).
 * Base path: /api/payments
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final BookingRepository bookingRepository;
    private final EmailService emailService;
    private final PaymentService paymentService;

    public PaymentController(BookingRepository bookingRepository, EmailService emailService,
                             PaymentService paymentService) {
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
        this.paymentService = paymentService;
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<PaymentAnalyticsResponse>> getPaymentAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentAnalytics(), "OK"));
    }

    @GetMapping("/methods")
    public ResponseEntity<ApiResponse<java.util.List<String>>> listPaymentMethods() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.listPaymentMethodNames(), "OK"));
    }

    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<java.util.List<String>>> listPaymentStatuses() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.listPaymentStatusNames(), "OK"));
    }

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<java.util.List<String>>> listPaymentTypes() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.listPaymentTypeNames(), "OK"));
    }

    @GetMapping("/by-branch/{branchId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentsByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentsByBranch(branchId), "OK"));
    }

    @GetMapping("/top-branch")
    public ResponseEntity<ApiResponse<PaymentTopBranchDto>> getTopBranch() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getTopBranch(), "OK"));
    }

    /**
     * Sends a payment confirmation email to the client when the booking is fully paid.
     * Request body: { "bookingId": number, "verifiedByUserId": number | null }
     */
    @PostMapping("/send-confirmation-email")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendConfirmationEmail(@RequestBody Map<String, Object> body) {
        Object bid = body.get("bookingId");
        if (bid == null) {
            throw new IllegalArgumentException("bookingId is required");
        }
        Long bookingId = bid instanceof Number ? ((Number) bid).longValue() : Long.parseLong(bid.toString());

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        BigDecimal estimated = booking.getEstimatedAmount() != null ? booking.getEstimatedAmount() : BigDecimal.ZERO;
        BigDecimal paid = booking.getPaidAmount() != null ? booking.getPaidAmount() : BigDecimal.ZERO;
        if (paid.compareTo(estimated) < 0) {
            throw new IllegalArgumentException("Payment is not complete for this booking. Cannot send confirmation email.");
        }

        if (booking.getClient() == null || booking.getClient().getEmail() == null || booking.getClient().getEmail().isBlank()) {
            throw new IllegalArgumentException("Client email not available for this booking.");
        }

        String clientEmail = booking.getClient().getEmail().trim();
        String clientName = booking.getClient().getFullName() != null ? booking.getClient().getFullName() : "Client";
        String ref = booking.getBookingReference();
        String eventType = booking.getEventType() != null ? booking.getEventType() : "";
        String eventDateStr = booking.getEventDate() != null ? booking.getEventDate().toString() : "";
        String totalRwf = estimated.toPlainString();

        emailService.sendPaymentConfirmationEmailSync(clientEmail, clientName, ref, eventType, eventDateStr, totalRwf);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("clientEmail", clientEmail),
                "Payment confirmation email sent successfully"));
    }
}
