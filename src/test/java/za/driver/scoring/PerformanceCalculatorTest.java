package za.driver.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import za.driver.model.Dimensions;
import za.driver.model.Engine;
import za.driver.model.Metric;
import za.driver.model.Performance;
import za.driver.model.Transmission;
import za.driver.model.TransmissionType;
import za.driver.model.Vehicle;
import za.driver.model.VehicleStatus;

class PerformanceCalculatorTest {

    private final PerformanceCalculator calculator = new PerformanceCalculator();

    @Test
    void metric_returnsPerformance() {
        assertEquals(Metric.PERFORMANCE, calculator.metric());
    }

    @Test
    void calculate_slowEconomyHatchback_returnsLowScore() {
        Double score = calculator.calculate(poloVivoLike());

        assertNotNull(score);
        assertEquals(15.1, score, 0.5);
    }

    @Test
    void calculate_averageCrossover_returnsMidScore() {
        Double score = calculator.calculate(corollaCrossLike());

        assertNotNull(score);
        assertEquals(45.5, score, 0.5);
    }

    @Test
    void calculate_warmHatch_returnsHighScore() {
        Double score = calculator.calculate(warmHatch());

        assertNotNull(score);
        assertEquals(84.1, score, 0.5);
    }

    @Test
    void calculate_performanceSedan_returnsVeryHighScore() {
        Double score = calculator.calculate(performanceSedan());

        assertNotNull(score);
        assertEquals(98.0, score, 0.5);
    }

    @Test
    void calculate_missingAcceleration_usesEstimate() {
        Vehicle estimated = corollaCrossLike();
        Vehicle measured = corollaCrossLike();
        measured.setPerformance(performanceWithZeroToHundred(
                PerformanceScoreUtil.estimateZeroToHundred(77.74)));

        assertEquals(calculator.calculate(estimated), calculator.calculate(measured), 0.01);
    }

    @Test
    void calculate_missingKerbWeight_returnsNull() {
        Vehicle vehicle = corollaCrossLike();
        vehicle.getDimensions().setKerbWeightKg(null);

        assertNull(calculator.calculate(vehicle));
    }

    @Test
    void calculate_zeroKerbWeight_returnsNull() {
        Vehicle vehicle = corollaCrossLike();
        vehicle.getDimensions().setKerbWeightKg(0);

        assertNull(calculator.calculate(vehicle));
    }

    @Test
    void calculate_missingPower_returnsNull() {
        Vehicle vehicle = corollaCrossLike();
        vehicle.getEngine().setPowerKw(null);

        assertNull(calculator.calculate(vehicle));
    }

    @Test
    void calculate_missingTorque_returnsNull() {
        Vehicle vehicle = corollaCrossLike();
        vehicle.getEngine().setTorqueNm(null);

        assertNull(calculator.calculate(vehicle));
    }

    @Test
    void calculate_missingTransmission_usesNeutralScore() {
        Vehicle withTransmission = corollaCrossLike();
        Vehicle withoutTransmission = corollaCrossLike();
        withoutTransmission.setTransmission(null);

        Double withScore = calculator.calculate(withTransmission);
        Double withoutScore = calculator.calculate(withoutTransmission);

        assertNotNull(withScore);
        assertNotNull(withoutScore);
        assertEquals(1.0, withoutScore - withScore, 0.5);
    }

    @Test
    void calculate_fullFixture_matchesExpectedScore() {
        Double score = calculator.calculate(ScoringTestFixtures.fullVehicle());

        assertNotNull(score);
        assertEquals(47.7, score, 0.5);
    }

    private static Vehicle poloVivoLike() {
        return vehicle(55.0, 130.0, 1063, TransmissionType.MANUAL, null);
    }

    private static Vehicle corollaCrossLike() {
        return vehicle(103.0, 172.0, 1325, TransmissionType.CVT, null);
    }

    private static Vehicle warmHatch() {
        return vehicle(150.0, 300.0, 1400, TransmissionType.DCT, 7.5);
    }

    private static Vehicle performanceSedan() {
        return vehicle(200.0, 400.0, 1600, TransmissionType.AUTOMATIC, 5.8);
    }

    private static Vehicle vehicle(
            double powerKw,
            double torqueNm,
            int kerbWeightKg,
            TransmissionType transmissionType,
            Double zeroToHundred) {
        Vehicle vehicle = new Vehicle();
        vehicle.setMake("Test");
        vehicle.setModel("Test");
        vehicle.setStatus(VehicleStatus.CANDIDATE);

        Engine engine = new Engine();
        engine.setPowerKw(powerKw);
        engine.setTorqueNm(torqueNm);
        vehicle.setEngine(engine);

        Dimensions dimensions = new Dimensions();
        dimensions.setKerbWeightKg(kerbWeightKg);
        vehicle.setDimensions(dimensions);

        Transmission transmission = new Transmission();
        transmission.setType(transmissionType);
        vehicle.setTransmission(transmission);

        if (zeroToHundred != null) {
            vehicle.setPerformance(performanceWithZeroToHundred(zeroToHundred));
        }

        return vehicle;
    }

    private static Performance performanceWithZeroToHundred(double zeroToHundred) {
        Performance performance = new Performance();
        performance.setZeroToHundredSeconds(zeroToHundred);
        return performance;
    }
}
