package rw.madeleinegroup.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String address;

    @Column(length = 1000)
    private String notes;

    private String profilePhotoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Client() {
    }

    public static ClientBuilder builder() {
        return new ClientBuilder();
    }

    public static class ClientBuilder {
        private String fullName;
        private String email;
        private String phone;
        private String address;
        private String notes;
        private User createdBy;
        private Branch branch;

        public ClientBuilder fullName(String n) { this.fullName = n; return this; }
        public ClientBuilder email(String e) { this.email = e; return this; }
        public ClientBuilder phone(String p) { this.phone = p; return this; }
        public ClientBuilder address(String a) { this.address = a; return this; }
        public ClientBuilder notes(String n) { this.notes = n; return this; }
        public ClientBuilder createdBy(User u) { this.createdBy = u; return this; }
        public ClientBuilder branch(Branch b) { this.branch = b; return this; }

        public Client build() {
            Client c = new Client();
            c.setFullName(fullName);
            c.setEmail(email);
            c.setPhone(phone);
            c.setAddress(address);
            c.setNotes(notes);
            c.setCreatedBy(createdBy);
            c.setBranch(branch);
            return c;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
