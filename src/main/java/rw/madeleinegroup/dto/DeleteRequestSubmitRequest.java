package rw.madeleinegroup.dto;

import jakarta.validation.constraints.Size;

public class DeleteRequestSubmitRequest {

    @Size(max = 5000)
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
