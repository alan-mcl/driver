package za.driver.service;

import java.math.BigDecimal;

import za.driver.model.VehicleFilterPreferences;
import za.driver.model.VehicleListPreferences;
import za.driver.model.VehicleSortPreferences;

public final class VehicleListPreferencesMapper {

    private static final int MIN_PRICE_ZAR = 10_000;
    private static final int DEFAULT_MAX_PRICE_ZAR = 2_000_000;

    private VehicleListPreferencesMapper() {
    }

    public static VehicleFilterCriteria toCriteria(VehicleFilterPreferences prefs) {
        if (prefs == null) {
            return VehicleFilterCriteria.empty();
        }
        return new VehicleFilterCriteria(
                null,
                prefs.getMaxPrice(),
                prefs.getBodyType(),
                prefs.getFuelType(),
                prefs.getMinOverallScore(),
                prefs.getMinGarageClearanceMm(),
                prefs.getStatus());
    }

    public static VehicleFilterPreferences fromCriteria(VehicleFilterCriteria criteria) {
        VehicleFilterPreferences prefs = new VehicleFilterPreferences();
        if (criteria == null) {
            return prefs;
        }
        prefs.setMaxPrice(criteria.maxPrice());
        prefs.setBodyType(criteria.bodyType());
        prefs.setFuelType(criteria.fuelType());
        prefs.setMinOverallScore(criteria.minOverallScore());
        prefs.setMinGarageClearanceMm(criteria.minGarageClearanceMm());
        prefs.setStatus(criteria.status());
        return prefs;
    }

    public static VehicleFilterPreferences clampFilter(
            VehicleFilterPreferences prefs,
            int fleetMaxPriceZar) {
        VehicleFilterPreferences clamped = new VehicleFilterPreferences();
        if (prefs == null) {
            return clamped;
        }
        int sliderMax = Math.max(DEFAULT_MAX_PRICE_ZAR, fleetMaxPriceZar);
        BigDecimal maxPrice = prefs.getMaxPrice();
        if (maxPrice != null) {
            int value = maxPrice.intValue();
            value = Math.max(MIN_PRICE_ZAR, Math.min(sliderMax, value));
            clamped.setMaxPrice(BigDecimal.valueOf(value));
        }
        clamped.setBodyType(prefs.getBodyType());
        clamped.setFuelType(prefs.getFuelType());
        clamped.setStatus(prefs.getStatus());
        if (prefs.getMinOverallScore() != null && prefs.getMinOverallScore() > 0) {
            clamped.setMinOverallScore(prefs.getMinOverallScore());
        }
        if (prefs.getMinGarageClearanceMm() != null && prefs.getMinGarageClearanceMm() > 0) {
            clamped.setMinGarageClearanceMm(prefs.getMinGarageClearanceMm());
        }
        return clamped;
    }

    public static VehicleListPreferences defaults() {
        return new VehicleListPreferences();
    }
}
