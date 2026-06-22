package za.driver.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import za.driver.model.Aspiration;
import za.driver.model.Engine;
import za.driver.model.FuelType;
import za.driver.model.Ownership;
import za.driver.model.Transmission;
import za.driver.model.TransmissionType;
import za.driver.model.Vehicle;
import za.driver.scoring.ScoringTestFixtures;

class ReliabilityCalculatorTest {

    private final ReliabilityCalculator calculator = new ReliabilityCalculator();

    @Test
    void breakdown_toyotaFixture_computesRoundedScore() {
        Vehicle vehicle = ScoringTestFixtures.fullVehicle();

        ReliabilityCalculator.ReliabilityBreakdown breakdown = calculator.breakdown(vehicle);

        assertEquals(91.0, breakdown.score());
        assertEquals(95, breakdown.brandScore());
        assertEquals(80.0, breakdown.powertrainScore());
        assertEquals(90, breakdown.partsSupportScore());
    }

    @Test
    void breakdown_missingPartsSupport_returnsNullScore() {
        Vehicle vehicle = ScoringTestFixtures.fullVehicle();
        vehicle.getOwnership().setPartsSupportScore(null);

        assertNull(calculator.calculate(vehicle));
    }

    @Test
    void breakdown_unknownBrand_returnsNullScore() {
        Vehicle vehicle = ScoringTestFixtures.fullVehicle();
        vehicle.setMake("UnknownBrand");

        assertNull(calculator.calculate(vehicle));
    }

    @Test
    void confidenceScore_toyota_returnsBrandConfidence() {
        assertEquals(98, calculator.confidenceScore(ScoringTestFixtures.fullVehicle()));
    }

    @Test
    void breakdown_hiluxStyleNaManual_computesExpectedScore() {
        Vehicle vehicle = ScoringTestFixtures.fullVehicle();
        vehicle.getEngine().setAspiration(Aspiration.NATURALLY_ASPIRATED);
        vehicle.getTransmission().setType(TransmissionType.MANUAL);
        vehicle.getOwnership().setPartsSupportScore(100);

        ReliabilityCalculator.ReliabilityBreakdown breakdown = calculator.breakdown(vehicle);

        assertEquals(96.0, breakdown.score());
        assertEquals(90.0, breakdown.powertrainScore());
    }
}
