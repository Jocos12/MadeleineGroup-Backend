package rw.madeleinegroup.dto;

import java.util.List;
import java.util.Map;

public class AiAdvisorRequest {

    private List<Map<String, String>> messages;
    private Map<String, Object> financialContext;
    /** Optional: set by controller for session-scoped state (e.g. pending email confirmation). */
    private Long userId;

    public List<Map<String, String>> getMessages() {
        return messages;
    }

    public void setMessages(List<Map<String, String>> messages) {
        this.messages = messages;
    }

    public Map<String, Object> getFinancialContext() {
        return financialContext;
    }

    public void setFinancialContext(Map<String, Object> financialContext) {
        this.financialContext = financialContext;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
