package rw.madeleinegroup.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import rw.madeleinegroup.common.ApiResponse;
import rw.madeleinegroup.dto.ExpenseRequest;
import rw.madeleinegroup.dto.FinanceSummaryDto;
import rw.madeleinegroup.dto.PaymentRequest;
import rw.madeleinegroup.entity.Payment;
import rw.madeleinegroup.service.CustomUserDetailsService;
import rw.madeleinegroup.service.FinanceService;
import rw.madeleinegroup.service.PaymentService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private final PaymentService paymentService;
    private final FinanceService financeService;

    public FinanceController(PaymentService paymentService, FinanceService financeService) {
        this.paymentService = paymentService;
        this.financeService = financeService;
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
    public ResponseEntity<ApiResponse<?>> listExpenses(@RequestParam(required = false) Long branchId) {
        return ResponseEntity.ok(ApiResponse.success(financeService.listAllExpenses(branchId), "OK"));
    }

    @GetMapping("/expenses/{id}")
    public ResponseEntity<ApiResponse<?>> getExpense(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(financeService.getExpenseById(id), "OK"));
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
}
