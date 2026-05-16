package rw.madeleinegroup.dto;

public class DebtBookingDebtPatchRequest {
    private String debtNotes;
    private String paymentMethod;

    public String getDebtNotes() { return debtNotes; }
    public void setDebtNotes(String debtNotes) { this.debtNotes = debtNotes; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
