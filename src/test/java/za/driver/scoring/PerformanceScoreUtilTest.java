package za.driver.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import za.driver.model.TransmissionType;

class PerformanceScoreUtilTest {

    @Test
    void calculatePowerToWeight_validInputs_returnsRatio() {
        assertEquals(79.23, PerformanceScoreUtil.calculatePowerToWeight(103.0, 1300), 0.01);
    }

    @Test
    void calculatePowerToWeight_missingWeight_returnsNull() {
        assertNull(PerformanceScoreUtil.calculatePowerToWeight(103.0, null));
        assertNull(PerformanceScoreUtil.calculatePowerToWeight(103.0, 0));
    }

    @Test
    void estimateZeroToHundred_at80kWPerTonne_returns10Seconds() {
        assertEquals(10.0, PerformanceScoreUtil.estimateZeroToHundred(80.0), 0.01);
    }

    @Test
    void calculateTransmissionScore_dct_returns100() {
        assertEquals(100.0, PerformanceScoreUtil.calculateTransmissionScore(TransmissionType.DCT));
    }

    @Test
    void calculateTransmissionScore_null_returnsNeutral70() {
        assertEquals(70.0, PerformanceScoreUtil.calculateTransmissionScore(null));
    }

    @Test
    void accelerationScore_fastTime_returnsHighScore() {
        assertEquals(100.0, PerformanceScoreUtil.accelerationScore(5.5), 0.01);
    }

    @Test
    void powerWeightScore_midRange_interpolates() {
        assertEquals(50.0, PerformanceScoreUtil.powerWeightScore(80.0), 0.01);
    }
}
