package com.example.sku;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Document(collection = "skus")
public class Sku {
    @Id
    private Long id;
    
    @Field("warehouse_id")
    private Long warehouseId;
    
    @Field("item_id")
    private Long itemId;
    
    private Integer amount;
    
    @Field("country_code")
    private String countryCode;
    
    @Field("availability_type")
    private AvailabilityType availabilityType = AvailabilityType.READY_TO_SHIP;
    
    @Field("price_amount")
    private BigDecimal priceAmount;
    
    @Field("currency_code")
    private String currencyCode;
    
    @Field("last_updated")
    private OffsetDateTime lastUpdated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public AvailabilityType getAvailabilityType() {
        return availabilityType;
    }

    public void setAvailabilityType(AvailabilityType availabilityType) {
        this.availabilityType = availabilityType;
    }

    public BigDecimal getPriceAmount() {
        return priceAmount;
    }

    public void setPriceAmount(BigDecimal priceAmount) {
        this.priceAmount = priceAmount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "Sku{" +
                "id=" + id +
                ", warehouseId=" + warehouseId +
                ", itemId=" + itemId +
                ", amount=" + amount +
                ", countryCode='" + countryCode + '\'' +
                ", availabilityType=" + availabilityType +
                ", priceAmount=" + priceAmount +
                ", currencyCode='" + currencyCode + '\'' +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
