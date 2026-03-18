package rw.madeleinegroup.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BookingRequest {
    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotBlank(message = "Event type is required")
    private String eventType;

    @NotNull(message = "Event date is required")
    @FutureOrPresent(message = "Event date must be today or in the future")
    private LocalDate eventDate;

    @Min(value = 1, message = "Guest count must be at least 1")
    @Max(value = 500, message = "Guest count cannot exceed 500")
    private Integer guestCount;

    private String notes;

    private BigDecimal estimatedAmount;

    private List<Long> packageIds;

    @Valid
    private List<BookingPackageRequest> packages;

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public Integer getGuestCount() { return guestCount; }
    public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public BigDecimal getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(BigDecimal estimatedAmount) { this.estimatedAmount = estimatedAmount; }
    public List<Long> getPackageIds() { return packageIds; }
    public void setPackageIds(List<Long> packageIds) { this.packageIds = packageIds; }
    public List<BookingPackageRequest> getPackages() { return packages; }
    public void setPackages(List<BookingPackageRequest> packages) { this.packages = packages; }
}
