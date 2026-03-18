package rw.madeleinegroup.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PackageItemRequest {
    @NotNull
    private Long branchId;
    @NotBlank
    private String name;
    private String description;
    @NotNull
    @DecimalMin("0")
    private BigDecimal price;
    private String priceUnit;
    private String category;
    private Boolean isFeatured;

    public PackageItemRequest() {}
    public PackageItemRequest(Long branchId, String name, String description, BigDecimal price, String priceUnit, String category, Boolean isFeatured) {
        this.branchId = branchId; this.name = name; this.description = description; this.price = price; this.priceUnit = priceUnit; this.category = category; this.isFeatured = isFeatured;
    }
    public static PackageItemRequestBuilder builder() { return new PackageItemRequestBuilder(); }
    public static class PackageItemRequestBuilder {
        private Long branchId; private String name, description, priceUnit, category; private BigDecimal price; private Boolean isFeatured;
        public PackageItemRequestBuilder branchId(Long v) { branchId = v; return this; } public PackageItemRequestBuilder name(String v) { name = v; return this; } public PackageItemRequestBuilder description(String v) { description = v; return this; } public PackageItemRequestBuilder price(BigDecimal v) { price = v; return this; } public PackageItemRequestBuilder priceUnit(String v) { priceUnit = v; return this; } public PackageItemRequestBuilder category(String v) { category = v; return this; } public PackageItemRequestBuilder isFeatured(Boolean v) { isFeatured = v; return this; }
        public PackageItemRequest build() { return new PackageItemRequest(branchId, name, description, price, priceUnit, category, isFeatured); }
    }
    public Long getBranchId() { return branchId; } public void setBranchId(Long v) { this.branchId = v; } public String getName() { return name; } public void setName(String v) { this.name = v; } public String getDescription() { return description; } public void setDescription(String v) { this.description = v; } public BigDecimal getPrice() { return price; } public void setPrice(BigDecimal v) { this.price = v; } public String getPriceUnit() { return priceUnit; } public void setPriceUnit(String v) { this.priceUnit = v; } public String getCategory() { return category; } public void setCategory(String v) { this.category = v; } public Boolean getIsFeatured() { return isFeatured; } public void setIsFeatured(Boolean v) { this.isFeatured = v; }
}
