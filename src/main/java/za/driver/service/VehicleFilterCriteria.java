package za.driver.service;

import java.math.BigDecimal;

import za.driver.model.BodyType;
import za.driver.model.FuelType;
import za.driver.model.VehicleStatus;

public record VehicleFilterCriteria(
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BodyType bodyType,
        FuelType fuelType,
        Double minOverallScore,
        Integer minGarageClearanceMm,
        VehicleStatus status) {

    public static VehicleFilterCriteria empty() {
        return new VehicleFilterCriteria(null, null, null, null, null, null, null);
    }
}
