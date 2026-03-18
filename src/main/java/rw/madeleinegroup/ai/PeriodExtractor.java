package rw.madeleinegroup.ai;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Extracts the relevant year and month from the user message and conversation history.
 * Used to fetch the correct period's data from the database (e.g. "what happened in February").
 */
@Component
public class PeriodExtractor {

    private static final String[] MONTH_NAMES_EN = {
        "january", "february", "march", "april", "may", "june",
        "july", "august", "september", "october", "november", "december"
    };
    private static final String[] MONTH_NAMES_FR = {
        "janvier", "février", "fevrier", "mars", "avril", "mai", "juin",
        "juillet", "août", "aout", "septembre", "octobre", "novembre", "décembre", "decembre"
    };

    private static final Pattern YEAR_PATTERN = Pattern.compile("\\b(20[0-9]{2})\\b");

    /**
     * Returns the period to use for data fetch. Parses lastMessage and optionally recent history.
     * Default: current month/year.
     */
    public int[] extractPeriod(String lastMessage, List<Map<String, String>> messages) {
        int nowYear = LocalDate.now().getYear();
        int nowMonth = LocalDate.now().getMonthValue();

        if (lastMessage == null || lastMessage.isBlank())
            return new int[] { nowYear, nowMonth };

        String lower = lastMessage.toLowerCase().trim();

        // "last month" / "mois dernier" / "previous month"
        if (lower.matches(".*\\b(last month|mois dernier|previous month|dernier mois)\\b.*")) {
            LocalDate prev = LocalDate.now().minusMonths(1);
            return new int[] { prev.getYear(), prev.getMonthValue() };
        }

        // "this month" / "ce mois" / "current month" — explicit current
        if (lower.matches(".*\\b(this month|ce mois|current month|ce mois-ci)\\b.*"))
            return new int[] { nowYear, nowMonth };

        // Named month in English
        for (int m = 0; m < MONTH_NAMES_EN.length; m++) {
            if (lower.contains(MONTH_NAMES_EN[m])) {
                int month = m + 1;
                int year = nowYear;
                java.util.regex.Matcher matcher = YEAR_PATTERN.matcher(lastMessage);
                if (matcher.find())
                    year = Integer.parseInt(matcher.group(1));
                return new int[] { year, month };
            }
        }

        // Named month in French
        int[] frMonthMap = { 1, 2, 2, 3, 4, 5, 6, 7, 8, 8, 9, 10, 11, 12, 12 };
        for (int i = 0; i < MONTH_NAMES_FR.length; i++) {
            if (lower.contains(MONTH_NAMES_FR[i])) {
                int month = frMonthMap[i];
                int year = nowYear;
                java.util.regex.Matcher matcher = YEAR_PATTERN.matcher(lastMessage);
                if (matcher.find())
                    year = Integer.parseInt(matcher.group(1));
                return new int[] { year, month };
            }
        }

        return new int[] { nowYear, nowMonth };
    }

    /** Returns [year, month] for the previous month (for comparison). */
    public int[] previousMonth(int year, int month) {
        if (month == 1)
            return new int[] { year - 1, 12 };
        return new int[] { year, month - 1 };
    }
}
