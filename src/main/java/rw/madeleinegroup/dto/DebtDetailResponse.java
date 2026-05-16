package rw.madeleinegroup.dto;

import java.util.ArrayList;
import java.util.List;

public class DebtDetailResponse {
    private DebtBookingRowDto booking;
    private List<DebtPaymentItemDto> debtPayments = new ArrayList<>();
    private List<DebtReminderItemDto> debtReminders = new ArrayList<>();

    public DebtBookingRowDto getBooking() { return booking; }
    public void setBooking(DebtBookingRowDto booking) { this.booking = booking; }
    public List<DebtPaymentItemDto> getDebtPayments() { return debtPayments; }
    public void setDebtPayments(List<DebtPaymentItemDto> debtPayments) { this.debtPayments = debtPayments != null ? debtPayments : new ArrayList<>(); }
    public List<DebtReminderItemDto> getDebtReminders() { return debtReminders; }
    public void setDebtReminders(List<DebtReminderItemDto> debtReminders) { this.debtReminders = debtReminders != null ? debtReminders : new ArrayList<>(); }
}
