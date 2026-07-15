package za.driver.service;

import java.math.BigDecimal;

import za.driver.garage.GarageClearanceCalculator;
import za.driver.model.DerivedMetrics;
import za.driver.model.Dimensions;
import za.driver.model.Engine;
import za.driver.model.GarageDimensions;
import za.driver.model.Pricing;
import za.driver.model.Vehicle;

public final class VehicleFilter {

    private VehicleFilter() {
    }

    public static boolean matches(Vehicle vehicle, VehicleFilterCriteria criteria, GarageDimensions garage) {
        if (vehicle == null) {
            return false;
        }
        if (criteria == null) {
            return true;
        }

        if (!matchesPrice(vehicle.getPricing(), criteria.minPrice(), criteria.maxPrice())) {
            return false;
        }
        if (criteria.bodyType() != null && vehicle.getBodyType() != criteria.bodyType()) {
            return false;
        }
        if (criteria.fuelType() != null) {
            Engine engine = vehicle.getEngine();
            if (engine == null || engine.getFuelType() != criteria.fuelType()) {
                return false;
            }
        }
        if (!matchesMinGarageClearance(vehicle.getDimensions(), criteria.minGarageClearanceMm(), garage)) {
            return false;
        }
        if (criteria.status() != null && vehicle.getStatus() != criteria.status()) {
            return false;
        }

        DerivedMetrics metrics = vehicle.getDerivedMetrics();
        if (criteria.minOverallScore() != null) {
            if (metrics == null || metrics.getOverallScore() == null) {
                return false;
            }
            if (metrics.getOverallScore() < criteria.minOverallScore()) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesPrice(Pricing pricing, BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null && maxPrice == null) {
            return true;
        }
        if (pricing == null || pricing.filterPrice() == null) {
            return false;
        }
        BigDecimal price = pricing.filterPrice();
        if (minPrice != null && price.compareTo(minPrice) < 0) {
            return false;
        }
        if (maxPrice != null && price.compareTo(maxPrice) > 0) {
            return false;
        }
        return true;
    }

    private static boolean matchesMinGarageClearance(
            Dimensions dimensions,
            Integer minGarageClearanceMm,
            GarageDimensions garage) {
        if (minGarageClearanceMm == null || minGarageClearanceMm <= 0) {
            return true;
        }
        Integer clearance = GarageClearanceCalculator.clearanceMm(garage, dimensions);
        return clearance != null && clearance >= minGarageClearanceMm;
    }
}
