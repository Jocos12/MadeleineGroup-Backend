package rw.madeleinegroup.ai;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Classifies user intent into two tiers:
 * - LOCAL: Handled entirely by Spring Boot with real data (client names, amounts, etc.). Groq is never called.
 * - GROQ: Handled by Groq with anonymous aggregated data only.
 */
@Component
public class IntentClassifier {

    /** Intents that Spring Boot handles locally with real data. Groq is never called. */
    private static final java.util.Set<String> LOCAL_INTENTS = java.util.Set.of(
        "SHOW_CLIENTS_PENDING", "SHOW_BOOKINGS", "SHOW_OVERDUE", "SHOW_PAYMENTS",
        "SEND_EMAIL_REMINDER", "DATA_EXPORT", "CONFIRM_EMAIL_ACTION", "CANCEL_EMAIL_ACTION"
    );

    public boolean isLocalIntent(String intent) {
        return intent != null && LOCAL_INTENTS.contains(intent);
    }

    private static final Pattern FOLLOW_UP_PATTERN = Pattern.compile(
        "\\b(why|explain|tell me more|more details|what about|how come|and then|et alors|" +
        "pourquoi|explique|détails|plus de détails|comment ça|en quoi|quoi d'autre|" +
        "continue|go on|and|et)\\b",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * Resolves intent for the last user message, using conversation history for follow-ups.
     * If the last message is a follow-up (e.g. "why", "explique"), returns the intent of the previous user question.
     */
    public String resolveIntent(String lastUserMessage, List<Map<String, String>> allMessages) {
        if (lastUserMessage == null) return "PERFORMANCE";
        String trimmed = lastUserMessage.trim().toLowerCase();

        if (isFollowUp(trimmed, allMessages)) {
            String previousUserMessage = getLastUserMessageBeforeCurrent(allMessages);
            if (previousUserMessage != null && !previousUserMessage.isBlank())
                return classify(previousUserMessage);
        }

        return classify(lastUserMessage);
    }

    /**
     * Classifies a single message (no history). Use resolveIntent when you have conversation history.
     */
    public String classify(String message) {
        if (message == null) return "PERFORMANCE";
        String m = message.toLowerCase().trim();

        // Casual greetings and short hellos (Prompt 1) — respond warmly, not with a cold report
        if (isCasualGreeting(m)) return "WELCOME";

        // Email action confirmation / cancel (short replies) — LOCAL
        if (isConfirmAction(m)) return "CONFIRM_EMAIL_ACTION";
        if (isCancelAction(m)) return "CANCEL_EMAIL_ACTION";

        // ——— LOCAL intents (Spring Boot only, real data, Groq never called) ———
        if (has(m, "quels clients", "which clients", "qui doit", "who owes", "liste les clients", "show me clients", "list clients", "clients avec", "clients qui ont"))
            return "SHOW_CLIENTS_PENDING";
        if (has(m, "montre les réservations", "show bookings", "liste les réservations", "quelles réservations", "which bookings", "list bookings", "liste des réservations"))
            return "SHOW_BOOKINGS";
        if (has(m, "réservations en retard", "overdue bookings", "bookings overdue", "en retard", "retard"))
            return "SHOW_OVERDUE";
        if (has(m, "liste les paiements", "show payments", "quels paiements", "list payments", "liste des paiements"))
            return "SHOW_PAYMENTS";
        if (has(m, "envoie un email", "envoyer un rappel", "send reminder", "send email", "rappelle les clients",
                "notify clients", "contacte les clients", "email the clients", "envoie un message", "préviens les clients",
                "remind clients", "envoie les rappels", "envoie un rappel", "send payment reminder", "payment reminders",
                "rappels de paiement", "soldes en attente", "clients en retard", "rappel à tous les clients"))
            return "SEND_EMAIL_REMINDER";
        if (has(m, "exporte", "export", "télécharge", "download", "exporter", "télécharger"))
            return "DATA_EXPORT";

        // ——— GROQ intents (anonymous data only) ———
        if (has(m, "combien j'ai", "combien d'argent", "combien comme argent", "j'ai comme argent", "argent dans le système", "argent dans mon système", "dans mon système",
                "dans mon compte", "l'argent dans", "montant total dans", "solde global", "solde dans",
                "how much money", "how much do i have", "money in the system", "money in my account", "total in the system",
                "what we keep", "ce que nous gardons", "position globale", "total dans"))
            return "SYSTEM_BALANCE";
        if (has(m, "performance", "mois", "month", "analyse", "global", "résumé", "summary", "overview"))
            return "PERFORMANCE";
        if (has(m, "dépense", "depense", "expense", "coût", "cost",
                "inutile", "unnecessary", "réduire", "reduce", "gaspillage", "spending"))
            return "EXPENSES";
        if (has(m, "profit", "marge", "margin", "bénéfice", "benefice", "rentab", "profitability"))
            return "PROFIT";
        if (has(m, "revenue", "revenu", "income", "croissance", "growth",
                "augment", "chiffre", "sales"))
            return "REVENUE";
        if (has(m, "risk", "risque", "danger", "problème", "problem", "alerte", "alert"))
            return "RISKS";
        if (has(m, "santé", "health", "score", "situation", "overall", "how are we"))
            return "HEALTH";
        if (has(m, "pending", "attente", "impayé", "recouvr", "collect", "dette", "debt", "outstanding"))
            return "PENDING";
        if (has(m, "réservation", "booking", "reservation", "annulation", "cancel", "bookings") && !has(m, "liste", "list", "show", "montre", "quelles", "which"))
            return "BOOKINGS";
        if (has(m, "client", "customer", "fidél", "loyalt", "clients") && !has(m, "quels", "which", "liste", "list", "show me", "qui doit", "who owes"))
            return "CLIENTS";
        if (has(m, "branche", "branch", "succursale", "agence", "branches"))
            return "BRANCHES";
        if (has(m, "prévision", "projection", "forecast", "prochain", "next",
                "futur", "future", "predict"))
            return "PROJECTION";
        if (has(m, "rapide", "quick", "win", "facile", "easy", "immédiat", "immediate"))
            return "QUICKWINS";
        if (has(m, "stratégie", "strategy", "recommandation", "recommendation"))
            return "STRATEGY";
        if (has(m, "merci", "thanks", "thank you"))
            return "THANKS";
        if (has(m, "comparer", "compare", "comparison", "vs", "versus"))
            return "COMPARISON";

        return "PERFORMANCE";
    }

    private boolean isCasualGreeting(String m) {
        if (m.isEmpty()) return false;
        String[] casual = {
            "hey", "hi", "hello", "salut", "bonjour", "bonsoir", "coucou", "yo",
            "how are you", "how do you do", "ça va", "comment ça va", "quoi de neuf",
            "good morning", "good afternoon", "good evening", "good day",
            "bonjour à tous", "hi there", "hello there", "hey there",
            "good to see you", "nice to meet you", "enchanté", "ravi",
            "start", "aide", "help", "begin", "commencer"
        };
        for (String w : casual) {
            if (m.equals(w) || m.startsWith(w + " ") || m.endsWith(" " + w) || m.contains(" " + w + " "))
                return true;
        }
        // Very short message that looks like a greeting (1–3 words, no numbers)
        String[] words = m.split("\\s+");
        if (words.length <= 3 && has(m, "hi", "hey", "hello", "salut", "bonjour", "yo", "ça va"))
            return true;
        return false;
    }

    private boolean isFollowUp(String lastUserMessage, List<Map<String, String>> allMessages) {
        if (allMessages == null || allMessages.size() < 2) return false;
        String trimmed = lastUserMessage.trim();
        if (trimmed.length() > 80) return false; // long message = probably new question
        String[] words = trimmed.split("\\s+");
        if (words.length > 5) return false; // more than 5 words = likely new topic
        return FOLLOW_UP_PATTERN.matcher(trimmed).find();
    }

    private String getLastUserMessageBeforeCurrent(List<Map<String, String>> allMessages) {
        // Last element is the current user message; we want the previous user message
        for (int i = allMessages.size() - 2; i >= 0; i--) {
            if ("user".equals(allMessages.get(i).get("role")))
                return allMessages.get(i).get("content");
        }
        return null;
    }

    private boolean has(String text, String... words) {
        for (String w : words) if (text.contains(w)) return true;
        return false;
    }

    private boolean isConfirmAction(String m) {
        if (m == null || m.length() > 20) return false;
        return m.matches("(?i)^\\s*(oui|yes|confirme|confirm|ok|confirmé|confirmed)\\s*$");
    }

    private boolean isCancelAction(String m) {
        if (m == null || m.length() > 20) return false;
        return m.matches("(?i)^\\s*(non|no|cancel|annule|annuler|cancelled)\\s*$");
    }
}
