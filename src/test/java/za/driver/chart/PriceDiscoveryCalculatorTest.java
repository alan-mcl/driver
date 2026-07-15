package za.driver.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PriceDiscoveryCalculatorTest {

    @Test
    void scorePer100kAtPrice_computesFromOverallScore() {
        assertEquals(20.0, PriceDiscoveryCalculator.scorePer100kAtPrice(70.0, 350_000.0));
    }

    @Test
    void scorePer100kAtPrice_returnsNullForInvalidInputs() {
        assertNull(PriceDiscoveryCalculator.scorePer100kAtPrice(null, 350_000.0));
        assertNull(PriceDiscoveryCalculator.scorePer100kAtPrice(70.0, 0.0));
        assertNull(PriceDiscoveryCalculator.scorePer100kAtPrice(70.0, -1.0));
    }

    @Test
    void crossoverPrice_matchesPlanExample() {
        Double crossover = PriceDiscoveryCalculator.crossoverPrice(70.0, 350_000.0, 65.0);
        assertEquals(376_923.08, crossover, 0.01);
    }

    @Test
    void crossoverPrice_returnsNullForInvalidInputs() {
        assertNull(PriceDiscoveryCalculator.crossoverPrice(null, 350_000.0, 65.0));
        assertNull(PriceDiscoveryCalculator.crossoverPrice(70.0, 350_000.0, null));
        assertNull(PriceDiscoveryCalculator.crossoverPrice(70.0, 0.0, 65.0));
        assertNull(PriceDiscoveryCalculator.crossoverPrice(70.0, 350_000.0, 0.0));
    }

    @Test
    void beatsAtList_whenCrossoverAtOrAboveListPrice() {
        assertTrue(PriceDiscoveryCalculator.beatsAtList(450_000.0, 450_000.0));
        assertTrue(PriceDiscoveryCalculator.beatsAtList(460_000.0, 450_000.0));
        assertFalse(PriceDiscoveryCalculator.beatsAtList(400_000.0, 450_000.0));
    }

    @Test
    void discountHelpers_computeZarAndPercent() {
        assertEquals(45_000.0, PriceDiscoveryCalculator.discountZar(450_000.0, 405_000.0));
        assertEquals(10.0, PriceDiscoveryCalculator.discountPct(450_000.0, 405_000.0), 0.001);
    }

    @Test
    void sampleCurve_isMonotonicDecreasingInPrice() {
        var points = PriceDiscoveryCalculator.sampleCurve(70.0, 300_000.0, 500_000.0, 5);
        assertEquals(5, points.size());
        for (int i = 1; i < points.size(); i++) {
            assertTrue(points.get(i - 1).scorePer100k() > points.get(i).scorePer100k());
            assertTrue(points.get(i - 1).price() < points.get(i).price());
        }
    }

    @Test
    void sampleCurve_returnsEmptyForInvalidRange() {
        assertTrue(PriceDiscoveryCalculator.sampleCurve(70.0, 500_000.0, 300_000.0, 5).isEmpty());
        assertTrue(PriceDiscoveryCalculator.sampleCurve(null, 300_000.0, 500_000.0, 5).isEmpty());
    }

    @Test
    void paddedRange_addsPadding() {
        double[] range = PriceDiscoveryCalculator.paddedRange(100.0, 200.0);
        assertEquals(95.0, range[0], 0.001);
        assertEquals(205.0, range[1], 0.001);
    }
}
