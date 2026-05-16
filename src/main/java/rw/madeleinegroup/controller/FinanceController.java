package rw.madeleinegroup.controller;

import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import rw.madeleinegroup.common.ApiResponse;
import rw.madeleinegroup.common.enums.ExpenseStatus;
import rw.madeleinegroup.dto.ExpenseRejectRequest;
import rw.madeleinegroup.dto.ExpenseRequest;
import rw.madeleinegroup.dto.FinanceSummaryDto;
import rw.madeleinegroup.dto.InvoiceEligibleRowDto;
import rw.madeleinegroup.dto.InvoicePdfPayload;
import rw.madeleinegroup.dto.InvoiceSendRequest;
import rw.madeleinegroup.dto.InvoiceSendResultDto;
import rw.madeleinegroup.dto.PaymentRequest;
import rw.madeleinegroup.entity.Payment;
import rw.madeleinegroup.service.CustomUserDetailsService;
import rw.madeleinegroup.service.FinanceService;
import rw.madeleinegroup.service.InvoiceService;
import rw.madeleinegroup.service.PaymentService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private final PaymentService paymentService;
    private final FinanceService financeService;
    private final InvoiceService invoiceService;

    public FinanceController(PaymentService paymentService, FinanceService financeService,
                             InvoiceService invoiceService) {
        this.paymentService = paymentService;
        this.financeService = financeService;
        this.invoiceService = invoiceService;
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<?>> listPayments(@RequestParam(required = false) Long branchId) {
        return ResponseEntity.ok(ApiResponse.success(financeService.listAllPayments(branchId), "OK"));
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<ApiResponse<?>> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(financeService.getPaymentById(id), "OK"));
    }

    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<Payment>> recordPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        Payment payment = paymentService.recordPayment(request, principal != null ? principal.getEmail() : null);
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment recorded successfully"));
    }

    @PostMapping("/expenses")
    public ResponseEntity<ApiResponse<?>> recordExpense(
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        Object expense = financeService.recordExpense(request, principal != null ? principal.getEmail() : null);
        return ResponseEntity.ok(ApiResponse.success(expense, "Expense recorded successfully"));
    }

    @GetMapping("/expenses")
    public ResponseEntity<ApiResponse<?>> listExpenses(@RequestParam(required = false) Long branchId,
                                                       @RequestParam(required = false) String status) {
        ExpenseStatus st = null;
        if (status != null && !status.isBlank()) {
            try {
                st = ExpenseStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                st = null;
            }
        }
        return ResponseEntity.ok(ApiResponse.success(financeService.listAllExpenses(branchId, st), "OK"));
    }

    @GetMapping("/expenses/{id}")
    public ResponseEntity<ApiResponse<?>> getExpense(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(financeService.getExpenseById(id), "OK"));
    }

    @PostMapping("/expenses/{id}/first-approve")
    public ResponseEntity<ApiResponse<?>> firstApproveExpense(@PathVariable Long id,
                                                              @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal p) {
        return ResponseEntity.ok(ApiResponse.success(
                financeService.firstApprove(id, principalEmail(p)), "First approval recorded"));
    }

    @PostMapping("/expenses/{id}/second-approve")
    public ResponseEntity<ApiResponse<?>> secondApproveExpense(@PathVariable Long id,
                                                               @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal p) {
        return ResponseEntity.ok(ApiResponse.success(
                financeService.secondApprove(id, principalEmail(p)), "Expense finalized as paid"));
    }

    @PostMapping("/expenses/{id}/reject")
    public ResponseEntity<ApiResponse<?>> rejectExpense(@PathVariable Long id,
                                                       @RequestBody(required = false) ExpenseRejectRequest req,
                                                       @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal p) {
        ExpenseRejectRequest body = req != null ? req : new ExpenseRejectRequest();
        return ResponseEntity.ok(ApiResponse.success(
                financeService.rejectExpense(id, body, principalEmail(p)), "Expense rejected"));
    }

    private static String principalEmail(CustomUserDetailsService.UserPrincipal p) {
        return p != null ? p.getEmail() : null;
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBranchFinance(
            @PathVariable Long branchId,
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month) {
        Map<String, Object> result = financeService.getBranchFinance(branchId, year, month);
        return ResponseEntity.ok(ApiResponse.success(result, "Branch finance retrieved"));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary(
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month) {
        Map<String, Object> result = financeService.getGroupFinance(year, month);
        return ResponseEntity.ok(ApiResponse.success(result, "Finance summary retrieved"));
    }

    @GetMapping("/summary/monthly")
    public ResponseEntity<ApiResponse<FinanceSummaryDto>> getMonthlySummary(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        FinanceSummaryDto summary = financeService.getMonthlySummary(year, month);
        return ResponseEntity.ok(ApiResponse.success(summary, "Monthly summary retrieved"));
    }

    @GetMapping("/summary/yearly")
    public ResponseEntity<ApiResponse<FinanceSummaryDto>> getYearlySummary(@RequestParam Integer year) {
        FinanceSummaryDto summary = financeService.getYearlySummary(year);
        return ResponseEntity.ok(ApiResponse.success(summary, "Yearly summary retrieved"));
    }

    @GetMapping("/analytics/monthly-trend")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMonthlyTrend(@RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(financeService.getMonthlyTrend(year), "OK"));
    }

    @GetMapping("/analytics/by-category")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getByCategory(
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(ApiResponse.success(financeService.getExpensesByCategory(year, month), "OK"));
    }

    @GetMapping("/analytics/by-branch")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getByBranch(
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(ApiResponse.success(financeService.getIncomeByBranch(year, month), "OK"));
    }

    @GetMapping("/analytics/cashflow")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCashflow(@RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(financeService.getCashflow(year), "OK"));
    }

    @GetMapping("/analytics/kpis")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getKpis(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        return ResponseEntity.ok(ApiResponse.success(financeService.getKpis(year, month), "OK"));
    }

    /** Fully paid bookings — send invoice / receipt by email to clients. */
    @GetMapping("/invoices/eligible")
    public ResponseEntity<ApiResponse<List<InvoiceEligibleRowDto>>> listInvoiceEligible() {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.listFullyPaidEligible(), "OK"));
    }

    @PostMapping("/invoices/send")
    public ResponseEntity<ApiResponse<InvoiceSendResultDto>> sendInvoices(
            @Valid @RequestBody InvoiceSendRequest body,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        String email = principalEmail(principal);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(ApiResponse.success(invoiceService.sendInvoices(body.getBookingIds(), email), "OK"));
    }

    /** PDF invoice / receipt for one fully paid booking (same rules as email). */
    @GetMapping(value = "/invoices/{bookingId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long bookingId,
                                                     @RequestParam(required = false) String lang,
                                                     @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        try {
            String resolvedLang = resolveLang(lang, acceptLanguage);
            InvoicePdfPayload p = invoiceService.getInvoicePdfPayload(bookingId, resolvedLang);
            return binaryFileResponse(p.bytes(), MediaType.APPLICATION_PDF, p.filename());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    /** ZIP of PDF invoices for multiple fully paid bookings. */
    @PostMapping(value = "/invoices/download-zip", produces = "application/zip")
    public ResponseEntity<byte[]> downloadInvoicesZip(@Valid @RequestBody InvoiceSendRequest body,
                                                      @RequestParam(required = false) String lang,
                                                      @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        try {
            String resolvedLang = resolveLang(lang, acceptLanguage);
            byte[] zip = invoiceService.getInvoicesZip(body.getBookingIds(), resolvedLang);
            return binaryFileResponse(zip, MediaType.parseMediaType("application/zip"), "madeleine-invoices.zip");
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not build ZIP archive");
        }
    }

    private static ResponseEntity<byte[]> binaryFileResponse(byte[] data, MediaType type, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
        headers.setContentLength(data.length);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    private static String resolveLang(String requestedLang, String acceptLanguage) {
        String direct = normalizeLang(requestedLang);
        if (direct != null) return direct;
        if (!StringUtils.hasText(acceptLanguage)) return "en";
        String lower = acceptLanguage.toLowerCase(Locale.ROOT);
        return lower.startsWith("fr") ? "fr" : "en";
    }

    private static String normalizeLang(String lang) {
        if (!StringUtils.hasText(lang)) return null;
        String normalized = lang.trim().toLowerCase(Locale.ROOT);
        if ("fr".equals(normalized) || "fr-fr".equals(normalized)) return "fr";
        if ("en".equals(normalized) || "en-gb".equals(normalized) || "en-us".equals(normalized)) return "en";
        return null;
    }
}
