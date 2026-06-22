package za.driver.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import za.driver.model.Safety;
import za.driver.model.Vehicle;

class ScoringInputCoverageTest {

    @Test
    void totalFieldCount_is35() {
        assertEquals(35, ScoringInputCoverage.totalFieldCount());
    }

    @Test
    void completenessPercent_fullVehicle_isHigh() {
        Vehicle vehicle = ScoringTestFixtures.fullVehicle();

        assertEquals(34, ScoringInputCoverage.populatedFieldCount(vehicle));
        assertEquals(34.0 / 35.0 * 100.0, ScoringInputCoverage.completenessPercent(vehicle), 0.01);
    }

    @Test
    void completenessPercent_partialVehicle_isLow() {
        Vehicle vehicle = ScoringTestFixtures.partialVehicle();

        assertEquals(0, ScoringInputCoverage.populatedFieldCount(vehicle));
        assertEquals(0.0, ScoringInputCoverage.completenessPercent(vehicle), 0.01);
    }

    @Test
    void completenessPercent_nullVehicle_isZero() {
        assertEquals(0.0, ScoringInputCoverage.completenessPercent(null), 0.01);
    }

    @Test
    void completenessPercent_falseBooleanStillCountsAsPopulated() {
        Vehicle vehicle = ScoringTestFixtures.fullVehicle();
        vehicle.getFeatures().setDigitalCluster(false);

        assertEquals(97.14, ScoringInputCoverage.completenessPercent(vehicle), 0.01);
    }

    @Test
    void completenessPercent_nullBooleanReducesCompleteness() {
        Vehicle vehicle = ScoringTestFixtures.fullVehicle();
        vehicle.getSafety().setBlindSpotMonitoring(null);

        double percent = ScoringInputCoverage.completenessPercent(vehicle);
        assertTrue(percent < 100.0);
        assertEquals(33.0 / 35.0 * 100.0, percent, 0.01);
    }

    @Test
    void completenessPercent_nullSafetyObject_isZeroForSafetyFields() {
        Vehicle vehicle = ScoringTestFixtures.fullVehicle();
        vehicle.setSafety(null);

        assertTrue(ScoringInputCoverage.completenessPercent(vehicle) < 100.0);
        assertEquals(25, ScoringInputCoverage.populatedFieldCount(vehicle));
    }
}
