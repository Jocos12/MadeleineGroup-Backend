package rw.madeleinegroup.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class InvoiceSendRequest {
    @NotEmpty
    private List<Long> bookingIds = new ArrayList<>();

    public List<Long> getBookingIds() { return bookingIds; }
    public void setBookingIds(List<Long> bookingIds) { this.bookingIds = bookingIds != null ? bookingIds : new ArrayList<>(); }
}
