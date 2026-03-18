package rw.madeleinegroup.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import rw.madeleinegroup.dto.*;
import rw.madeleinegroup.entity.Booking;
import rw.madeleinegroup.entity.BookingStatus;
import rw.madeleinegroup.service.BookingService;
import rw.madeleinegroup.service.CustomUserDetailsService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest request,
                                           @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Authentication required to create a booking");
        }
        Booking booking = bookingService.createBooking(request, principal.getId());
        return ResponseEntity.ok(Map.of("id", booking.getId(), "bookingReference", booking.getBookingReference()));
    }

    @GetMapping
    public ResponseEntity<?> getAllBookings(@RequestParam(required = false) Long branchId,
                                            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(bookingService.findAllAsResponse(branchId, status != null ? status : null));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchBookings(@RequestParam(required = false) String query,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) Long branchId,
                                            @RequestParam(required = false) String eventType,
                                            @RequestParam(required = false) LocalDate dateFrom,
                                            @RequestParam(required = false) LocalDate dateTo,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(bookingService.searchBookings(query, status, branchId, eventType, dateFrom, dateTo, page, size));
    }

    @GetMapping("/available-dates")
    public ResponseEntity<?> getAvailableDates(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(bookingService.getUnavailableDates(year, month));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getBookingStats() {
        return ResponseEntity.ok(bookingService.getBookingStats());
    }

    @GetMapping("/calendar")
    public ResponseEntity<?> getBookingsForCalendar(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(bookingService.getBookingsForCalendar(year, month));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getBookingsByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(bookingService.getBookingsByClientAsResponse(clientId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.findByIdAsResponse(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id, @Valid @RequestBody BookingRequest request,
                                          @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Authentication required to update a booking");
        }
        return ResponseEntity.ok(bookingService.updateBooking(id, request, principal.getId()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CEO', 'MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body,
                                         @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        String statusStr = body != null ? body.get("status") : null;
        if (statusStr == null || statusStr.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }
        BookingStatus status = BookingStatus.valueOf(statusStr.toUpperCase());
        Long modifierId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(bookingService.updateStatus(id, status, modifierId));
    }

    @PatchMapping("/{id}/notes")
    public ResponseEntity<?> updateAdminNotes(@PathVariable Long id, @RequestBody AdminNotesRequest request) {
        String adminNotes = request != null ? request.getAdminNotes() : null;
        return ResponseEntity.ok(bookingService.updateAdminNotes(id, adminNotes));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmBooking(@PathVariable Long id,
                                            @RequestBody BookingConfirmationRequest request,
                                            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Authentication required to confirm a booking");
        }
        return ResponseEntity.ok(bookingService.confirmBooking(id, request, principal.getId()));
    }

    @GetMapping("/{id}/payment-summary")
    public ResponseEntity<?> getPaymentSummary(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getPaymentSummary(id));
    }
}
