package za.driver.garage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import za.driver.model.Dimensions;
import za.driver.model.GarageDimensions;
import za.driver.scoring.ScoringTestFixtures;

class GarageClearanceCalculatorTest {

    private static final GarageDimensions DEFAULT_GARAGE = GarageDimensions.defaults();

    @Test
    void openingWidthAtHeight_belowSpringLine_returnsFullWidth() {
        assertEquals(2370, GarageClearanceCalculator.openingWidthAtHeightMm(1120, DEFAULT_GARAGE));
        assertEquals(2370, GarageClearanceCalculator.openingWidthAtHeightMm(500, DEFAULT_GARAGE));
    }

    @Test
    void openingWidthAtHeight_onArch_reducesWidth() {
        int opening = GarageClearanceCalculator.openingWidthAtHeightMm(1435, DEFAULT_GARAGE);
        assertEquals(2285, opening);
    }

    @Test
    void openingWidthAtHeight_atApex_returnsZero() {
        assertEquals(0, GarageClearanceCalculator.openingWidthAtHeightMm(2305, DEFAULT_GARAGE));
    }

    @Test
    void openingWidthAtHeight_aboveApex_returnsNull() {
        assertNull(GarageClearanceCalculator.openingWidthAtHeightMm(2306, DEFAULT_GARAGE));
    }

    @Test
    void clearanceMm_corollaFixture_matchesExpected() {
        Dimensions dimensions = ScoringTestFixtures.fullVehicle().getDimensions();
        assertEquals(505, GarageClearanceCalculator.clearanceMm(DEFAULT_GARAGE, dimensions));
    }

    @Test
    void clearanceMm_belowSpringLine_usesFullWidth() {
        Dimensions dimensions = new Dimensions();
        dimensions.setWidthMm(1800);
        dimensions.setHeightMm(1000);
        assertEquals(570, GarageClearanceCalculator.clearanceMm(DEFAULT_GARAGE, dimensions));
    }

    @Test
    void clearanceMm_missingDimensions_returnsNull() {
        assertNull(GarageClearanceCalculator.clearanceMm(DEFAULT_GARAGE, null));
        assertNull(GarageClearanceCalculator.clearanceMm(DEFAULT_GARAGE, new Dimensions()));
        Dimensions partial = new Dimensions();
        partial.setWidthMm(1800);
        assertNull(GarageClearanceCalculator.clearanceMm(DEFAULT_GARAGE, partial));
    }

    @Test
    void clearanceMm_tooTall_returnsNull() {
        Dimensions dimensions = new Dimensions();
        dimensions.setWidthMm(1800);
        dimensions.setHeightMm(2400);
        assertNull(GarageClearanceCalculator.clearanceMm(DEFAULT_GARAGE, dimensions));
    }

    @Test
    void clearanceMm_tooWide_returnsNegative() {
        Dimensions dimensions = new Dimensions();
        dimensions.setWidthMm(2400);
        dimensions.setHeightMm(1435);
        assertEquals(-115, GarageClearanceCalculator.clearanceMm(DEFAULT_GARAGE, dimensions));
    }
}
