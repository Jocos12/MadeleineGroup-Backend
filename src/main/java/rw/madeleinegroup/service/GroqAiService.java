package rw.madeleinegroup.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import rw.madeleinegroup.ai.DataAnonymizer;
import rw.madeleinegroup.ai.LiveFinancialSnapshot;
import rw.madeleinegroup.ai.MonthlyData;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
public class GroqAiService {

    private static final Logger log = LoggerFactory.getLogger(GroqAiService.class);
    private static final String PLACEHOLDER_KEY = "gsk_your_real_groq_key_here";
    private static final int MAX_HISTORY_MESSAGES = 10;

    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final int maxTokens;
    private final double temperature;
    /** Lower temperature for finance Q&A — more faithful to figures (default 0.42). */
    private final double financeChatTemperature;
    private final RestTemplate restTemplate;
    private final DataAnonymizer dataAnonymizer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GroqAiService(
            @Value("${groq.api.key:}") String apiKey,
            @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}") String apiUrl,
            @Value("${groq.model:llama3-70b-8192}") String model,
            @Value("${groq.max.tokens:1500}") int maxTokens,
            @Value("${groq.temperature:0.7}") double temperature,
            @Value("${groq.finance.temperature:0.42}") double financeChatTemperature,
            @Qualifier("groqRestTemplate") RestTemplate restTemplate,
            DataAnonymizer dataAnonymizer) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.apiUrl = apiUrl;
        this.model = model;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
        this.financeChatTemperature = financeChatTemperature;
        this.restTemplate = restTemplate;
        this.dataAnonymizer = dataAnonymizer;
    }

    @PostConstruct
    public void logStartupStatus() {
        boolean keyLooksValid = apiKey != null && apiKey.startsWith("gsk_") && !PLACEHOLDER_KEY.equals(apiKey);
        if (keyLooksValid) {
            log.info("[Groq] Groq AI ready — model: {}", model);
        } else {
            log.info("[Groq] Groq API key not configured (missing or placeholder) — falling back to local AI");
        }
    }

    public boolean isConfigured() {
        return !apiKey.isEmpty() && !PLACEHOLDER_KEY.equals(apiKey);
    }

    public String getModel() {
        return model;
    }

    /**
     * Sends a simple "hello" message to Groq and returns the raw response for connection testing.
     * Returns a map with: configured, success, model, responseTimeMs, content, rawResponse (or error).
     */
    public Map<String, Object> groqTest() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("configured", isConfigured());
        result.put("model", model);
        if (!isConfigured()) {
            result.put("success", false);
            result.put("error", "Groq API key not configured");
            return result;
        }

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "You are a helpful assistant. Reply briefly in one sentence."));
        messages.add(Map.of("role", "user", "content", "hello"));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 100);
        requestBody.put("temperature", 0.3);
        requestBody.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        log.info("[Groq] groq-test: Calling API with model {}", model);
        long startMs = System.currentTimeMillis();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            long responseTimeMs = System.currentTimeMillis() - startMs;
            log.info("[Groq] groq-test: Response received in {}ms", responseTimeMs);

            String body = response.getBody();
            result.put("responseTimeMs", responseTimeMs);
            result.put("rawResponse", body);

            if (body == null || body.isEmpty()) {
                result.put("success", false);
                result.put("error", "Empty response body");
                return result;
            }
            JsonNode root = objectMapper.readTree(body);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.size() == 0) {
                result.put("success", false);
                result.put("error", "No choices in response");
                return result;
            }
            String content = choices.get(0).path("message").path("content").asText("");
            result.put("success", true);
            result.put("content", content != null ? content.trim() : "");
            return result;
        } catch (ResourceAccessException e) {
            long responseTimeMs = System.currentTimeMillis() - startMs;
            String errMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            log.warn("[Groq] groq-test: Connection error after {}ms: {} (no API key logged)", responseTimeMs, errMsg);
            result.put("success", false);
            result.put("responseTimeMs", responseTimeMs);
            result.put("error", errMsg);
            return result;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            long responseTimeMs = System.currentTimeMillis() - startMs;
            int code = e.getStatusCode() != null ? e.getStatusCode().value() : 0;
            String errBody = e.getResponseBodyAsString();
            log.warn("[Groq] groq-test: HTTP {} after {}ms, body={} (no API key logged)", code, responseTimeMs, errBody);
            result.put("success", false);
            result.put("responseTimeMs", responseTimeMs);
            result.put("error", "HTTP " + code + " " + e.getStatusText());
            result.put("rawResponse", errBody);
            return result;
        } catch (Exception e) {
            long responseTimeMs = System.currentTimeMillis() - startMs;
            String errMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            log.warn("[Groq] groq-test: Error after {}ms: {} (no API key logged)", responseTimeMs, errMsg);
            result.put("success", false);
            result.put("responseTimeMs", responseTimeMs);
            result.put("error", errMsg);
            return result;
        }
    }

    /**
     * Call Groq chat completions. Do not call if API key is blank or placeholder.
     *
     * @param userMessage     current user message
     * @param conversationHistory list of maps with "role" and "content"
     * @param snapshot        live financial snapshot (will be anonymized before sending)
     * @param healthScore    optional 0-100 health score (computed locally); include when relevant
     * @return assistant reply text
     */
    public String chat(String userMessage, List<Map<String, String>> conversationHistory,
                      LiveFinancialSnapshot snapshot, Integer healthScore) {
        if (!isConfigured()) {
            throw new IllegalStateException("Groq API key is not configured");
        }

        LiveFinancialSnapshot anonymized = dataAnonymizer.anonymizeSnapshot(snapshot);
        String systemPrompt = buildSystemPrompt(anonymized, healthScore);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        int start = conversationHistory != null ? Math.max(0, conversationHistory.size() - MAX_HISTORY_MESSAGES) : 0;
        if (conversationHistory != null) {
            for (int i = start; i < conversationHistory.size(); i++) {
                Map<String, String> m = conversationHistory.get(i);
                String role = Objects.toString(m.get("role"), "user");
                if ("assistant".equals(role) || "user".equals(role)) {
                    String content = Objects.toString(m.get("content"), "").trim();
                    if (!content.isEmpty()) {
                        messages.add(Map.of("role", role, "content", content));
                    }
                }
            }
        }
        messages.add(Map.of("role", "user", "content", userMessage != null ? userMessage.trim() : ""));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", financeChatTemperature);
        requestBody.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        log.info("[Groq] Calling API with model {} (finance temperature={})", model, financeChatTemperature);
        long startMs = System.currentTimeMillis();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            long responseTimeMs = System.currentTimeMillis() - startMs;
            log.info("[Groq] Response received in {}ms", responseTimeMs);

            String body = response.getBody();
            if (body == null || body.isEmpty()) {
                log.warn("[Groq] API returned empty body");
                throw new RuntimeException("Groq returned an empty response");
            }
            JsonNode root = objectMapper.readTree(body);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.size() == 0) {
                log.warn("[Groq] Response had no choices");
                throw new RuntimeException("Groq returned no reply");
            }
            JsonNode message = choices.get(0).path("message");
            String content = message.path("content").asText("");
            return content != null ? content.trim() : "";
        } catch (ResourceAccessException e) {
            long responseTimeMs = System.currentTimeMillis() - startMs;
            String errMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            log.warn("[Groq] Connection error after {}ms: {} (no API key logged)", responseTimeMs, errMsg);
            if (errMsg.contains("timeout") || errMsg.contains("timed out")) {
                throw new RuntimeException("Groq is temporarily unavailable. Please try again in a moment.");
            }
            throw new RuntimeException("Groq is temporarily unavailable. Please try again in a moment.");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            long responseTimeMs = System.currentTimeMillis() - startMs;
            int code = e.getStatusCode() != null ? e.getStatusCode().value() : 0;
            String body = e.getResponseBodyAsString();
            log.warn("[Groq] HTTP error after {}ms: status={}, body={} (no API key logged)", responseTimeMs, code, body != null ? body : "");
            if (code == 401) {
                throw new RuntimeException("Groq API key is invalid. Please check your configuration.");
            }
            if (code == 429) {
                throw new RuntimeException("Rate limit exceeded. Please try again in a moment.");
            }
            throw new RuntimeException("Groq request failed: " + e.getStatusText());
        } catch (Exception e) {
            long responseTimeMs = System.currentTimeMillis() - startMs;
            String errMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            log.warn("[Groq] Error after {}ms: {} (no API key logged)", responseTimeMs, errMsg);
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException("Groq is temporarily unavailable. Please try again in a moment.");
        }
    }

    private String buildSystemPrompt(LiveFinancialSnapshot s, Integer healthScore) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are a professional financial advisor for an event services company. ");
        sb.append("You must always respond in the same language the user writes in (French or English).\n\n");

        String periodNote = s.getMonth() != null
            ? ("year " + s.getYear() + ", calendar month " + s.getMonth())
            : ("year " + s.getYear());

        sb.append("--- CRITICAL: TWO DIFFERENT METRICS (do not confuse them) ---\n");
        sb.append("(1) NET PROFIT / REVENUE KPIs below are for the SELECTED PERIOD ONLY (").append(periodNote).append(") — like the top row of the Finance dashboard.\n");
        sb.append("(2) WHAT WE KEEP (NET) is the SYSTEM-WIDE position: all recorded INCOME payments minus all expense-module records — ");
        sb.append("this matches the dashboard card \"What We Keep (Net)\" / \"Ce que nous gardons (net)\".\n");
        sb.append("When the user asks how much money they HAVE IN THE SYSTEM, their TOTAL in the account, \"combien j'ai\", \"montant total\", ");
        sb.append("\"solde\", \"l'argent dans mon système\", you MUST cite WHAT WE KEEP (NET), NOT the period net profit.\n\n");

        sb.append("--- SELECTED PERIOD KPIs (").append(periodNote).append(") — monthly / period view ---\n");
        if (healthScore != null) {
            sb.append("Financial health score (0–100, higher is better): ").append(healthScore).append(". ");
            sb.append("If the score is very low, say the business should review costs and collections; never invent a different score.\n");
        }
        sb.append("Total income (this period only): ").append(formatRwf(s.getTotalIncome())).append("\n");
        sb.append("Total expenses (this period only): ").append(formatRwf(s.getTotalExpenses())).append("\n");
        sb.append("Net profit (this period only — NOT \"money in the system\"): ").append(formatRwf(s.getNetProfit())).append("\n");
        sb.append("Profit margin (this period): ").append(String.format("%.1f", s.getProfitMargin())).append("%\n\n");

        sb.append("--- SYSTEM-WIDE POSITION (all-time in database; use for \"how much do I have\") ---\n");
        sb.append("Sum of all INCOME payment amounts recorded: ").append(formatRwf(s.getSystemWideIncomePaymentsTotal())).append("\n");
        sb.append("Sum of all expense-module (Dépenses) records: ").append(formatRwf(s.getSystemWideExpenseModuleTotal())).append("\n");
        sb.append("WHAT WE KEEP (NET) = ").append(formatRwf(s.getWhatWeKeepNet())).append("  <-- USE THIS for \"argent dans le système\" / total available position.\n\n");

        sb.append("--- RECEIVABLES ---\n");
        sb.append("Still to receive / Reste à recevoir (outstanding on bookings, same as dashboard): ").append(formatRwf(s.getPendingAmount())).append("\n\n");

        sb.append("--- BOOKINGS & CLIENTS (period-scoped where noted) ---\n");
        sb.append("Total bookings (period): ").append(s.getTotalBookings()).append("\n");
        sb.append("Confirmed: ").append(s.getConfirmedBookings()).append(", Completed: ").append(s.getCompletedBookings());
        sb.append(", Pending: ").append(s.getPendingBookings()).append(", Cancelled: ").append(s.getCancelledBookings()).append("\n");
        sb.append("Overdue bookings (count only — no client names in this snapshot): ").append(s.getOverdueBookings()).append("\n");
        sb.append("Total clients: ").append(s.getTotalClients()).append(", New this period: ").append(s.getNewClientsThisPeriod()).append("\n\n");

        List<MonthlyData> monthlyTrend = s.getMonthlyTrend();
        if (monthlyTrend != null && !monthlyTrend.isEmpty()) {
            sb.append("--- MONTHLY TREND (current year, by month) ---\n");
            for (MonthlyData md : monthlyTrend) {
                sb.append(md.getMonthName()).append(": income ").append(formatRwf(md.getIncome()))
                    .append(", expenses ").append(formatRwf(md.getExpenses()))
                    .append(", net ").append(formatRwf(md.getNetProfit())).append("\n");
            }
            sb.append("\n");
        }

        List<Object[]> categories = s.getCategoryBreakdown();
        if (categories != null && !categories.isEmpty()) {
            sb.append("--- EXPENSE BREAKDOWN BY CATEGORY (period) ---\n");
            for (Object[] row : categories) {
                if (row != null && row.length >= 2) {
                    sb.append(row[0]).append(": ").append(formatRwf(toDouble(row[1]))).append("\n");
                }
            }
            sb.append("\n");
        }

        List<Object[]> branches = s.getBranchPerformance();
        if (branches != null && !branches.isEmpty()) {
            sb.append("--- BRANCH REVENUE (period) ---\n");
            for (Object[] row : branches) {
                if (row != null && row.length >= 2) {
                    sb.append(row[0]).append(": ").append(formatRwf(toDouble(row[1]))).append("\n");
                }
            }
            sb.append("\n");
        }

        sb.append("--- RESPONSE RULES ---\n");
        sb.append("- Use ONLY numbers from this message; never invent amounts or client names.\n");
        sb.append("- For \"who owes us\" / debtor names: this snapshot does not list individual debtors. Say they should open Payments or Bookings in the app for details, or use reminder features.\n");
        sb.append("- Do not equate period net profit with total money in the system; use WHAT WE KEEP (NET) for the latter.\n");
        sb.append("- Be concise, structured (short paragraphs or bullets), and actionable.\n");
        sb.append("- Same language as the user (French or English).\n");

        return sb.toString();
    }

    private static String formatRwf(double v) {
        return String.format("%,.0f RWF", v);
    }

    private static double toDouble(Object o) {
        if (o == null) return 0;
        try {
            return Double.parseDouble(o.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Generate an email template (subject + body) using only non-PII parameters.
     * The body must use placeholders: CLIENT_NAME, BOOKING_REFERENCE, REMAINING_AMOUNT, EVENT_DATE.
     * No client names or amounts are sent to Groq.
     */
    public rw.madeleinegroup.dto.GenerateEmailResponse generateEmailTemplate(
            String emailType, String language, String userInstruction, String tone) {
        if (!isConfigured()) {
            throw new IllegalStateException("Groq API key is not configured");
        }
        String langLabel = "FR".equalsIgnoreCase(language) ? "French" : "English";
        String systemPrompt = "You are a professional email writer for an event services company (Madeleine Group). "
                + "Generate a single email template. You must write in " + langLabel + " only. "
                + "Use ONLY these placeholders in the email body and subject where appropriate: "
                + "CLIENT_NAME, BOOKING_REFERENCE, REMAINING_AMOUNT, EVENT_DATE. "
                + "Do not use real names, amounts, or dates. "
                + "Email type: " + (emailType != null ? emailType : "PAYMENT_REMINDER") + ". "
                + "Tone: " + (tone != null ? tone : "FORMAL") + ". ";
        String userContent = "Write the email subject and body. "
                + (userInstruction != null && !userInstruction.isBlank() ? "Additional instruction: " + userInstruction + ". " : "")
                + "Reply in this exact format, nothing else:\nSUBJECT:\n<one line subject>\nBODY:\n<email body with placeholders>";

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userContent));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 800);
        requestBody.put("temperature", 0.5);
        requestBody.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            String body = response.getBody();
            if (body == null || body.isEmpty()) throw new RuntimeException("Groq returned empty response");
            JsonNode root = objectMapper.readTree(body);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.size() == 0) throw new RuntimeException("Groq returned no reply");
            String content = choices.get(0).path("message").path("content").asText("").trim();
            String subject = "";
            String bodyText = content;
            int subIdx = content.toUpperCase().indexOf("SUBJECT:");
            int bodyIdx = content.toUpperCase().indexOf("BODY:");
            if (subIdx >= 0 && bodyIdx > subIdx) {
                subject = content.substring(subIdx + 8, bodyIdx).replaceAll("(?m)^\\s*", "").trim().split("\n")[0].trim();
                bodyText = content.substring(bodyIdx + 5).replaceAll("(?m)^\\s*", "").trim();
            } else if (content.contains("\n")) {
                String[] parts = content.split("\n", 2);
                subject = parts[0].trim();
                bodyText = parts.length > 1 ? parts[1].trim() : content;
            }
            return new rw.madeleinegroup.dto.GenerateEmailResponse(subject, bodyText);
        } catch (ResourceAccessException e) {
            log.warn("[Groq] Connection error generating email template: {}", e.getMessage());
            throw new RuntimeException("Groq is temporarily unavailable. Please try again.");
        } catch (Exception e) {
            log.warn("[Groq] Error generating email template: {}", e.getMessage());
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException("Failed to generate email template: " + e.getMessage());
        }
    }
}
