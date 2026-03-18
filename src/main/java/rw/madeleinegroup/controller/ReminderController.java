package rw.madeleinegroup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import rw.madeleinegroup.dto.*;
import rw.madeleinegroup.repository.AiEmailActionRepository;
import rw.madeleinegroup.service.CustomUserDetailsService;
import rw.madeleinegroup.service.ReminderService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reminders")
@PreAuthorize("hasAnyRole('CEO', 'ADMIN', 'MANAGER')")
public class ReminderController {

    private final ReminderService reminderService;
    private final AiEmailActionRepository aiEmailActionRepository;

    public ReminderController(ReminderService reminderService,
                              AiEmailActionRepository aiEmailActionRepository) {
        this.reminderService = reminderService;
        this.aiEmailActionRepository = aiEmailActionRepository;
    }

    @GetMapping("/eligible-clients")
    public ResponseEntity<List<EligibleClientDto>> getEligibleClients() {
        List<EligibleClientDto> list = reminderService.getEligibleClients();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/generate-email")
    public ResponseEntity<?> generateEmail(
            @RequestBody GenerateEmailRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        try {
            GenerateEmailResponse response = reminderService.generateEmail(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to generate email"));
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> send(
            @RequestBody SendRemindersRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        SendRemindersResponse response = reminderService.sendReminders(request, principal.getId());
        Map<String, Object> body = new HashMap<>();
        body.put("sentCount", response.getSentCount());
        body.put("failedCount", response.getFailedCount());
        body.put("contactedClientNames", response.getContactedClientNames());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        List<Map<String, Object>> list = aiEmailActionRepository.findTop20ByOrderByTriggeredAtDesc().stream()
            .map(a -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", a.getId());
                m.put("actionType", a.getActionType());
                m.put("emailsSent", a.getEmailsSent());
                m.put("totalAmountRwf", a.getTotalAmountRwf());
                m.put("clientsContacted", a.getClientsContacted());
                m.put("triggeredAt", a.getTriggeredAt());
                m.put("notes", a.getNotes());
                m.put("triggeredByUserId", a.getTriggeredBy() != null ? a.getTriggeredBy().getId() : null);
                return m;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
