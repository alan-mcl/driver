package za.driver.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VehicleFilterPreferences {

    @JsonProperty("maxPrice")
    @JsonAlias("maxPriceZar")
    private BigDecimal maxPrice;
    private BodyType bodyType;
    private FuelType fuelType;
    private VehicleStatus status;
    private Double minOverallScore;
    private Integer minGarageClearanceMm;

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public BodyType getBodyType() {
        return bodyType;
    }

    public void setBodyType(BodyType bodyType) {
        this.bodyType = bodyType;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public Double getMinOverallScore() {
        return minOverallScore;
    }

    public void setMinOverallScore(Double minOverallScore) {
        this.minOverallScore = minOverallScore;
    }

    public Integer getMinGarageClearanceMm() {
        return minGarageClearanceMm;
    }

    public void setMinGarageClearanceMm(Integer minGarageClearanceMm) {
        this.minGarageClearanceMm = minGarageClearanceMm;
    }
}
