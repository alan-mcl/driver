package za.driver.scoring;

import static za.driver.scoring.PerformanceScoreUtil.accelerationScore;
import static za.driver.scoring.PerformanceScoreUtil.calculatePowerToWeight;
import static za.driver.scoring.PerformanceScoreUtil.calculateTorqueToWeight;
import static za.driver.scoring.PerformanceScoreUtil.calculateTransmissionScore;
import static za.driver.scoring.PerformanceScoreUtil.estimateZeroToHundred;
import static za.driver.scoring.PerformanceScoreUtil.powerWeightScore;
import static za.driver.scoring.PerformanceScoreUtil.torqueWeightScore;
import static za.driver.scoring.ScoreUtil.clamp;
import static za.driver.scoring.ScoringConstants.PERFORMANCE_ACCELERATION_WEIGHT;
import static za.driver.scoring.ScoringConstants.PERFORMANCE_POWER_TO_WEIGHT_WEIGHT;
import static za.driver.scoring.ScoringConstants.PERFORMANCE_TORQUE_TO_WEIGHT_WEIGHT;
import static za.driver.scoring.ScoringConstants.PERFORMANCE_TRANSMISSION_WEIGHT;

import za.driver.model.Dimensions;
import za.driver.model.Engine;
import za.driver.model.Metric;
import za.driver.model.Performance;
import za.driver.model.Transmission;
import za.driver.model.TransmissionType;
import za.driver.model.Vehicle;

public class PerformanceCalculator implements MetricCalculator {

    @Override
    public Metric metric() {
        return Metric.PERFORMANCE;
    }

    @Override
    public Double calculate(Vehicle vehicle) {
        Engine engine = vehicle.getEngine();
        Dimensions dimensions = vehicle.getDimensions();

        if (engine == null || engine.getPowerKw() == null || engine.getTorqueNm() == null) {
            return null;
        }

        Integer kerbWeightKg = dimensions != null ? dimensions.getKerbWeightKg() : null;
        if (kerbWeightKg == null || kerbWeightKg <= 0) {
            return null;
        }

        Double powerToWeight = calculatePowerToWeight(engine.getPowerKw(), kerbWeightKg);
        Double torqueToWeight = calculateTorqueToWeight(engine.getTorqueNm(), kerbWeightKg);
        if (powerToWeight == null || torqueToWeight == null) {
            return null;
        }

        double zeroToHundredSeconds = resolveZeroToHundred(vehicle, powerToWeight);

        Double accelerationComponent = accelerationScore(zeroToHundredSeconds);
        Double powerWeightComponent = powerWeightScore(powerToWeight);
        Double torqueWeightComponent = torqueWeightScore(torqueToWeight);
        if (accelerationComponent == null || powerWeightComponent == null || torqueWeightComponent == null) {
            return null;
        }

        TransmissionType transmissionType = vehicle.getTransmission() != null
                ? vehicle.getTransmission().getType()
                : null;
        double transmissionComponent = calculateTransmissionScore(transmissionType);

        double weightedSum = accelerationComponent * PERFORMANCE_ACCELERATION_WEIGHT
                + powerWeightComponent * PERFORMANCE_POWER_TO_WEIGHT_WEIGHT
                + torqueWeightComponent * PERFORMANCE_TORQUE_TO_WEIGHT_WEIGHT
                + transmissionComponent * PERFORMANCE_TRANSMISSION_WEIGHT;

        return clamp(weightedSum / 100.0);
    }

    private static double resolveZeroToHundred(Vehicle vehicle, double powerToWeight) {
        Performance performance = vehicle.getPerformance();
        if (performance != null && performance.getZeroToHundredSeconds() != null) {
            return performance.getZeroToHundredSeconds();
        }
        return estimateZeroToHundred(powerToWeight);
    }
}
