package rw.madeleinegroup.ai;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import rw.madeleinegroup.entity.BookingStatus;
import rw.madeleinegroup.entity.PaymentType;
import rw.madeleinegroup.repository.BookingRepository;
import rw.madeleinegroup.repository.ClientRepository;
import rw.madeleinegroup.repository.ExpenseRepository;
import rw.madeleinegroup.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FinancialDataService {

    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final BookingRepository bookingRepository;
    private final ClientRepository clientRepository;

    public FinancialDataService(
            PaymentRepository paymentRepository,
            ExpenseRepository expenseRepository,
            BookingRepository bookingRepository,
            ClientRepository clientRepository) {
        this.paymentRepository = paymentRepository;
        this.expenseRepository = expenseRepository;
        this.bookingRepository = bookingRepository;
        this.clientRepository = clientRepository;
    }

    /**
     * Same expense total as {@link rw.madeleinegroup.service.FinanceService#getKpis}:
     * payment journal (EXPENSE) + expense module (approved/paid rows).
     */
    private BigDecimal totalExpensesForPeriod(LocalDate start, LocalDate end, LocalDateTime startDt, LocalDateTime endDt) {
        BigDecimal paymentExpenses = paymentRepository.sumTotalByTypeAndDateRange(PaymentType.EXPENSE, startDt, endDt);
        BigDecimal expenseAmount = expenseRepository.sumTotalByDateRange(start, end);
        return (paymentExpenses != null ? paymentExpenses : BigDecimal.ZERO)
            .add(expenseAmount != null ? expenseAmount : BigDecimal.ZERO);
    }

    @Cacheable(value = "aiLiveSnapshot", key = "#year + '-' + (#month != null ? #month : 'all')")
    public LiveFinancialSnapshot getSnapshot(int year, Integer month) {
        LiveFinancialSnapshot snap = new LiveFinancialSnapshot();
        snap.setYear(year);
        snap.setMonth(month);

        LocalDate start = month != null
            ? LocalDate.of(year, month, 1)
            : LocalDate.of(year, 1, 1);
        LocalDate end = month != null
            ? start.withDayOfMonth(start.lengthOfMonth())
            : LocalDate.of(year, 12, 31);

        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(23, 59, 59);

        BigDecimal income = paymentRepository.sumIncomeByPeriod(startDt, endDt);
        snap.setTotalIncome(income != null ? income.doubleValue() : 0);

        BigDecimal expenses = totalExpensesForPeriod(start, end, startDt, endDt);
        snap.setTotalExpenses(expenses.doubleValue());

        snap.setNetProfit(snap.getTotalIncome() - snap.getTotalExpenses());
        snap.setProfitMargin(snap.getTotalIncome() > 0
            ? (snap.getNetProfit() / snap.getTotalIncome()) * 100 : 0);

        // Same as Finance overview "Still to receive" / KPI pendingAmount: min remaining per booking, then sum (global).
        BigDecimal pending = paymentRepository.sumOutstandingReceivableMinPerBooking();
        snap.setPendingAmount(pending != null ? pending.doubleValue() : 0);

        BigDecimal sysInc = paymentRepository.sumAllIncomePaymentAmounts();
        BigDecimal sysExpMod = expenseRepository.sumAllExpenseAmounts();
        double sysIncD = sysInc != null ? sysInc.doubleValue() : 0;
        double sysExpD = sysExpMod != null ? sysExpMod.doubleValue() : 0;
        snap.setSystemWideIncomePaymentsTotal(sysIncD);
        snap.setSystemWideExpenseModuleTotal(sysExpD);
        snap.setWhatWeKeepNet(sysIncD - sysExpD);

        snap.setTotalBookings(bookingRepository.countByPeriod(start, end));
        snap.setConfirmedBookings(bookingRepository.countByStatusAndPeriod(BookingStatus.CONFIRMED, start, end));
        snap.setCompletedBookings(bookingRepository.countByStatusAndPeriod(BookingStatus.COMPLETED, start, end));
        snap.setPendingBookings(bookingRepository.countByStatusAndPeriod(BookingStatus.PENDING, start, end));
        snap.setCancelledBookings(bookingRepository.countByStatusAndPeriod(BookingStatus.CANCELLED, start, end));
        snap.setOverdueBookings(bookingRepository.countOverdueBookings(LocalDate.now()));

        snap.setTotalClients((int) clientRepository.count());
        snap.setNewClientsThisPeriod((int) clientRepository.countNewByPeriod(startDt, endDt));

        snap.setMonthlyTrend(getMonthlyTrend(year));
        snap.setCategoryBreakdown(expenseRepository.getExpensesByCategory(year, month));
        snap.setBranchPerformance(paymentRepository.getIncomeByBranch(startDt, endDt));
        snap.setTopClients(paymentRepository.getTopClientsByRevenue(startDt, endDt, 5));
        snap.setRecentLargeExpenses(expenseRepository.findLargeExpenses(start, end, BigDecimal.valueOf(100000)));

        return snap;
    }

    public List<MonthlyData> getMonthlyTrend(int year) {
        List<MonthlyData> trend = new ArrayList<>();
        String[] names = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        for (int m = 1; m <= 12; m++) {
            LocalDate s = LocalDate.of(year, m, 1);
            LocalDate e = s.withDayOfMonth(s.lengthOfMonth());
            LocalDateTime sd = s.atStartOfDay();
            LocalDateTime ed = e.atTime(23, 59, 59);

            BigDecimal inc = paymentRepository.sumIncomeByPeriod(sd, ed);
            BigDecimal payExp = paymentRepository.sumTotalByTypeAndDateRange(PaymentType.EXPENSE, sd, ed);
            BigDecimal tabExp = expenseRepository.sumExpensesByPeriod(s, e);
            BigDecimal exp = (payExp != null ? payExp : BigDecimal.ZERO).add(tabExp != null ? tabExp : BigDecimal.ZERO);

            double income = inc != null ? inc.doubleValue() : 0;
            double expenses = exp.doubleValue();

            MonthlyData md = new MonthlyData();
            md.setMonth(m);
            md.setMonthName(names[m - 1]);
            md.setIncome(income);
            md.setExpenses(expenses);
            md.setNetProfit(income - expenses);
            md.setProfitMargin(income > 0 ? ((income - expenses) / income) * 100 : 0);
            md.setBookingCount(bookingRepository.countByPeriod(s, e));
            trend.add(md);
        }
        return trend;
    }

    public YearComparison compareYears(int currentYear, int previousYear) {
        YearComparison comp = new YearComparison();

        LocalDateTime cyStart = LocalDate.of(currentYear, 1, 1).atStartOfDay();
        LocalDateTime cyEnd = LocalDate.of(currentYear, 12, 31).atTime(23, 59, 59);
        LocalDateTime pyStart = LocalDate.of(previousYear, 1, 1).atStartOfDay();
        LocalDateTime pyEnd = LocalDate.of(previousYear, 12, 31).atTime(23, 59, 59);

        BigDecimal cyIncome = paymentRepository.sumIncomeByPeriod(cyStart, cyEnd);
        BigDecimal pyIncome = paymentRepository.sumIncomeByPeriod(pyStart, pyEnd);

        double cy = cyIncome != null ? cyIncome.doubleValue() : 0;
        double py = pyIncome != null ? pyIncome.doubleValue() : 0;

        comp.setCurrentYearIncome(cy);
        comp.setPreviousYearIncome(py);
        comp.setGrowthAmount(cy - py);
        comp.setGrowthPercent(py > 0 ? ((cy - py) / py) * 100 : 0);
        comp.setIsGrowing(cy > py);

        return comp;
    }
}
