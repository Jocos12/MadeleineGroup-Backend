package rw.madeleinegroup.dto;

import java.util.ArrayList;
import java.util.List;

public class InvoiceSendResultDto {
    private int sent;
    private int failed;
    private List<String> errors = new ArrayList<>();

    public int getSent() { return sent; }
    public void setSent(int sent) { this.sent = sent; }
    public int getFailed() { return failed; }
    public void setFailed(int failed) { this.failed = failed; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors != null ? errors : new ArrayList<>(); }
}
