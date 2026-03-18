package rw.madeleinegroup.dto;

import java.util.List;

/**
 * Result of an AI advisor chat call: reply text and optional health score for UI badge.
 * When the AI asks for email confirmation, confirmationRequest is true and payload is set.
 */
public class AiAdvisorResult {
    private String reply;
    private Integer healthScore;
    private Boolean confirmationRequest;
    private String confirmationActionType;
    private List<String> confirmationClients;
    private String confirmationTotalRwf;

    public AiAdvisorResult(String reply, Integer healthScore) {
        this.reply = reply;
        this.healthScore = healthScore;
    }

    public static AiAdvisorResult confirmation(String reply, Integer healthScore, String actionType,
                                               List<String> clients, String totalRwf) {
        AiAdvisorResult r = new AiAdvisorResult(reply, healthScore);
        r.setConfirmationRequest(true);
        r.setConfirmationActionType(actionType);
        r.setConfirmationClients(clients);
        r.setConfirmationTotalRwf(totalRwf);
        return r;
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public Integer getHealthScore() { return healthScore; }
    public void setHealthScore(Integer healthScore) { this.healthScore = healthScore; }
    public Boolean getConfirmationRequest() { return confirmationRequest; }
    public void setConfirmationRequest(Boolean confirmationRequest) { this.confirmationRequest = confirmationRequest; }
    public String getConfirmationActionType() { return confirmationActionType; }
    public void setConfirmationActionType(String s) { this.confirmationActionType = s; }
    public List<String> getConfirmationClients() { return confirmationClients; }
    public void setConfirmationClients(List<String> list) { this.confirmationClients = list; }
    public String getConfirmationTotalRwf() { return confirmationTotalRwf; }
    public void setConfirmationTotalRwf(String s) { this.confirmationTotalRwf = s; }
}
