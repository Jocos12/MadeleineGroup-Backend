package rw.madeleinegroup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import rw.madeleinegroup.common.ApiResponse;
import rw.madeleinegroup.dto.AiAdvisorRequest;
import rw.madeleinegroup.dto.AiAdvisorResult;
import rw.madeleinegroup.entity.AiEmailAction;
import rw.madeleinegroup.entity.AiUsageLog;
import rw.madeleinegroup.repository.AiEmailActionRepository;
import rw.madeleinegroup.repository.AiUsageLogRepository;
import rw.madeleinegroup.repository.UserRepository;
import rw.madeleinegroup.service.AiAdvisorService;
import rw.madeleinegroup.service.CustomUserDetailsService;
import rw.madeleinegroup.service.GroqAiService;
import rw.madeleinegroup.service.RateLimitService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api/ai")
public class AiAdvisorController {

    private final AiAdvisorService aiAdvisorService;
    private final GroqAiService groqAiService;
    private final RateLimitService rateLimitService;
    private final AiUsageLogRepository aiUsageLogRepository;
    private final AiEmailActionRepository aiEmailActionRepository;
    private final UserRepository userRepository;

    public AiAdvisorController(AiAdvisorService aiAdvisorService,
                               GroqAiService groqAiService,
                               RateLimitService rateLimitService,
                               AiUsageLogRepository aiUsageLogRepository,
                               AiEmailActionRepository aiEmailActionRepository,
                               UserRepository userRepository) {
        this.aiAdvisorService = aiAdvisorService;
        this.groqAiService = groqAiService;
        this.rateLimitService = rateLimitService;
        this.aiUsageLogRepository = aiUsageLogRepository;
        this.aiEmailActionRepository = aiEmailActionRepository;
        this.userRepository = userRepository;
    }

    /** Test Groq connection: sends "hello" and returns raw response. No auth required for easy curl/Postman check. */
    @GetMapping("/groq-test")
    public ResponseEntity<?> groqTest() {
        return ResponseEntity.ok(groqAiService.groqTest());
    }

    @PostMapping("/finance-advisor")
    public ResponseEntity<?> chat(
            @RequestBody AiAdvisorRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required"));
        }

        if (!rateLimitService.tryConsume(principal.getId())) {
            long remaining = rateLimitService.getRemainingTokens(principal.getId());
            AiUsageLog rateLimitLog = new AiUsageLog();
            rateLimitLog.setUser(userRepository.findById(principal.getId()).orElse(null));
            rateLimitLog.setUserMessage(request.getMessages() != null && !request.getMessages().isEmpty()
                ? request.getMessages().get(request.getMessages().size() - 1).get("content") : null);
            rateLimitLog.setWasRateLimited(true);
            rateLimitLog.setErrorOccurred(false);
            aiUsageLogRepository.save(rateLimitLog);

            return ResponseEntity.status(429).body(
                ApiResponse.error("Rate limit exceeded. You have used all 20 requests for this hour. Please try again later.", Map.of("remainingRequests", remaining)));
        }

        long startTime = System.currentTimeMillis();
        String lastUserMessage = request.getMessages() != null && !request.getMessages().isEmpty()
            ? request.getMessages().get(request.getMessages().size() - 1).get("content") : null;

        request.setUserId(principal.getId());

