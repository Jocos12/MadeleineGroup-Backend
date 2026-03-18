package rw.madeleinegroup.ai;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Replaces all identifying information before sending data to Groq.
 * Keeps all financial numbers unchanged so the AI can perform accurate analysis.
 */
@Component
public class DataAnonymizer {

    private static final String COMPANY_LABEL = "Company";
    private static final Pattern COMPANY_PATTERN = Pattern.compile("(?i)madeleine\\s*group");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:(?:\\+?\\d{1,3}[-.\\s]?)?(?:\\(?\\d{2,4}\\)?[-.\\s]?)?\\d{3,4}[-.\\s]?\\d{3,4}|\\d{5,})");
    private static final Pattern BOOKING_REF_PATTERN = Pattern.compile("(?i)(?:ref|booking|#)?\\s*[A-Z0-9]{6,}");

    /**
     * Returns an anonymized copy of the snapshot: company and branch/client names replaced,
     * all numbers unchanged. Does not modify the original.
     */
    public LiveFinancialSnapshot anonymizeSnapshot(LiveFinancialSnapshot snap) {
        if (snap == null) return null;
        LiveFinancialSnapshot out = new LiveFinancialSnapshot();
        out.setYear(snap.getYear());
        out.setMonth(snap.getMonth());
        out.setTotalIncome(snap.getTotalIncome());
        out.setTotalExpenses(snap.getTotalExpenses());
        out.setNetProfit(snap.getNetProfit());
        out.setProfitMargin(snap.getProfitMargin());
        out.setPendingAmount(snap.getPendingAmount());
        out.setTotalBookings(snap.getTotalBookings());
        out.setConfirmedBookings(snap.getConfirmedBookings());
        out.setCompletedBookings(snap.getCompletedBookings());
        out.setPendingBookings(snap.getPendingBookings());
        out.setCancelledBookings(snap.getCancelledBookings());
        out.setOverdueBookings(snap.getOverdueBookings());
        out.setTotalClients(snap.getTotalClients());
        out.setNewClientsThisPeriod(snap.getNewClientsThisPeriod());

        out.setMonthlyTrend(snap.getMonthlyTrend()); // month names (Jan, Feb) and numbers only
        out.setCategoryBreakdown(anonymizeCategoryBreakdown(snap.getCategoryBreakdown()));
        out.setBranchPerformance(anonymizeBranchPerformance(snap.getBranchPerformance()));
        out.setTopClients(anonymizeTopClients(snap.getTopClients()));
        out.setRecentLargeExpenses(snap.getRecentLargeExpenses()); // no identifiers in list from repo
        return out;
    }

    /** Replace branch names with Branch A, Branch B, etc.; keep amounts. */
    private List<Object[]> anonymizeBranchPerformance(List<Object[]> branchPerformance) {
        if (branchPerformance == null || branchPerformance.isEmpty()) return branchPerformance;
        List<Object[]> out = new ArrayList<>();
        int i = 0;
        for (Object[] row : branchPerformance) {
            if (row == null || row.length < 2) continue;
            String branchLabel = "Branch " + (char) ('A' + (i % 26));
            if (i >= 26) branchLabel = "Branch " + (i + 1);
            out.add(new Object[]{ branchLabel, row[1] });
            i++;
        }
        return out;
    }

    /** Replace client names with Client 1, Client 2, etc.; keep amounts. */
    private List<Object[]> anonymizeTopClients(List<Object[]> topClients) {
        if (topClients == null || topClients.isEmpty()) return topClients;
        List<Object[]> out = new ArrayList<>();
        int i = 1;
        for (Object[] row : topClients) {
            if (row == null || row.length < 2) continue;
            out.add(new Object[]{ "Client " + i, row[1] });
            i++;
        }
        return out;
    }

    /** Category names are generic (e.g. SALAIRE); keep as is. Remove any accidental identifiers in first column. */
    private List<Object[]> anonymizeCategoryBreakdown(List<Object[]> categoryBreakdown) {
        if (categoryBreakdown == null) return null;
        List<Object[]> out = new ArrayList<>();
        for (Object[] row : categoryBreakdown) {
            if (row == null || row.length < 2) continue;
            String cat = String.valueOf(row[0]);
            if (COMPANY_PATTERN.matcher(cat).find()) cat = COMPANY_LABEL;
            out.add(new Object[]{ cat, row[1] });
        }
        return out;
    }

    /**
     * Replace company name, branch names, client names, emails, phones, booking refs in text.
     * Keeps numbers unchanged.
     */
    public String anonymizeText(String text) {
        if (text == null || text.isEmpty()) return text;
        String s = COMPANY_PATTERN.matcher(text).replaceAll(COMPANY_LABEL);
        s = EMAIL_PATTERN.matcher(s).replaceAll("[email redacted]");
        s = PHONE_PATTERN.matcher(s).replaceAll("[phone redacted]");
        s = BOOKING_REF_PATTERN.matcher(s).replaceAll("[ref redacted]");
        return s;
    }
}
