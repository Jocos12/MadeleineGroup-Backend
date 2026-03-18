package rw.madeleinegroup.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Client eligible to receive a reminder (pending payment, overdue, or pending booking > 3 days).
 */
public class EligibleClientDto {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String profilePhotoUrl;
    private List<EligibleBookingDto> bookings;
    private BigDecimal totalRemainingBalance;
    private String eligibilityReason; // PENDING_PAYMENT, OVERDUE, PENDING_BOOKING
    private String priority;          // HIGH, MEDIUM, LOW

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
    public List<EligibleBookingDto> getBookings() { return bookings; }
    public void setBookings(List<EligibleBookingDto> bookings) { this.bookings = bookings; }
    public BigDecimal getTotalRemainingBalance() { return totalRemainingBalance; }
    public void setTotalRemainingBalance(BigDecimal totalRemainingBalance) { this.totalRemainingBalance = totalRemainingBalance; }
    public String getEligibilityReason() { return eligibilityReason; }
    public void setEligibilityReason(String eligibilityReason) { this.eligibilityReason = eligibilityReason; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
