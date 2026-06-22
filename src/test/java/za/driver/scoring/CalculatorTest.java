package za.driver.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import za.driver.model.Vehicle;

class CalculatorTest {

    private final Vehicle fullVehicle = ScoringTestFixtures.fullVehicle();
    private final Vehicle partialVehicle = ScoringTestFixtures.partialVehicle();

    @Test
    void safetyCalculator_fullVehicle_returnsScoreInRange() {
        assertScoreInRange(new SafetyCalculator().calculate(fullVehicle));
    }

    @Test
    void runningCostCalculator_fullVehicle_returnsScoreInRange() {
        assertScoreInRange(new RunningCostCalculator().calculate(fullVehicle));
    }

    @Test
    void comfortCalculator_fullVehicle_returnsScoreInRange() {
        assertScoreInRange(new ComfortCalculator().calculate(fullVehicle));
    }

    @Test
    void performanceCalculator_fullVehicle_returnsScoreInRange() {
        assertScoreInRange(new PerformanceCalculator().calculate(fullVehicle));
    }

    @Test
    void dailyDriverCalculator_fullVehicle_returnsScoreInRange() {
        assertScoreInRange(new DailyDriverCalculator().calculate(fullVehicle));
    }

    @Test
    void technologyCalculator_fullVehicle_returnsScoreInRange() {
        assertScoreInRange(new TechnologyCalculator().calculate(fullVehicle));
    }

    @Test
    void reliabilityCalculator_withoutOverride_returnsComputedScore() {
        assertEquals(91.0, new ReliabilityCalculator().calculate(ScoringTestFixtures.fullVehicle()));
    }

    @Test
    void prestigeCalculator_withoutOverride_returnsNull() {
        assertNull(new PrestigeCalculator().calculate(fullVehicle));
    }

    @Test
    void safetyCalculator_partialVehicle_returnsNull() {
        assertNull(new SafetyCalculator().calculate(partialVehicle));
    }

    private static void assertScoreInRange(Double score) {
        assertNotNull(score);
        assertTrue(score >= 0.0 && score <= 100.0, "Score out of range: " + score);
    }
}
