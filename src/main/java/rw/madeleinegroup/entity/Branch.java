package rw.madeleinegroup.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "branches")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "bookings", "payments", "packages"})
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    private String address;

    private String phone;

    private String email;

    @Column(name = "manager_name")
    private String managerName;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PackageItem> packages = new ArrayList<>();

    public Branch() {
    }

    public Branch(Long id, String code, String name, String description,
                  List<Booking> bookings, List<Payment> payments, List<PackageItem> packages) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.active = true;
        this.bookings = bookings != null ? bookings : new ArrayList<>();
        this.payments = payments != null ? payments : new ArrayList<>();
        this.packages = packages != null ? packages : new ArrayList<>();
    }

    public static BranchBuilder builder() {
        return new BranchBuilder();
    }

    public static class BranchBuilder {
        private Long id;
        private String code;
        private String name;
        private String description;
        private List<Booking> bookings = new ArrayList<>();
        private List<Payment> payments = new ArrayList<>();
        private List<PackageItem> packages = new ArrayList<>();

        public BranchBuilder id(Long id) { this.id = id; return this; }
        public BranchBuilder code(String code) { this.code = code; return this; }
        public BranchBuilder name(String name) { this.name = name; return this; }
        public BranchBuilder description(String description) { this.description = description; return this; }
        public BranchBuilder bookings(List<Booking> b) { this.bookings = b; return this; }
        public BranchBuilder payments(List<Payment> p) { this.payments = p; return this; }
        public BranchBuilder packages(List<PackageItem> p) { this.packages = p; return this; }

        public Branch build() {
            return new Branch(id, code, name, description, bookings, payments, packages);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings != null ? bookings : new ArrayList<>(); }
    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments != null ? payments : new ArrayList<>(); }
    public List<PackageItem> getPackages() { return packages; }
    public void setPackages(List<PackageItem> packages) { this.packages = packages != null ? packages : new ArrayList<>(); }
}
