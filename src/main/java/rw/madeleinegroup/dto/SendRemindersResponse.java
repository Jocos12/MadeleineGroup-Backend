package rw.madeleinegroup.dto;

import java.util.List;

/**
 * Result of sending reminder emails.
 */
public class SendRemindersResponse {
    private int sentCount;
    private int failedCount;
    private List<String> contactedClientNames;

    public int getSentCount() { return sentCount; }
    public void setSentCount(int sentCount) { this.sentCount = sentCount; }
    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
    public List<String> getContactedClientNames() { return contactedClientNames; }
    public void setContactedClientNames(List<String> contactedClientNames) { this.contactedClientNames = contactedClientNames; }
}
