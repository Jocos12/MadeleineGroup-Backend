package rw.madeleinegroup.dto;

import rw.madeleinegroup.entity.Role;

import java.time.LocalDateTime;

public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private Role role;
    private String othersRoleSpecification;
    private String profilePhotoUrl;
    private Long branchId;
    private String branchName;
    private Boolean enabled;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByName;
    private String createdByEmail;

    public UserResponse() {}
    public UserResponse(Long id, String email, String fullName, String phone, Role role, String othersRoleSpecification,
                        String profilePhotoUrl, Long branchId, String branchName, Boolean enabled, Boolean emailVerified,
                        LocalDateTime createdAt, LocalDateTime updatedAt, String createdByName, String createdByEmail) {
        this.id = id; this.email = email; this.fullName = fullName; this.phone = phone; this.role = role;
        this.othersRoleSpecification = othersRoleSpecification; this.profilePhotoUrl = profilePhotoUrl;
        this.branchId = branchId; this.branchName = branchName; this.enabled = enabled != null ? enabled : true;
        this.emailVerified = emailVerified != null ? emailVerified : false;
        this.createdAt = createdAt; this.updatedAt = updatedAt;
        this.createdByName = createdByName; this.createdByEmail = createdByEmail;
    }
    public static UserResponseBuilder builder() { return new UserResponseBuilder(); }
    public static class UserResponseBuilder {
        private Long id; private String email; private String fullName; private String phone; private Role role;
        private String othersRoleSpecification; private String profilePhotoUrl; private Long branchId; private String branchName;
        private Boolean enabled; private Boolean emailVerified; private LocalDateTime createdAt; private LocalDateTime updatedAt;
        private String createdByName; private String createdByEmail;
        public UserResponseBuilder id(Long id) { this.id = id; return this; }
        public UserResponseBuilder email(String email) { this.email = email; return this; }
        public UserResponseBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public UserResponseBuilder phone(String phone) { this.phone = phone; return this; }
        public UserResponseBuilder role(Role role) { this.role = role; return this; }
        public UserResponseBuilder othersRoleSpecification(String s) { this.othersRoleSpecification = s; return this; }
        public UserResponseBuilder profilePhotoUrl(String s) { this.profilePhotoUrl = s; return this; }
        public UserResponseBuilder branchId(Long id) { this.branchId = id; return this; }
        public UserResponseBuilder branchName(String s) { this.branchName = s; return this; }
        public UserResponseBuilder enabled(Boolean b) { this.enabled = b; return this; }
        public UserResponseBuilder emailVerified(Boolean b) { this.emailVerified = b; return this; }
        public UserResponseBuilder createdAt(LocalDateTime t) { this.createdAt = t; return this; }
        public UserResponseBuilder updatedAt(LocalDateTime t) { this.updatedAt = t; return this; }
        public UserResponseBuilder createdByName(String s) { this.createdByName = s; return this; }
        public UserResponseBuilder createdByEmail(String s) { this.createdByEmail = s; return this; }
        public UserResponse build() {
            return new UserResponse(id, email, fullName, phone, role, othersRoleSpecification, profilePhotoUrl,
                branchId, branchName, enabled, emailVerified, createdAt, updatedAt, createdByName, createdByEmail);
        }
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; } public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public Role getRole() { return role; } public void setRole(Role role) { this.role = role; }
    public String getOthersRoleSpecification() { return othersRoleSpecification; } public void setOthersRoleSpecification(String s) { this.othersRoleSpecification = s; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; } public void setProfilePhotoUrl(String s) { this.profilePhotoUrl = s; }
    public Long getBranchId() { return branchId; } public void setBranchId(Long id) { this.branchId = id; }
    public String getBranchName() { return branchName; } public void setBranchName(String s) { this.branchName = s; }
    public Boolean getEnabled() { return enabled; } public void setEnabled(Boolean b) { this.enabled = b; }
    public Boolean getEmailVerified() { return emailVerified; } public void setEmailVerified(Boolean b) { this.emailVerified = b; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime t) { this.updatedAt = t; }
    public String getCreatedByName() { return createdByName; } public void setCreatedByName(String s) { this.createdByName = s; }
    public String getCreatedByEmail() { return createdByEmail; } public void setCreatedByEmail(String s) { this.createdByEmail = s; }
}
