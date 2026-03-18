package rw.madeleinegroup.ai;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class DeepAnalysisEngine {

    public DeepReport analyze(LiveFinancialSnapshot snap) {
        DeepReport report = new DeepReport();

        report.setHealthScore(computeHealthScore(snap));
        report.setRiskLevel(computeRiskLevel(snap));
        report.setProfitMarginGrade(gradeProfitMargin(snap.getProfitMargin()));

        report.setTrend(analyzeTrend(snap.getMonthlyTrend()));
        report.setMomentum(computeMomentum(snap.getMonthlyTrend()));
        report.setBestMonth(findBestMonth(snap.getMonthlyTrend()));
        report.setWorstMonth(findWorstMonth(snap.getMonthlyTrend()));
        report.setProjectedNextMonthIncome(projectNextMonth(snap.getMonthlyTrend()));

        report.setExpenseRatio(snap.getTotalIncome() > 0
            ? snap.getTotalExpenses() / snap.getTotalIncome() * 100 : 0);
        report.setTopExpenseCategory(findTopCategory(snap.getCategoryBreakdown()));
        report.setExpenseAlerts(detectExpenseAlerts(
            snap.getCategoryBreakdown(), snap.getTotalIncome()));
        report.setWastedSpend(estimateWastedSpend(
            snap.getCategoryBreakdown(), snap.getTotalExpenses()));

        report.setBookingCompletionRate(computeCompletionRate(snap));
        report.setCancellationRate(computeCancellationRate(snap));
        report.setAverageRevenuePerBooking(
            snap.getTotalBookings() > 0
                ? snap.getTotalIncome() / snap.getTotalBookings() : 0);

        report.setClientAcquisitionRate(snap.getNewClientsThisPeriod());
        report.setRevenuePerClient(
            snap.getTotalClients() > 0
                ? snap.getTotalIncome() / snap.getTotalClients() : 0);

        report.setPendingRatio(snap.getTotalIncome() > 0
            ? snap.getPendingAmount() / snap.getTotalIncome() * 100 : 0);
        report.setCashFlowStatus(computeCashFlowStatus(snap));

        report.setUrgentActions(generateUrgentActions(snap, report));
        report.setStrategicRecommendations(
            generateStrategicRecommendations(snap, report));
        report.setQuickWins(generateQuickWins(snap, report));

        report.setTopBranch(findTopBranch(snap.getBranchPerformance()));
        report.setUnderperformingBranch(
            findBottomBranch(snap.getBranchPerformance()));

        // Exact figures for targets (Prompt 4)
        double income = snap.getTotalIncome();
        double expenses = snap.getTotalExpenses();
        double margin = snap.getProfitMargin();
        if (margin < 60 && income > 0) {
            double gapExpense = expenses - 0.4 * income; // cut expenses by this to reach 60% margin
            report.setRwfToSaveForTargetMargin(Math.max(0, gapExpense));
            report.setRwfToEarnForTargetMargin(gapExpense > 0 ? gapExpense / 0.4 : 0); // extra revenue needed
        } else {
            report.setRwfToSaveForTargetMargin(0);
            report.setRwfToEarnForTargetMargin(0);
        }
        int overdue = snap.getOverdueBookings();
        report.setOverdueBookingsImpactRwf(overdue > 0 && report.getAverageRevenuePerBooking() > 0
            ? overdue * report.getAverageRevenuePerBooking() : 0);
        List<Object[]> cats = snap.getCategoryBreakdown();
        if (cats != null && !cats.isEmpty()) {
            double topCatAmount = toDouble(cats.get(0)[1]);
            report.setTopExpenseCategoryReductionTargetRwf(Math.max(0, topCatAmount * 0.15)); // 15% reduction target
        } else {
            report.setTopExpenseCategoryReductionTargetRwf(0);
        }

        return report;
    }

    private int computeHealthScore(LiveFinancialSnapshot snap) {
        int score = 0;

        double margin = snap.getProfitMargin();
        if (margin >= 70) score += 35;
        else if (margin >= 55) score += 28;
        else if (margin >= 40) score += 20;
        else if (margin >= 25) score += 12;
        else if (margin >= 10) score += 5;

        if (snap.getTotalIncome() > 5000000) score += 15;
        else if (snap.getTotalIncome() > 1000000) score += 10;
        else if (snap.getTotalIncome() > 0) score += 5;

        double pendingRatio = snap.getTotalIncome() > 0
            ? snap.getPendingAmount() / snap.getTotalIncome() : 1;
        if (pendingRatio < 0.05) score += 15;
        else if (pendingRatio < 0.15) score += 10;
        else if (pendingRatio < 0.30) score += 5;

        double completionRate = computeCompletionRate(snap);
        if (completionRate >= 80) score += 20;
        else if (completionRate >= 60) score += 14;
        else if (completionRate >= 40) score += 8;
        else if (completionRate > 0) score += 3;

        int penalty = Math.min(snap.getOverdueBookings() * 5, 15);
        score -= penalty;

        String trend = analyzeTrend(snap.getMonthlyTrend());
        if ("STRONG_GROWTH".equals(trend)) score += 15;
        else if ("GROWING".equals(trend)) score += 10;
        else if ("STABLE".equals(trend)) score += 6;
        else if ("DECLINING".equals(trend)) score += 2;

        return Math.max(0, Math.min(100, score));
    }

    private String computeRiskLevel(LiveFinancialSnapshot snap) {
        int riskPoints = 0;
        if (snap.getProfitMargin() < 15) riskPoints += 3;
        else if (snap.getProfitMargin() < 30) riskPoints += 1;
        if (snap.getTotalIncome() == 0) riskPoints += 4;
        if (snap.getTotalIncome() > 0
                && snap.getPendingAmount() / snap.getTotalIncome() > 0.4)
            riskPoints += 2;
        if (snap.getOverdueBookings() > 3) riskPoints += 2;
        if (snap.getCancelledBookings() > snap.getCompletedBookings())
            riskPoints += 2;

        if (riskPoints >= 6) return "CRITICAL";
        if (riskPoints >= 4) return "HIGH";
        if (riskPoints >= 2) return "MEDIUM";
        return "LOW";
    }

    private String gradeProfitMargin(double margin) {
        if (margin >= 70) return "A+";
        if (margin >= 60) return "A";
        if (margin >= 50) return "B+";
        if (margin >= 40) return "B";
        if (margin >= 30) return "C";
        if (margin >= 20) return "D";
        return "F";
    }

    private String analyzeTrend(List<MonthlyData> trend) {
        if (trend == null || trend.size() < 3) return "INSUFFICIENT_DATA";
        List<MonthlyData> active = trend.stream()
            .filter(m -> m.getIncome() > 0).toList();
        if (active.size() < 2) return "INSUFFICIENT_DATA";

        int up = 0, down = 0;
        for (int i = 1; i < active.size(); i++) {
            if (active.get(i).getIncome() > active.get(i - 1).getIncome()) up++;
            else if (active.get(i).getIncome() < active.get(i - 1).getIncome()) down++;
        }
        int total = up + down;
        if (total == 0) return "STABLE";
        double upRatio = (double) up / total;
        if (upRatio >= 0.75) return "STRONG_GROWTH";
        if (upRatio >= 0.55) return "GROWING";
        if (upRatio >= 0.45) return "STABLE";
        if (upRatio >= 0.25) return "DECLINING";
        return "STRONG_DECLINE";
    }

    private String computeMomentum(List<MonthlyData> trend) {
        if (trend == null || trend.size() < 6) return "UNKNOWN";
        List<MonthlyData> active = trend.stream()
            .filter(m -> m.getIncome() > 0).toList();
        if (active.size() < 4) return "UNKNOWN";

        int size = active.size();
        double recent = active.subList(size - 3, size).stream()
            .mapToDouble(MonthlyData::getIncome).average().orElse(0);
        double previous = active.subList(
            Math.max(0, size - 6), size - 3).stream()
            .mapToDouble(MonthlyData::getIncome).average().orElse(0);

        if (previous == 0) return "UNKNOWN";
        double change = (recent - previous) / previous * 100;
        if (change > 20) return "ACCELERATING";
        if (change > 5) return "POSITIVE";
        if (change > -5) return "NEUTRAL";
        if (change > -20) return "SLOWING";
        return "DECELERATING";
    }

    private double projectNextMonth(List<MonthlyData> trend) {
        if (trend == null || trend.size() < 3) return 0;
        List<MonthlyData> active = trend.stream()
            .filter(m -> m.getIncome() > 0).toList();
        if (active.isEmpty()) return 0;

        int size = active.size();
        int last = Math.min(3, size);
        double weightedSum = 0, weightTotal = 0;
        for (int i = 0; i < last; i++) {
            double weight = last - i;
            weightedSum += active.get(size - 1 - i).getIncome() * weight;
            weightTotal += weight;
        }
        return weightTotal > 0 ? weightedSum / weightTotal : 0;
    }

    private String findBestMonth(List<MonthlyData> trend) {
        if (trend == null || trend.isEmpty()) return "N/A";
        return trend.stream()
            .max(Comparator.comparingDouble(MonthlyData::getNetProfit))
            .map(MonthlyData::getMonthName).orElse("N/A");
    }

    private String findWorstMonth(List<MonthlyData> trend) {
        if (trend == null || trend.isEmpty()) return "N/A";
        return trend.stream()
            .filter(m -> m.getIncome() > 0)
            .min(Comparator.comparingDouble(MonthlyData::getNetProfit))
            .map(MonthlyData::getMonthName).orElse("N/A");
    }

    private String findTopCategory(List<Object[]> cats) {
        if (cats == null || cats.isEmpty()) return "N/A";
        Object[] top = cats.get(0);
        return top[0] != null ? top[0].toString() : "N/A";
    }

    private List<String> detectExpenseAlerts(
            List<Object[]> cats, double totalIncome) {
        List<String> alerts = new ArrayList<>();
        if (cats == null) return alerts;

        double totalExpenses = cats.stream()
            .mapToDouble(c -> toDouble(c[1])).sum();

        for (Object[] cat : cats) {
            String name = cat[0] != null ? cat[0].toString() : "";
            double amount = toDouble(cat[1]);
            double pctOfExpenses = totalExpenses > 0
                ? amount / totalExpenses * 100 : 0;
            double pctOfIncome = totalIncome > 0
                ? amount / totalIncome * 100 : 0;

            if ("SALAIRE".equals(name) && pctOfExpenses > 55) {
                alerts.add("⚠️ Salary costs represent " +
                    f1(pctOfExpenses) + "% of total expenses " +
                    "(healthy max: 55%). Review staffing efficiency.");
            }
            if (pctOfIncome > 35) {
                alerts.add("🔴 " + name + " expenses consume " +
                    f1(pctOfIncome) + "% of your total income. " +
                    "Immediate review recommended.");
            }
            if ("LOCATION".equals(name) && amount > 500000) {
                alerts.add("🏢 High venue rental cost: " +
                    fmt(amount) + " RWF. " +
                    "Negotiate long-term contracts for better rates.");
            }
        }
        return alerts;
    }

    private double estimateWastedSpend(
            List<Object[]> cats, double totalExpenses) {
        if (cats == null || totalExpenses == 0) return 0;
        double waste = 0;
        for (Object[] cat : cats) {
            String name = cat[0] != null ? cat[0].toString() : "";
            double amount = toDouble(cat[1]);
            double pct = amount / totalExpenses * 100;
            if (!"SALAIRE".equals(name) && pct > 25) {
                waste += amount * 0.15;
            }
        }
        return waste;
    }

    private double computeCompletionRate(LiveFinancialSnapshot snap) {
        return snap.getTotalBookings() > 0
            ? (double) snap.getCompletedBookings()
                / snap.getTotalBookings() * 100 : 0;
    }

    private double computeCancellationRate(LiveFinancialSnapshot snap) {
        return snap.getTotalBookings() > 0
            ? (double) snap.getCancelledBookings()
                / snap.getTotalBookings() * 100 : 0;
    }

    private String computeCashFlowStatus(LiveFinancialSnapshot snap) {
        if (snap.getTotalIncome() == 0) return "NO_INCOME";
        double pendingRatio = snap.getPendingAmount()
            / snap.getTotalIncome();
        if (pendingRatio < 0.1) return "STRONG";
        if (pendingRatio < 0.25) return "HEALTHY";
        if (pendingRatio < 0.4) return "MODERATE";
        if (pendingRatio < 0.6) return "TIGHT";
        return "CRITICAL";
    }

    private String findTopBranch(List<Object[]> branches) {
        if (branches == null || branches.isEmpty()) return "N/A";
        return branches.get(0)[0] != null
            ? branches.get(0)[0].toString() : "N/A";
    }

    private String findBottomBranch(List<Object[]> branches) {
        if (branches == null || branches.size() < 2) return "N/A";
        Object[] last = branches.get(branches.size() - 1);
        return last[0] != null ? last[0].toString() : "N/A";
    }

    private List<String> generateUrgentActions(
            LiveFinancialSnapshot snap, DeepReport report) {
        List<String> actions = new ArrayList<>();

        if (snap.getOverdueBookings() > 0) {
            actions.add("🚨 " + snap.getOverdueBookings() +
                " booking(s) are overdue — update their status immediately");
        }
        if ("CRITICAL".equals(report.getCashFlowStatus())
                || "TIGHT".equals(report.getCashFlowStatus())) {
            actions.add("💰 Send payment reminders for " +
                fmt(snap.getPendingAmount()) +
                " RWF in pending collections today");
        }
        if (snap.getProfitMargin() < 20) {
            actions.add("🔴 Profit margin critically low at " +
                f1(snap.getProfitMargin()) +
                "% — review and cut non-essential expenses immediately");
        }
        if (snap.getCancelledBookings() > snap.getCompletedBookings()
                && snap.getTotalBookings() > 0) {
            actions.add("⚠️ Cancellation rate exceeds completion rate — " +
                "investigate root cause and improve client retention");
        }

        return actions;
    }

    private List<String> generateStrategicRecommendations(
            LiveFinancialSnapshot snap, DeepReport report) {
        List<String> recs = new ArrayList<>();

        if ("DECLINING".equals(report.getTrend())
                || "STRONG_DECLINE".equals(report.getTrend())) {
            recs.add("📉 Revenue declining — launch a referral program " +
                "offering 10% discount to clients who refer new customers");
            recs.add("📱 Increase Instagram/social media presence " +
                "showcasing recent events to attract new bookings");
        }

        if (snap.getProfitMargin() < 50) {
            recs.add("💡 Bundle services (Décor + Catering + Studio) " +
                "into premium packages with 15% higher pricing — " +
                "increases perceived value and revenue per booking");
        }

        if (snap.getNewClientsThisPeriod() < 3) {
            recs.add("👥 Low new client acquisition this period — " +
                "partner with Kigali wedding planners and hotels " +
                "for referral agreements");
        }

        int currentMonth = snap.getMonth() != null
            ? snap.getMonth() : 0;
        if (currentMonth >= 6 && currentMonth <= 8) {
            recs.add("☀️ Peak wedding season approaching — " +
                "increase prices by 10-15% for July-August bookings " +
                "and require full payment 30 days before event");
        }

        if (report.getAverageRevenuePerBooking() > 0
                && report.getAverageRevenuePerBooking() < 500000) {
            recs.add("💎 Average booking value of " +
                fmt(report.getAverageRevenuePerBooking()) +
                " RWF is low — upsell premium add-ons " +
                "(live band, MC, special lighting) at booking time");
        }

        return recs;
    }

    private List<String> generateQuickWins(
            LiveFinancialSnapshot snap, DeepReport report) {
        List<String> wins = new ArrayList<>();

        if (snap.getPendingAmount() > 100000) {
            wins.add("✅ Call top 3 clients with pending balances today — " +
                "could recover " +
                fmt(snap.getPendingAmount() * 0.5) + " RWF this week");
        }
        if (report.getWastedSpend() > 50000) {
            wins.add("✅ Review " + report.getTopExpenseCategory() +
                " expenses — estimated " +
                fmt(report.getWastedSpend()) +
                " RWF in potentially reducible costs");
        }
        if (snap.getTotalBookings() > 0
                && report.getBookingCompletionRate() < 60) {
            wins.add("✅ Follow up on " + snap.getPendingBookings() +
                " pending bookings — converting just 2 more " +
                "could add significant revenue");
        }

        return wins;
    }

    private double toDouble(Object v) {
        if (v == null) return 0;
        try { return Double.parseDouble(v.toString()); }
        catch (Exception e) { return 0; }
    }

    private String fmt(double v) {
        return String.format("%,.0f", v);
    }

    private String f1(double v) {
        return String.format("%.1f", v);
    }
}
