package rw.madeleinegroup.dto;

import java.util.Set;

public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String fullName;
    private Set<String> roles;
    private String profilePhotoUrl;
    private boolean otpRequired;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String tokenType, Long userId, String email, String fullName,
                        Set<String> roles, String profilePhotoUrl, boolean otpRequired) {
        this.accessToken = accessToken;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
        this.profilePhotoUrl = profilePhotoUrl;
        this.otpRequired = otpRequired;
    }

    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }

    public static class AuthResponseBuilder {
        private String accessToken;
        private String tokenType = "Bearer";
        private Long userId;
        private String email;
        private String fullName;
        private Set<String> roles;
        private String profilePhotoUrl;
        private boolean otpRequired;

        public AuthResponseBuilder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public AuthResponseBuilder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public AuthResponseBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public AuthResponseBuilder email(String email) {
            this.email = email;
            return this;
        }

        public AuthResponseBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public AuthResponseBuilder roles(Set<String> roles) {
            this.roles = roles;
            return this;
        }

        public AuthResponseBuilder profilePhotoUrl(String profilePhotoUrl) {
            this.profilePhotoUrl = profilePhotoUrl;
            return this;
        }

        public AuthResponseBuilder otpRequired(boolean otpRequired) {
            this.otpRequired = otpRequired;
            return this;
        }

        public AuthResponse build() {
            return new AuthResponse(accessToken, tokenType, userId, email, fullName, roles, profilePhotoUrl, otpRequired);
        }
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
    public boolean isOtpRequired() { return otpRequired; }
    public void setOtpRequired(boolean otpRequired) { this.otpRequired = otpRequired; }
}
