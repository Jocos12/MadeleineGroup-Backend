package rw.madeleinegroup.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "package_items")
public class PackageItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_type", nullable = false)
    private PricingType pricingType = PricingType.FIXED;

    @Column(name = "min_guests")
    private Integer minGuests;

    @Column(name = "max_guests")
    private Integer maxGuests;

    private String priceUnit;

    private String category;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    public PackageItem() {
    }

    public PackageItem(Long id, Branch branch, Department department, String name, String description,
                       BigDecimal price, PricingType pricingType, Integer minGuests, Integer maxGuests,
                       String priceUnit, String category, Boolean isFeatured) {
        this.id = id;
        this.branch = branch;
        this.department = department;
        this.name = name;
        this.description = description;
        this.price = price;
        this.pricingType = pricingType != null ? pricingType : PricingType.FIXED;
        this.minGuests = minGuests;
        this.maxGuests = maxGuests;
        this.priceUnit = priceUnit;
        this.category = category;
        this.isFeatured = isFeatured;
    }

    public static PackageItemBuilder builder() {
        return new PackageItemBuilder();
    }

    public static class PackageItemBuilder {
        private Long id;
        private Branch branch;
        private Department department;
        private String name;
        private String description;
        private BigDecimal price;
        private PricingType pricingType = PricingType.FIXED;
        private Integer minGuests;
        private Integer maxGuests;
        private String priceUnit;
        private String category;
        private Boolean isFeatured;

        public PackageItemBuilder id(Long id) { this.id = id; return this; }
        public PackageItemBuilder branch(Branch branch) { this.branch = branch; return this; }
        public PackageItemBuilder department(Department department) { this.department = department; return this; }
        public PackageItemBuilder name(String name) { this.name = name; return this; }
        public PackageItemBuilder description(String description) { this.description = description; return this; }
        public PackageItemBuilder price(BigDecimal price) { this.price = price; return this; }
        public PackageItemBuilder pricingType(PricingType pricingType) { this.pricingType = pricingType; return this; }
        public PackageItemBuilder minGuests(Integer minGuests) { this.minGuests = minGuests; return this; }
        public PackageItemBuilder maxGuests(Integer maxGuests) { this.maxGuests = maxGuests; return this; }
        public PackageItemBuilder priceUnit(String priceUnit) { this.priceUnit = priceUnit; return this; }
        public PackageItemBuilder category(String category) { this.category = category; return this; }
        public PackageItemBuilder isFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; return this; }

        public PackageItem build() {
            return new PackageItem(id, branch, department, name, description, price, pricingType, minGuests, maxGuests, priceUnit, category, isFeatured);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public PricingType getPricingType() { return pricingType; }
    public void setPricingType(PricingType pricingType) { this.pricingType = pricingType; }
    public Integer getMinGuests() { return minGuests; }
    public void setMinGuests(Integer minGuests) { this.minGuests = minGuests; }
    public Integer getMaxGuests() { return maxGuests; }
    public void setMaxGuests(Integer maxGuests) { this.maxGuests = maxGuests; }
    public String getPriceUnit() { return priceUnit; }
    public void setPriceUnit(String priceUnit) { this.priceUnit = priceUnit; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
}
