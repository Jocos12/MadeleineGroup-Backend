package rw.madeleinegroup.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import rw.madeleinegroup.common.enums.PaymentMethod;
import rw.madeleinegroup.dto.ExpenseRequest;
import rw.madeleinegroup.dto.ExpenseUpdateRequest;
import rw.madeleinegroup.dto.PaymentResponse;
import rw.madeleinegroup.dto.PaymentUpdateRequest;
import rw.madeleinegroup.entity.*;
import rw.madeleinegroup.exception.ResourceNotFoundException;
import rw.madeleinegroup.repository.*;
import rw.madeleinegroup.dto.FinanceSummaryDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FinanceService {

    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ClientRepository clientRepository;

    public FinanceService(PaymentRepository paymentRepository, ExpenseRepository expenseRepository,
                          BranchRepository branchRepository, UserRepository userRepository,
                          BookingRepository bookingRepository, ClientRepository clientRepository) {
        this.paymentRepository = paymentRepository;
        this.expenseRepository = expenseRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional
    @CacheEvict(value = {"financeKpis", "monthlyTrend", "categoryBreakdown", "branchRevenue", "aiLiveSnapshot"}, allEntries = true)
    public Expense recordExpense(ExpenseRequest request, String currentUserEmail) {
        User recordedBy = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        Expense expense = new Expense();
        expense.setRecordedBy(recordedBy);
        expense.setBranch(branch);
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate() != null ? request.getExpenseDate() : LocalDate.now());
        expense.setReceiptUrl(request.getReceiptUrl());
        return expenseRepository.save(expense);
    }

    public Map<String, Object> getBranchFinance(Long branchId, Integer year, Integer month) {
        LocalDateTime start = month != null ?
                YearMonth.of(year, month).atDay(1).atStartOfDay() :
                java.time.LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = month != null ?
                YearMonth.of(year, month).atEndOfMonth().atTime(23, 59, 59) :
                java.time.LocalDate.of(year, 12, 31).atTime(23, 59, 59);

        BigDecimal income = paymentRepository.sumByBranchAndTypeAndDateRange(branchId, Payment.PaymentType.INCOME, start, end);
        BigDecimal expense = paymentRepository.sumByBranchAndTypeAndDateRange(branchId, Payment.PaymentType.EXPENSE, start, end);
        BigDecimal balance = (income != null ? income : BigDecimal.ZERO).subtract(expense != null ? expense : BigDecimal.ZERO);

        Map<String, Object> result = new HashMap<>();
        result.put("branchId", branchId);
        result.put("year", year);
        result.put("month", month);
        result.put("income", income);
        result.put("expense", expense);
        result.put("balance", balance);
        return result;
    }

    public Map<String, Object> getGroupFinance(Integer year, Integer month) {
        LocalDateTime start = month != null ?
                YearMonth.of(year, month).atDay(1).atStartOfDay() :
                java.time.LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = month != null ?
                YearMonth.of(year, month).atEndOfMonth().atTime(23, 59, 59) :
                java.time.LocalDate.of(year, 12, 31).atTime(23, 59, 59);

        BigDecimal totalIncome = paymentRepository.sumTotalByTypeAndDateRange(Payment.PaymentType.INCOME, start, end);
        BigDecimal totalExpense = paymentRepository.sumTotalByTypeAndDateRange(Payment.PaymentType.EXPENSE, start, end);
        BigDecimal balance = (totalIncome != null ? totalIncome : BigDecimal.ZERO).subtract(totalExpense != null ? totalExpense : BigDecimal.ZERO);

        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("month", month);
        result.put("totalIncome", totalIncome != null ? totalIncome : BigDecimal.ZERO);
        result.put("totalExpense", totalExpense != null ? totalExpense : BigDecimal.ZERO);
        result.put("balance", balance != null ? balance : BigDecimal.ZERO);
        return result;
    }

    public FinanceSummaryDto getMonthlySummary(Integer year, Integer month) {
        LocalDateTime start = YearMonth.of(year, month).atDay(1).atStartOfDay();
        LocalDateTime end = YearMonth.of(year, month).atEndOfMonth().atTime(23, 59, 59);
        BigDecimal income = paymentRepository.sumTotalByTypeAndDateRange(Payment.PaymentType.INCOME, start, end);
        BigDecimal expSum = expenseRepository.sumTotalByDateRange(
                YearMonth.of(year, month).atDay(1),
                YearMonth.of(year, month).atEndOfMonth());
        BigDecimal paymentExpenses = paymentRepository.sumTotalByTypeAndDateRange(Payment.PaymentType.EXPENSE, start, end);
        BigDecimal totalExp = (expSum != null ? expSum : BigDecimal.ZERO).add(paymentExpenses != null ? paymentExpenses : BigDecimal.ZERO);
        FinanceSummaryDto dto = new FinanceSummaryDto();
        dto.setTotalIncome(income != null ? income : BigDecimal.ZERO);
        dto.setTotalExpenses(totalExp);
        dto.setNetProfit((income != null ? income : BigDecimal.ZERO).subtract(totalExp));
        dto.setPeriod(month);
        dto.setYear(year);
        return dto;
    }

    public FinanceSummaryDto getYearlySummary(Integer year) {
        LocalDateTime start = java.time.LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = java.time.LocalDate.of(year, 12, 31).atTime(23, 59, 59);
        BigDecimal income = paymentRepository.sumTotalByTypeAndDateRange(Payment.PaymentType.INCOME, start, end);
        BigDecimal expSum = expenseRepository.sumTotalByDateRange(
                java.time.LocalDate.of(year, 1, 1),
                java.time.LocalDate.of(year, 12, 31));
        BigDecimal paymentExpenses = paymentRepository.sumTotalByTypeAndDateRange(Payment.PaymentType.EXPENSE, start, end);
        BigDecimal totalExp = (expSum != null ? expSum : BigDecimal.ZERO).add(paymentExpenses != null ? paymentExpenses : BigDecimal.ZERO);
        FinanceSummaryDto dto = new FinanceSummaryDto();
        dto.setTotalIncome(income != null ? income : BigDecimal.ZERO);
        dto.setTotalExpenses(totalExp);
        dto.setNetProfit((income != null ? income : BigDecimal.ZERO).subtract(totalExp));
        dto.setYear(year);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listAllPayments(Long branchId) {
        List<Payment> payments = paymentRepository.findAllWithDetails(branchId);
        return payments.stream().map(PaymentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> searchPayments(String query, String type, Long branchId, String paymentMethodStr,
                                              String paymentStatusStr, LocalDate dateFrom, LocalDate dateTo,
                                              String sortBy, String sortDir, int page, int size) {
        PaymentMethod paymentMethod = (paymentMethodStr != null && !paymentMethodStr.isBlank())
                ? PaymentMethod.valueOf(paymentMethodStr.toUpperCase()) : null;
        LocalDateTime dtFrom = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime dtTo = dateTo != null ? dateTo.atTime(LocalTime.MAX) : null;
        Specification<Payment> spec = PaymentSpecification.searchPayments(query, type, branchId, paymentMethod, paymentStatusStr, dtFrom, dtTo);
        Sort sort = Sort.by("asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy != null && !sortBy.isBlank() ? sortBy : "recordedAt");
        Page<Payment> pageResult = paymentRepository.findAll(spec, PageRequest.of(page, size, sort));
        List<PaymentResponse> content = pageResult.getContent().stream().map(PaymentResponse::from).toList();
        Map<String, Object> result = new HashMap<>();
        result.put("content", content);
        result.put("totalCount", pageResult.getTotalElements());
        result.put("totalPages", pageResult.getTotalPages());
        result.put("currentPage", page);
        result.put("pageSize", size);
        return result;
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment p = paymentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return PaymentResponse.from(p);
    }

    @Transactional
    @CacheEvict(value = {"financeKpis", "monthlyTrend", "categoryBreakdown", "branchRevenue", "aiLiveSnapshot"}, allEntries = true)
    public PaymentResponse updatePayment(Long id, PaymentUpdateRequest request, String currentUserEmail) {
        if (request.getUpdateReason() == null || request.getUpdateReason().isBlank()) {
            throw new IllegalArgumentException("Update reason is required");
        }
        User updatedBy = userRepository.findByEmail(currentUserEmail).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Payment p = paymentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        if (request.getBranchId() != null) {
            Branch b = branchRepository.findById(request.getBranchId()).orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
            p.setBranch(b);
        }
        if (request.getBookingId() != null) {
            Booking bk = bookingRepository.findById(request.getBookingId()).orElse(null);
            p.setBooking(bk);
        }
        if (request.getClientId() != null) {
            Client c = clientRepository.findById(request.getClientId()).orElseThrow(() -> new ResourceNotFoundException("Client not found"));
            p.setClient(c);
        }
        if (request.getType() != null) p.setType(Payment.PaymentType.valueOf(request.getType().toUpperCase()));
        if (request.getAmount() != null) p.setAmount(request.getAmount());
        if (request.getRemainingBalance() != null) p.setRemainingBalance(request.getRemainingBalance());
        if (request.getPaymentMethod() != null) p.setPaymentMethod(request.getPaymentMethod());
        if (request.getPaymentStatus() != null) p.setPaymentStatus(request.getPaymentStatus());
        if (request.getDescription() != null) p.setDescription(request.getDescription());
        p.setUpdatedBy(updatedBy);
        p.setUpdatedAt(LocalDateTime.now());
        p.setUpdateReason(request.getUpdateReason().trim());
        p = paymentRepository.save(p);
        return PaymentResponse.from(p);
    }

    @Transactional
    @CacheEvict(value = {"financeKpis", "monthlyTrend", "categoryBreakdown", "branchRevenue", "aiLiveSnapshot"}, allEntries = true)
    public void deletePayment(Long id) {
        Payment p = paymentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        paymentRepository.delete(p);
    }

    public List<Expense> listAllExpenses(Long branchId) {
        return branchId != null
                ? expenseRepository.findByBranch_IdOrderByCreatedAtDesc(branchId)
                : expenseRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
    }

    @Transactional
    @CacheEvict(value = {"financeKpis", "monthlyTrend", "categoryBreakdown", "branchRevenue", "aiLiveSnapshot"}, allEntries = true)
    public Expense updateExpense(Long id, ExpenseUpdateRequest request, String currentUserEmail) {
        if (request.getUpdateReason() == null || request.getUpdateReason().isBlank()) {
            throw new IllegalArgumentException("Update reason is required");
        }
        User updatedBy = userRepository.findByEmail(currentUserEmail).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
            expense.setBranch(branch);
        }
        if (request.getCategory() != null) expense.setCategory(request.getCategory());
        if (request.getDescription() != null) expense.setDescription(request.getDescription());
        if (request.getAmount() != null) expense.setAmount(request.getAmount());
        if (request.getExpenseDate() != null) expense.setExpenseDate(request.getExpenseDate());
        if (request.getReceiptUrl() != null) expense.setReceiptUrl(request.getReceiptUrl());
        expense.setUpdatedBy(updatedBy);
        expense.setUpdatedAt(LocalDateTime.now());
        expense.setUpdateReason(request.getUpdateReason().trim());
        return expenseRepository.save(expense);
    }

    @Transactional
    @CacheEvict(value = {"financeKpis", "monthlyTrend", "categoryBreakdown", "branchRevenue", "aiLiveSnapshot"}, allEntries = true)
    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        expenseRepository.delete(expense);
    }

    private static final String[] MONTH_NAMES = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    @Transactional(readOnly = true)
    @Cacheable(value = "monthlyTrend", key = "#year")
    public List<Map<String, Object>> getMonthlyTrend(int year) {
        List<Object[]> incomeRows = paymentRepository.findMonthlyIncome(year);
        List<Object[]> expenseRows = expenseRepository.findMonthlyExpenses(year);
        Map<Integer, BigDecimal> incomeByMonth = new HashMap<>();
        for (Object[] row : incomeRows) {
            Integer month = ((Number) row[0]).intValue();
            incomeByMonth.put(month, (BigDecimal) row[1]);
        }
        Map<Integer, BigDecimal> expenseByMonth = new HashMap<>();
        for (Object[] row : expenseRows) {
            Integer month = ((Number) row[0]).intValue();
            expenseByMonth.put(month, (BigDecimal) row[1]);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            BigDecimal inc = incomeByMonth.getOrDefault(m, BigDecimal.ZERO);
            BigDecimal exp = expenseByMonth.getOrDefault(m, BigDecimal.ZERO);
            result.add(Map.of(
                    "month", m,
                    "monthName", MONTH_NAMES[m - 1],
                    "income", inc != null ? inc : BigDecimal.ZERO,
                    "expenses", exp != null ? exp : BigDecimal.ZERO,
                    "netProfit", (inc != null ? inc : BigDecimal.ZERO).subtract(exp != null ? exp : BigDecimal.ZERO)
            ));
        }
        return result;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "categoryBreakdown", key = "#year + '-' + (#month != null ? #month : 'all')")
    public List<Map<String, Object>> getExpensesByCategory(Integer year, Integer month) {
        List<Object[]> rows = expenseRepository.findExpensesByCategory(year);
        if (month != null) {
            LocalDate start = YearMonth.of(year, month).atDay(1);
            LocalDate end = YearMonth.of(year, month).atEndOfMonth();
            List<Expense> all = expenseRepository.findAllByOrderByCreatedAtDesc();
            Map<String, BigDecimal> byCat = new HashMap<>();
            for (Expense e : all) {
                if (e.getExpenseDate() == null || e.getExpenseDate().isBefore(start) || e.getExpenseDate().isAfter(end)) continue;
                String cat = e.getCategory() != null ? e.getCategory() : "OTHER";
                byCat.merge(cat, e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO, BigDecimal::add);
            }
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map.Entry<String, BigDecimal> entry : byCat.entrySet()) {
                result.add(Map.of("category", entry.getKey(), "amount", entry.getValue()));
            }
            result.sort((a, b) -> ((BigDecimal) b.get("amount")).compareTo((BigDecimal) a.get("amount")));
            return result;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(Map.of("category", row[0] != null ? row[0].toString() : "OTHER", "amount", row[1] != null ? row[1] : BigDecimal.ZERO));
        }
        result.sort((a, b) -> ((BigDecimal) b.get("amount")).compareTo((BigDecimal) a.get("amount")));
        return result;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "branchRevenue", key = "#year + '-' + (#month != null ? #month : 'all')")
    public List<Map<String, Object>> getIncomeByBranch(Integer year, Integer month) {
        LocalDateTime start = month != null ? YearMonth.of(year, month).atDay(1).atStartOfDay() : LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = month != null ? YearMonth.of(year, month).atEndOfMonth().atTime(23, 59, 59) : LocalDate.of(year, 12, 31).atTime(23, 59, 59);
        List<Branch> branches = branchRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Branch b : branches) {
            BigDecimal income = paymentRepository.sumByBranchAndTypeAndDateRange(b.getId(), Payment.PaymentType.INCOME, start, end);
            result.add(Map.of("branchId", b.getId(), "branchName", b.getName() != null ? b.getName() : "N/A", "income", income != null ? income : BigDecimal.ZERO));
        }
        result.sort((a, b) -> ((BigDecimal) b.get("income")).compareTo((BigDecimal) a.get("income")));
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCashflow(int year) {
        List<Map<String, Object>> trend = getMonthlyTrend(year);
        return trend.stream().map(m -> Map.<String, Object>of(
                "month", m.get("month"),
                "monthName", m.get("monthName"),
                "netCashflow", m.get("netProfit")
        )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "financeKpis", key = "#year + '-' + (#month != null ? #month : 'all')")
    public Map<String, Object> getKpis(Integer year, Integer month) {
        LocalDate start = YearMonth.of(year, month).atDay(1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(23, 59, 59);

        BigDecimal totalIncome = paymentRepository.sumTotalByTypeAndDateRange(Payment.PaymentType.INCOME, startDt, endDt);
        BigDecimal paymentExpenses = paymentRepository.sumTotalByTypeAndDateRange(Payment.PaymentType.EXPENSE, startDt, endDt);
        BigDecimal expenseAmount = expenseRepository.sumTotalByDateRange(start, end);
        BigDecimal totalExpenses = (paymentExpenses != null ? paymentExpenses : BigDecimal.ZERO).add(expenseAmount != null ? expenseAmount : BigDecimal.ZERO);
        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        BigDecimal netProfit = totalIncome.subtract(totalExpenses);
        double profitMarginPercent = totalIncome.doubleValue() > 0
                ? (netProfit.doubleValue() / totalIncome.doubleValue() * 100) : 0;

        BigDecimal pendingAmount = bookingRepository.sumPendingAmountByEventDateBetween(start, end);
        if (pendingAmount == null) pendingAmount = BigDecimal.ZERO;

        long totalBookings = bookingRepository.countByEventDateBetween(start, end);
        long completedBookings = bookingRepository.countByEventDateBetweenAndStatus(start, end, BookingStatus.COMPLETED);
        long pendingBookings = bookingRepository.countByEventDateBetweenAndStatus(start, end, BookingStatus.PENDING);
        long cancelledBookings = bookingRepository.countByEventDateBetweenAndStatus(start, end, BookingStatus.CANCELLED);
        long confirmedBookings = bookingRepository.countByEventDateBetweenAndStatus(start, end, BookingStatus.CONFIRMED);

        long totalClients = clientRepository.count();

        List<Map<String, Object>> byCat = getExpensesByCategory(year, month);
        String topExpenseCategory = byCat.isEmpty() ? null : (String) byCat.get(0).get("category");

        List<Map<String, Object>> trend = getMonthlyTrend(year);
        String bestMonth = null;
        BigDecimal bestNet = null;
        for (Map<String, Object> t : trend) {
            BigDecimal net = (BigDecimal) t.get("netProfit");
            if (bestNet == null || net.compareTo(bestNet) > 0) {
                bestNet = net;
                bestMonth = (String) t.get("monthName");
            }
        }

        int yearForAvg = year;
        List<Map<String, Object>> fullYearTrend = getMonthlyTrend(yearForAvg);
        BigDecimal avgIncome = fullYearTrend.stream().map(m -> (BigDecimal) m.get("income")).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgExpense = fullYearTrend.stream().map(m -> (BigDecimal) m.get("expenses")).reduce(BigDecimal.ZERO, BigDecimal::add);
        int monthsWithData = (int) fullYearTrend.stream().filter(m -> ((BigDecimal) m.get("income")).add((BigDecimal) m.get("expenses")).compareTo(BigDecimal.ZERO) > 0).count();
        if (monthsWithData == 0) monthsWithData = 1;
        avgIncome = avgIncome.divide(BigDecimal.valueOf(monthsWithData), 0, RoundingMode.HALF_UP);
        avgExpense = avgExpense.divide(BigDecimal.valueOf(monthsWithData), 0, RoundingMode.HALF_UP);

        Map<String, Object> kpis = new HashMap<>();
        kpis.put("totalIncome", totalIncome);
        kpis.put("totalExpenses", totalExpenses);
        kpis.put("netProfit", netProfit);
        kpis.put("profitMarginPercent", Math.round(profitMarginPercent * 10) / 10.0);
        kpis.put("pendingAmount", pendingAmount);
        kpis.put("totalBookings", totalBookings);
        kpis.put("completedBookings", completedBookings);
        kpis.put("pendingBookings", pendingBookings);
        kpis.put("cancelledBookings", cancelledBookings);
        kpis.put("totalClients", totalClients);
        kpis.put("topExpenseCategory", topExpenseCategory != null ? topExpenseCategory : "N/A");
        kpis.put("bestMonth", bestMonth != null ? bestMonth : "N/A");
        kpis.put("avgMonthlyIncome", avgIncome);
        kpis.put("avgMonthlyExpense", avgExpense);
        kpis.put("period", month);
        kpis.put("year", year);
        return kpis;
    }
}
