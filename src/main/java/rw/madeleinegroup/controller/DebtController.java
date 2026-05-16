package rw.madeleinegroup.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import rw.madeleinegroup.common.ApiResponse;
import rw.madeleinegroup.dto.*;
import rw.madeleinegroup.service.CustomUserDetailsService;
import rw.madeleinegroup.service.DebtService;

@RestController
@RequestMapping("/api/finance/debts")
public class DebtController {

    private final DebtService debtService;

    public DebtController(DebtService debtService) {
        this.debtService = debtService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DebtListResponse>> list(
            @RequestParam(required = false) Long branchId) {
        return ResponseEntity.ok(ApiResponse.success(debtService.listOutstanding(branchId), "OK"));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<DebtDetailResponse>> detail(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success(debtService.getBookingDebtDetail(bookingId), "OK"));
    }

    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<DebtPaymentItemDto>> recordPayment(
            @Valid @RequestBody DebtPaymentRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        String email = principal != null ? principal.getEmail() : null;
        DebtPaymentItemDto dto = debtService.recordDebtPayment(request, email);
        return ResponseEntity.ok(ApiResponse.success(dto, "Debt payment recorded"));
    }

    @PostMapping("/reminders")
    public ResponseEntity<ApiResponse<DebtReminderItemDto>> recordReminder(
            @Valid @RequestBody DebtReminderRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        String email = principal != null ? principal.getEmail() : null;
        DebtReminderItemDto dto = debtService.recordReminder(request, email);
        return ResponseEntity.ok(ApiResponse.success(dto, "Reminder logged"));
    }

    @PatchMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<DebtBookingRowDto>> patchDebtFields(
            @PathVariable Long bookingId,
            @RequestBody DebtBookingDebtPatchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(debtService.patchBookingDebt(bookingId, request), "Updated"));
    }
}
