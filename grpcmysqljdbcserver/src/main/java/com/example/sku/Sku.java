package com.example.sku;


import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Sku {
    private Long id;
    private Long warehouseId;
    private Long itemId;
    private Integer amount;
    private String countryCode;
    private AvailabilityType availabilityType = AvailabilityType.READY_TO_SHIP;
    private BigDecimal priceAmount;
    private String currencyCode;
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