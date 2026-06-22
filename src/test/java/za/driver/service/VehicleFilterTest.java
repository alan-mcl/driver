package za.driver.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import za.driver.model.BodyType;
import za.driver.model.DerivedMetrics;
import za.driver.model.Dimensions;
import za.driver.model.Engine;
import za.driver.model.FuelType;
import za.driver.model.GarageDimensions;
import za.driver.model.Pricing;
import za.driver.model.Vehicle;
import za.driver.model.VehicleStatus;
import za.driver.scoring.ScoringTestFixtures;

class VehicleFilterTest {

    private static final GarageDimensions GARAGE = GarageDimensions.defaults();

    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        vehicle = ScoringTestFixtures.fullVehicle();
        Pricing pricing = new Pricing();
        pricing.setPriceZar(new BigDecimal("450000"));
        vehicle.setPricing(pricing);

        DerivedMetrics metrics = new DerivedMetrics();
        metrics.setSafetyScore(85.0);
        metrics.setRunningCostScore(70.0);
        metrics.setReliabilityScore(90.0);
        metrics.setAwesomenessScore(75.0);
        metrics.setOverallScore(78.0);
        vehicle.setDerivedMetrics(metrics);
    }

    @Test
    void matches_emptyCriteria_returnsTrue() {
        assertTrue(VehicleFilter.matches(vehicle, VehicleFilterCriteria.empty(), GARAGE));
    }

    @Test
    void matches_priceRange_filtersCorrectly() {
        assertTrue(VehicleFilter.matches(
                vehicle,
                criteria(new BigDecimal("400000"), new BigDecimal("500000"), null, null),
                GARAGE));
        assertFalse(VehicleFilter.matches(
                vehicle,
                criteria(null, new BigDecimal("400000"), null, null),
                GARAGE));
        assertFalse(VehicleFilter.matches(
                vehicle,
                criteria(new BigDecimal("500000"), null, null, null),
                GARAGE));
    }

    @Test
    void matches_nullPrice_failsPriceFilter() {
        vehicle.setPricing(null);
        assertFalse(VehicleFilter.matches(
                vehicle,
                criteria(null, new BigDecimal("500000"), null, null),
                GARAGE));
    }

    @Test
    void matches_bodyType_filtersCorrectly() {
        assertTrue(VehicleFilter.matches(
                vehicle,
                criteria(null, null, BodyType.SEDAN, null),
                GARAGE));
        assertFalse(VehicleFilter.matches(
                vehicle,
                criteria(null, null, BodyType.SUV, null),
                GARAGE));
    }

    @Test
    void matches_fuelType_filtersCorrectly() {
        assertTrue(VehicleFilter.matches(
                vehicle,
                criteria(null, null, null, FuelType.PETROL),
                GARAGE));
        assertFalse(VehicleFilter.matches(
                vehicle,
                criteria(null, null, null, FuelType.DIESEL),
                GARAGE));
    }

    @Test
    void matches_minScores_filtersCorrectly() {
        assertTrue(VehicleFilter.matches(
                vehicle,
                new VehicleFilterCriteria(
                        null, null, null, null, 80.0, 60.0, 85.0, 70.0, 70.0, null, null, null, null),
                GARAGE));
        assertFalse(VehicleFilter.matches(
                vehicle,
                new VehicleFilterCriteria(
                        null, null, null, null, 90.0, null, null, null, null, null, null, null, null),
                GARAGE));
        assertFalse(VehicleFilter.matches(
                vehicle,
                new VehicleFilterCriteria(
                        null, null, null, null, null, null, null, null, 80.0, null, null, null, null),
                GARAGE));
    }

    @Test
    void matches_minAwesomeness_filtersCorrectly() {
        assertTrue(VehicleFilter.matches(
                vehicle,
                new VehicleFilterCriteria(
                        null, null, null, null, null, null, null, 75.0, null, null, null, null, null),
                GARAGE));
        assertFalse(VehicleFilter.matches(
                vehicle,
                new VehicleFilterCriteria(
                        null, null, null, null, null, null, null, 80.0, null, null, null, null, null),
                GARAGE));
        assertTrue(VehicleFilter.matches(
                vehicle,
                new VehicleFilterCriteria(
                        null, null, null, null, null, null, null, 0.0, null, null, null, null, null),
                GARAGE));
    }

    @Test
    void matches_nullEngine_failsFuelFilter() {
        vehicle.setEngine(null);
        assertFalse(VehicleFilter.matches(
                vehicle,
                criteria(null, null, null, FuelType.PETROL),
                GARAGE));
    }

    @Test
    void matches_maxDimensions_filtersCorrectly() {
        Dimensions dimensions = vehicle.getDimensions();
        dimensions.setWidthMm(1780);
        dimensions.setHeightMm(1435);

        assertTrue(VehicleFilter.matches(
                vehicle,
                new VehicleFilterCriteria(
                        null, null, null, null, null, null, null, null, null, 1800, 1500, null, null),
                GARAGE));
        assertFalse(VehicleFilter.matches(
                vehicle,
                new VehicleFilterCriteria(
                        null, null, null, null, null, null, null, null, null, 1700, null, null, null),
                GARAGE));
        assertFalse(VehicleFilter.matches(
                vehicle,
                new VehicleFilterCriteria(
                        null, null, null, null, null, null, null, null, null, null, 1400, null, null),
                GARAGE));
    }

    @Test
    void matches_maxDimensions_unknownDimension_passes() {
        vehicle.setDimensions(null);
        assertTrue(VehicleFilter.matches(
                vehicle,
                new VehicleFilterCriteria(
                        null, null, null, null, null, null, null, null, null, 1800, 1500, null, null),
                GARAGE));
        Dimensions dimensions = new Dimensions();
        dimensions.setWidthMm(1780);
        vehicle.setDimensions(dimensions);
        assertTrue(VehicleFilter.matches(
                vehicle,
                new VehicleFilterCriteria(
                        null, null, null, null, null, null, null, null, null, 1800, 1500, null, null),
                GARAGE));
    }

    @Test
    void matches_status_filtersCorrectly() {
        vehicle.setStatus(VehicleStatus.CANDIDATE);
        assertTrue(VehicleFilter.matches(
                vehicle,
                new VehicleFilterCriteria(
                        null, null, null, null, null, null, null, null, null, null, null, null, VehicleStatus.CANDIDATE),
                GARAGE));
        assertFalse(VehicleFilter.matches(
                vehicle,
                new VehicleFilterCriteria(
                        null, null, null, null, null, null, null, null, null, null, null, null, VehicleStatus.SHORTLISTED),
                GARAGE));
    }

    @Test
    void matches_minGarageClearance_filtersCorrectly() {
        // Corolla fixture clearance ≈ 505 mm
        assertTrue(VehicleFilter.matches(
                vehicle,
                new VehicleFilterCriteria(
                        null, null, null, null, null, null, null, null, null, null, null, 500, null),
                GARAGE));
        assertFalse(VehicleFilter.matches(
                vehicle,
                new VehicleFilterCriteria(
                        null, null, null, null, null, null, null, null, null, null, null, 600, null),
                GARAGE));
        assertTrue(VehicleFilter.matches(
                vehicle,
                new VehicleFilterCriteria(
                        null, null, null, null, null, null, null, null, null, null, null, 0, null),
                GARAGE));
    }

    @Test
    void matches_minGarageClearance_missingDimensions_fails() {
        vehicle.setDimensions(null);
        assertFalse(VehicleFilter.matches(
                vehicle,
                new VehicleFilterCriteria(
                        null, null, null, null, null, null, null, null, null, null, null, 100, null),
                GARAGE));
    }

    private static VehicleFilterCriteria criteria(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BodyType bodyType,
            FuelType fuelType) {
        return new VehicleFilterCriteria(
                minPrice, maxPrice, bodyType, fuelType, null, null, null, null, null, null, null, null, null);
    }
}
