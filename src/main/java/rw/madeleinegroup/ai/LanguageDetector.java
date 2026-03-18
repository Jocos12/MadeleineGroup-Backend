package rw.madeleinegroup.ai;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects whether the user is writing in French or English.
 * Analyzes the full sentence. Mixed language defaults to French (Rwanda primary).
 */
@Component
public class LanguageDetector {

    private static final Set<String> FRENCH_WORDS = Set.of(
        "bonjour", "bonsoir", "salut", "merci", "oui", "non", "comment", "quoi", "quel", "quelle",
        "les", "des", "une", "pour", "avec", "dans", "sur", "par", "est", "sont", "pas", "plus",
        "moins", "très", "bien", "mal", "tout", "tous", "toute", "toutes", "avoir", "être",
        "analyse", "dépense", "dépenses", "revenu", "revenus", "mois", "année", "conseil",
        "résumé", "réservation", "réservations", "annulation", "client", "clients", "branche",
        "santé", "risque", "risques", "marge", "bénéfice", "bénéfices", "impayé", "attente",
        "prévision", "projection", "explique", "pourquoi", "détails", "commentaire", "aide",
        "besoin", "voulez", "pouvez", "pouvoir", "voir", "savoir", "données", "chiffres",
        "rapport", "performance", "croissance", "réduire", "augmenter", "améliorer",
        "problème", "problèmes", "alerte", "alertes", "urgence", "urgent", "situation",
        "actuel", "actuelle", "actuels", "ceux", "celles", "cette", "cet", "ces",
        "notre", "votre", "leur", "leurs", "aux", "du", "au", "été", "fait", "faire"
    );

    private static final Set<String> ENGLISH_WORDS = Set.of(
        "hello", "hi", "hey", "thanks", "thank you", "yes", "no", "how", "what", "which",
        "the", "a", "an", "for", "with", "in", "on", "by", "is", "are", "not", "more",
        "less", "very", "good", "bad", "all", "have", "has", "been", "being",
        "analysis", "expense", "expenses", "income", "revenue", "month", "year", "advice",
        "summary", "booking", "bookings", "cancellation", "client", "clients", "branch",
        "health", "risk", "risks", "margin", "profit", "profits", "pending", "outstanding",
        "forecast", "projection", "explain", "why", "details", "help", "need", "want",
        "can", "could", "would", "see", "know", "data", "numbers", "report", "performance",
        "growth", "reduce", "increase", "improve", "problem", "problems", "alert", "alerts",
        "urgent", "situation", "current", "these", "those", "this", "that", "our", "your",
        "their", "done", "do", "does", "did", "will", "should"
    );

    private static final Pattern WORD_PATTERN = Pattern.compile("[a-zA-ZàâäéèêëïîôùûüçÀÂÄÉÈÊËÏÎÔÙÛÜÇ]+");

    /**
     * Returns true if the message is primarily in French, false for English.
     * Mixed or ambiguous defaults to true (French) for Rwanda context.
     */
    public boolean isFrench(String message) {
        if (message == null || message.isBlank()) return true;
        String lower = message.toLowerCase().trim();

        int frenchCount = 0;
        int englishCount = 0;
        Matcher matcher = WORD_PATTERN.matcher(lower);
        while (matcher.find()) {
            String word = matcher.group();
            if (word.length() < 2) continue;
            if (FRENCH_WORDS.contains(word)) frenchCount++;
            else if (ENGLISH_WORDS.contains(word)) englishCount++;
        }

        // Mixed or tie → French (Rwanda primary)
        return frenchCount >= englishCount;
    }
}
