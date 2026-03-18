package rw.madeleinegroup.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "others_role_specification")
    private String othersRoleSpecification;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Boolean emailVerified = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ClientExperience> clientExperiences = new ArrayList<>();

    public User() {
    }

    public User(Long id, String email, String password, String fullName, String phone, Role role,
                String othersRoleSpecification, String profilePhotoUrl, Branch branch,
                Boolean enabled, Boolean emailVerified, LocalDateTime createdAt, LocalDateTime updatedAt,
                List<ClientExperience> clientExperiences) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.othersRoleSpecification = othersRoleSpecification;
        this.profilePhotoUrl = profilePhotoUrl;
        this.branch = branch;
        this.enabled = enabled != null ? enabled : true;
        this.emailVerified = emailVerified != null ? emailVerified : false;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.clientExperiences = clientExperiences != null ? clientExperiences : new ArrayList<>();
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private Long id;
        private String email;
        private String password;
        private String fullName;
        private String phone;
        private Role role;
        private String othersRoleSpecification;
        private String profilePhotoUrl;
        private Branch branch;
        private Boolean enabled = true;
        private Boolean emailVerified = false;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<ClientExperience> clientExperiences = new ArrayList<>();

        public UserBuilder id(Long id) { this.id = id; return this; }
        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder password(String password) { this.password = password; return this; }
        public UserBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public UserBuilder phone(String phone) { this.phone = phone; return this; }
        public UserBuilder role(Role role) { this.role = role; return this; }
        public UserBuilder othersRoleSpecification(String s) { this.othersRoleSpecification = s; return this; }
        public UserBuilder profilePhotoUrl(String s) { this.profilePhotoUrl = s; return this; }
        public UserBuilder branch(Branch branch) { this.branch = branch; return this; }
        public UserBuilder enabled(Boolean b) { this.enabled = b; return this; }
        public UserBuilder emailVerified(Boolean b) { this.emailVerified = b; return this; }
        public UserBuilder createdAt(LocalDateTime t) { this.createdAt = t; return this; }
        public UserBuilder updatedAt(LocalDateTime t) { this.updatedAt = t; return this; }
        public UserBuilder clientExperiences(List<ClientExperience> l) { this.clientExperiences = l; return this; }

        public User build() {
            return new User(id, email, password, fullName, phone, role, othersRoleSpecification,
                    profilePhotoUrl, branch, enabled, emailVerified, createdAt, updatedAt, clientExperiences);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    @JsonIgnore
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
    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<ClientExperience> getClientExperiences() { return clientExperiences; }
    public void setClientExperiences(List<ClientExperience> clientExperiences) { this.clientExperiences = clientExperiences != null ? clientExperiences : new ArrayList<>(); }
}
