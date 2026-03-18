package rw.madeleinegroup.dto;

import rw.madeleinegroup.entity.PricingType;

import java.math.BigDecimal;

public class PackageItemResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private PricingType pricingType;
    private Integer minGuests;
    private Integer maxGuests;
    private String priceUnit;

    public PackageItemResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
}
