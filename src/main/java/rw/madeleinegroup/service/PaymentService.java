package rw.madeleinegroup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;

import rw.madeleinegroup.common.enums.PaymentStatus;
import rw.madeleinegroup.dto.PaymentAnalyticsResponse;
import rw.madeleinegroup.dto.PaymentBranchStatDto;
import rw.madeleinegroup.dto.PaymentMonthStatDto;
import rw.madeleinegroup.dto.PaymentRecorderStatDto;
import rw.madeleinegroup.dto.PaymentRequest;
import rw.madeleinegroup.dto.PaymentResponse;
import rw.madeleinegroup.dto.PaymentTopBranchDto;
import rw.madeleinegroup.common.enums.PaymentMethod;
import rw.madeleinegroup.entity.*;
import rw.madeleinegroup.exception.ResourceNotFoundException;
import rw.madeleinegroup.repository.BookingRepository;
import rw.madeleinegroup.repository.BranchRepository;
import rw.madeleinegroup.repository.PaymentRepository;
import rw.madeleinegroup.repository.UserRepository;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

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
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        User recordedBy = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));
        if (!branch.isActive()) {
            throw new IllegalArgumentException("Branch is not active; payments cannot be recorded for it");
        }
        Booking booking = request.getBookingId() != null ?
                bookingRepository.findById(request.getBookingId()).orElse(null) : null;

        PaymentType ptype = PaymentType.valueOf(request.getType());
        if (ptype == PaymentType.EXPENSE) {
            BigDecimal net = paymentRepository.netBalanceForBranch(branch.getId());
            if (net == null) net = BigDecimal.ZERO;
            BigDecimal after = net.subtract(request.getAmount());
            if (after.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Expense of {} RWF on branch {} ({}) would make branch net balance negative (current net {}). Recording is allowed.",
                        request.getAmount().toPlainString(), branch.getId(), branch.getName(), net.toPlainString());
            }
        }
        if (booking != null && ptype == PaymentType.INCOME) {
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

        if (booking != null && ptype == PaymentType.INCOME) {
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

    @Transactional(readOnly = true)
    public PaymentAnalyticsResponse getPaymentAnalytics() {
        long totalPayments = paymentRepository.count();
        BigDecimal totalIncome = paymentRepository.sumTotalAmountByType(PaymentType.INCOME);
        BigDecimal totalExpense = paymentRepository.sumTotalAmountByType(PaymentType.EXPENSE);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;
        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        List<Object[]> rows = paymentRepository.aggregatePaymentsByBranch();
        BigDecimal globalVolume = BigDecimal.ZERO;
        List<PaymentBranchStatDto> byBranch = new ArrayList<>();
        for (Object[] r : rows) {
            Long bid = ((Number) r[0]).longValue();
            String name = (String) r[1];
            BigDecimal inc = (BigDecimal) r[2];
            BigDecimal exp = (BigDecimal) r[3];
            long cnt = ((Number) r[4]).longValue();
            BigDecimal vol = inc.add(exp);
            globalVolume = globalVolume.add(vol);
            PaymentBranchStatDto dto = new PaymentBranchStatDto();
            dto.setBranchId(bid);
            dto.setBranchName(name);
            dto.setTotalIncome(inc);
            dto.setTotalExpense(exp);
            dto.setNetBalance(inc.subtract(exp));
            dto.setPaymentCount(cnt);
            byBranch.add(dto);
        }
        byBranch.sort(Comparator.<PaymentBranchStatDto, BigDecimal>comparing(
                d -> d.getTotalIncome().add(d.getTotalExpense())).reversed());
        for (PaymentBranchStatDto dto : byBranch) {
            BigDecimal vol = dto.getTotalIncome().add(dto.getTotalExpense());
            if (globalVolume.compareTo(BigDecimal.ZERO) > 0) {
                dto.setPercentageOfTotal(vol.multiply(BigDecimal.valueOf(100))
                        .divide(globalVolume, 2, RoundingMode.HALF_UP));
            } else {
                dto.setPercentageOfTotal(BigDecimal.ZERO);
            }
        }

        PaymentTopBranchDto top = null;
        if (!byBranch.isEmpty()) {
            PaymentBranchStatDto first = byBranch.get(0);
            top = new PaymentTopBranchDto();
            top.setBranchId(first.getBranchId());
            top.setName(first.getBranchName());
            BigDecimal vol = first.getTotalIncome().add(first.getTotalExpense());
            top.setTotalAmount(vol);
            top.setTotalIncome(first.getTotalIncome());
            top.setTotalExpense(first.getTotalExpense());
            top.setNetBalance(first.getNetBalance());
            top.setPaymentCount(first.getPaymentCount());
        }

        List<PaymentMonthStatDto> byMonth = new ArrayList<>();
        List<Object[]> monthRows = paymentRepository.aggregatePaymentsByYearMonth();
        int from = Math.max(0, monthRows.size() - 24);
        for (int i = from; i < monthRows.size(); i++) {
            Object[] m = monthRows.get(i);
            PaymentMonthStatDto md = new PaymentMonthStatDto();
            int y = ((Number) m[0]).intValue();
            int mo = ((Number) m[1]).intValue();
            md.setYear(y);
            md.setMonth(mo);
            md.setMonthKey(String.format("%04d-%02d", y, mo));
            BigDecimal inc = (BigDecimal) m[2];
            BigDecimal exp = (BigDecimal) m[3];
            md.setIncome(inc);
            md.setExpense(exp);
            md.setNet(inc.subtract(exp));
            byMonth.add(md);
        }

        List<PaymentRecorderStatDto> topRecorders = new ArrayList<>();
        List<Object[]> recRows = paymentRepository.aggregatePaymentsByRecorder(PageRequest.of(0, 10));
        for (Object[] rr : recRows) {
            PaymentRecorderStatDto rd = new PaymentRecorderStatDto();
            rd.setUserId(((Number) rr[0]).longValue());
            rd.setFullName((String) rr[1]);
            rd.setPaymentCount(((Number) rr[2]).longValue());
            rd.setTotalVolume((BigDecimal) rr[3]);
            topRecorders.add(rd);
        }

        PaymentAnalyticsResponse out = new PaymentAnalyticsResponse();
        out.setTotalPayments(totalPayments);
        out.setTotalCount(totalPayments);
        out.setTotalIncome(totalIncome);
        out.setTotalExpense(totalExpense);
        out.setNetBalance(netBalance);
        int branchCount = byBranch.size();
        out.setBranchesWithPayments(branchCount);
        out.setActiveBranchCount(branchCount);
        out.setTopBranch(top);
        out.setByBranch(byBranch);
        out.setByMonth(byMonth);
        out.setTopRecorders(topRecorders);
        return out;
    }

    @Transactional(readOnly = true)
    public List<String> listPaymentMethodNames() {
        return Arrays.stream(PaymentMethod.values()).map(Enum::name).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> listPaymentStatusNames() {
        return Arrays.stream(PaymentStatus.values()).map(Enum::name).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> listPaymentTypeNames() {
        return Arrays.stream(PaymentType.values()).map(Enum::name).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentsByBranch(Long branchId) {
        branchRepository.findById(branchId).orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        List<Payment> list = paymentRepository.findAllWithDetails(branchId);
        List<PaymentResponse> payments = list.stream().map(PaymentResponse::from).toList();
        BigDecimal inc = BigDecimal.ZERO;
        BigDecimal exp = BigDecimal.ZERO;
        for (Payment p : list) {
            if (p.getType() == PaymentType.INCOME) inc = inc.add(p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO);
            else if (p.getType() == PaymentType.EXPENSE) exp = exp.add(p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO);
        }
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncome", inc);
        summary.put("totalExpense", exp);
        summary.put("netBalance", inc.subtract(exp));
        summary.put("paymentCount", list.size());
        Map<String, Object> res = new HashMap<>();
        res.put("payments", payments);
        res.put("summary", summary);
        return res;
    }

    @Transactional(readOnly = true)
    public PaymentTopBranchDto getTopBranch() {
        List<Object[]> rows = paymentRepository.aggregatePaymentsByBranch();
        if (rows.isEmpty()) {
            return null;
        }
        Object[] r = rows.get(0);
        PaymentTopBranchDto top = new PaymentTopBranchDto();
        top.setBranchId(((Number) r[0]).longValue());
        top.setName((String) r[1]);
        BigDecimal inc = (BigDecimal) r[2];
        BigDecimal exp = (BigDecimal) r[3];
        long cnt = ((Number) r[4]).longValue();
        top.setTotalIncome(inc);
        top.setTotalExpense(exp);
        top.setNetBalance(inc.subtract(exp));
        top.setPaymentCount(cnt);
        top.setTotalAmount(inc.add(exp));
        return top;
    }
}