        try {
            CompletableFuture<AiAdvisorResult> future = aiAdvisorService.chatAsync(request);
            AiAdvisorResult result = future.get(30, TimeUnit.SECONDS);
            String reply = result != null && result.getReply() != null ? result.getReply() : "";
            long responseTime = System.currentTimeMillis() - startTime;

            AiUsageLog log = new AiUsageLog();
            log.setUser(userRepository.findById(principal.getId()).orElse(null));
            log.setUserMessage(lastUserMessage);
            log.setResponseLength(reply.length());
            log.setResponseTimeMs(responseTime);
            log.setWasRateLimited(false);
            log.setErrorOccurred(false);
            aiUsageLogRepository.save(log);

            long remaining = rateLimitService.getRemainingTokens(principal.getId());
            Map<String, Object> body = new HashMap<>(Map.of(
                "reply", reply,
                "remainingRequests", remaining
            ));
            if (result != null && result.getHealthScore() != null)
                body.put("healthScore", result.getHealthScore());
            if (result != null && Boolean.TRUE.equals(result.getConfirmationRequest())) {
                body.put("confirmationRequest", true);
                body.put("confirmationActionType", result.getConfirmationActionType());
                body.put("confirmationClients", result.getConfirmationClients());
                body.put("confirmationTotalRwf", result.getConfirmationTotalRwf());
            }
            return ResponseEntity.ok(body);
        } catch (TimeoutException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            AiUsageLog log = new AiUsageLog();
            log.setUser(userRepository.findById(principal.getId()).orElse(null));
            log.setUserMessage(lastUserMessage);
            log.setResponseTimeMs(responseTime);
            log.setWasRateLimited(false);
            log.setErrorOccurred(true);
            aiUsageLogRepository.save(log);
            return ResponseEntity.status(504).body(ApiResponse.error("AI response timed out. Please try again."));
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            AiUsageLog log = new AiUsageLog();
            log.setUser(userRepository.findById(principal.getId()).orElse(null));
            log.setUserMessage(lastUserMessage);
            log.setResponseTimeMs(responseTime);
            log.setWasRateLimited(false);
            log.setErrorOccurred(true);
            aiUsageLogRepository.save(log);
            return ResponseEntity.status(500).body(ApiResponse.error("AI error: " + e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        Map<String, Object> status = aiAdvisorService.getStatus();
        return ResponseEntity.ok(status);
    }

    @PostMapping("/finance-advisor/local")
    public ResponseEntity<?> chatLocal(
            @RequestBody AiAdvisorRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required"));
        }
        if (!rateLimitService.tryConsume(principal.getId())) {
            long remaining = rateLimitService.getRemainingTokens(principal.getId());
            return ResponseEntity.status(429).body(
                ApiResponse.error("Rate limit exceeded.", Map.of("remainingRequests", remaining)));
        }
        request.setUserId(principal.getId());
        try {
            AiAdvisorResult result = aiAdvisorService.chatLocal(request);
            String reply = result != null && result.getReply() != null ? result.getReply() : "";
            long remaining = rateLimitService.getRemainingTokens(principal.getId());
            Map<String, Object> body = new HashMap<>(Map.of("reply", reply, "remainingRequests", remaining));
            if (result != null && result.getHealthScore() != null)
                body.put("healthScore", result.getHealthScore());
            body.put("localFallback", true);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Local AI error: " + e.getMessage()));
        }
    }

    @GetMapping("/email-actions")
    @PreAuthorize("hasAnyRole('CEO', 'ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getEmailActions() {
        List<AiEmailAction> list = aiEmailActionRepository.findTop20ByOrderByTriggeredAtDesc();
        List<Map<String, Object>> out = new java.util.ArrayList<>();
        for (AiEmailAction a : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("actionType", a.getActionType());
            m.put("emailsSent", a.getEmailsSent());
            m.put("totalAmountRwf", a.getTotalAmountRwf());
            m.put("clientsContacted", a.getClientsContacted());
            m.put("triggeredAt", a.getTriggeredAt());
            m.put("notes", a.getNotes());
            m.put("triggeredByUserId", a.getTriggeredBy() != null ? a.getTriggeredBy().getId() : null);
            out.add(m);
        }
        return ResponseEntity.ok(out);
    }

    @GetMapping("/usage-stats")
    @PreAuthorize("hasRole('CEO')")
    public ResponseEntity<?> getUsageStats() {
        List<AiUsageLog> logs = aiUsageLogRepository.findTop50ByOrderByCreatedAtDesc();
        long totalRequests = aiUsageLogRepository.count();
        double avgResponseTime = logs.stream()
            .mapToLong(l -> l.getResponseTimeMs() != null ? l.getResponseTimeMs() : 0)
            .average()
            .orElse(0);
        return ResponseEntity.ok(Map.of(
            "totalRequests", totalRequests,
            "recentLogs", logs,
            "avgResponseTimeMs", avgResponseTime
        ));
    }
}
