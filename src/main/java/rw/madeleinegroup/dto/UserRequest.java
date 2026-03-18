package rw.madeleinegroup.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rw.madeleinegroup.entity.Role;

public class UserRequest {
    @NotBlank
    @Email
    private String email;
    private String password; // Optional for update
    @NotBlank
    private String fullName;
    private String phone;
    @NotNull
    private Role role;
    private String othersRoleSpecification; // DJ, Cleaner, etc. when role is OTHERS
    private String profilePhotoUrl;
    private Long branchId; // For MANAGER
    private Boolean enabled;
    private Boolean sendWelcomeEmail = true;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getOthersRoleSpecification() { return othersRoleSpecification; }
    public void setOthersRoleSpecification(String othersRoleSpecification) { this.othersRoleSpecification = othersRoleSpecification; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Boolean getSendWelcomeEmail() { return sendWelcomeEmail != null ? sendWelcomeEmail : true; }
    public void setSendWelcomeEmail(Boolean sendWelcomeEmail) { this.sendWelcomeEmail = sendWelcomeEmail; }
}
