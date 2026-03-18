package rw.madeleinegroup.dto;

import java.time.LocalDateTime;

public class ClientResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String notes;
    private String profilePhotoUrl;
    private Long branchId;
    private String branchName;
    private LocalDateTime createdAt;

    public ClientResponse() {}
    public ClientResponse(Long id, String fullName, String email, String phone, String address, String notes,
                          String profilePhotoUrl, Long branchId, String branchName, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.notes = notes;
        this.profilePhotoUrl = profilePhotoUrl;
        this.branchId = branchId;
        this.branchName = branchName;
        this.createdAt = createdAt;
    }
    public static ClientResponseBuilder builder() { return new ClientResponseBuilder(); }
    public static class ClientResponseBuilder {
        private Long id; private String fullName; private String email; private String phone; private String address;
        private String notes; private String profilePhotoUrl; private Long branchId; private String branchName;
        private LocalDateTime createdAt;
        public ClientResponseBuilder id(Long id) { this.id = id; return this; }
        public ClientResponseBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public ClientResponseBuilder email(String email) { this.email = email; return this; }
        public ClientResponseBuilder phone(String phone) { this.phone = phone; return this; }
        public ClientResponseBuilder address(String address) { this.address = address; return this; }
        public ClientResponseBuilder notes(String notes) { this.notes = notes; return this; }
        public ClientResponseBuilder profilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; return this; }
        public ClientResponseBuilder branchId(Long branchId) { this.branchId = branchId; return this; }
        public ClientResponseBuilder branchName(String branchName) { this.branchName = branchName; return this; }
        public ClientResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ClientResponse build() { return new ClientResponse(id, fullName, email, phone, address, notes, profilePhotoUrl, branchId, branchName, createdAt); }
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; } public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; } public void setAddress(String address) { this.address = address; }
    public String getNotes() { return notes; } public void setNotes(String notes) { this.notes = notes; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; } public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
    public Long getBranchId() { return branchId; } public void setBranchId(Long branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; } public void setBranchName(String branchName) { this.branchName = branchName; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
