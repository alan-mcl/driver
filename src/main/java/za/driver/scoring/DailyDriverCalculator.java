package za.driver.scoring;

import static za.driver.scoring.ScoreUtil.inverseScale;
import static za.driver.scoring.ScoreUtil.subScore;
import static za.driver.scoring.ScoreUtil.weightedAverage;

import java.util.ArrayList;
import java.util.List;

import za.driver.model.Dimensions;
import za.driver.model.Economy;
import za.driver.model.Features;
import za.driver.model.Metric;
import za.driver.model.Vehicle;

import static za.driver.scoring.ScoringConstants.DAILY_DRIVER_FUEL_WEIGHT;
import static za.driver.scoring.ScoringConstants.DAILY_DRIVER_LENGTH_WEIGHT;
import static za.driver.scoring.ScoringConstants.DAILY_DRIVER_PARKING_AIDS_WEIGHT;
import static za.driver.scoring.ScoringConstants.DAILY_DRIVER_TURNING_CIRCLE_WEIGHT;
import static za.driver.scoring.ScoringConstants.FUEL_MAX;
import static za.driver.scoring.ScoringConstants.FUEL_MIN;
import static za.driver.scoring.ScoringConstants.LENGTH_MAX;
import static za.driver.scoring.ScoringConstants.LENGTH_MIN;
import static za.driver.scoring.ScoringConstants.TURNING_CIRCLE_MAX;
import static za.driver.scoring.ScoringConstants.TURNING_CIRCLE_MIN;

public class DailyDriverCalculator implements MetricCalculator {

    @Override
    public Metric metric() {
        return Metric.DAILY_DRIVER;
    }

    @Override
    public Double calculate(Vehicle vehicle) {
        List<ScoreUtil.SubScore> subScores = new ArrayList<>();

        Dimensions dimensions = vehicle.getDimensions();
        if (dimensions != null) {
            if (dimensions.getTurningCircleM() != null) {
                subScores.add(subScore(
                        inverseScale(dimensions.getTurningCircleM(), TURNING_CIRCLE_MIN, TURNING_CIRCLE_MAX),
                        DAILY_DRIVER_TURNING_CIRCLE_WEIGHT));
            }
            if (dimensions.getLengthMm() != null) {
                subScores.add(subScore(
                        inverseScale(dimensions.getLengthMm(), LENGTH_MIN, LENGTH_MAX),
                        DAILY_DRIVER_LENGTH_WEIGHT));
            }
        }

        Economy economy = vehicle.getEconomy();
        if (economy != null && economy.getFuelConsumptionCombined() != null) {
            subScores.add(subScore(
                    inverseScale(economy.getFuelConsumptionCombined(), FUEL_MIN, FUEL_MAX),
                    DAILY_DRIVER_FUEL_WEIGHT));
        }

        Features features = vehicle.getFeatures();
        if (features != null) {
            Double parkingAids = averageParkingAids(features);
            subScores.add(subScore(parkingAids, DAILY_DRIVER_PARKING_AIDS_WEIGHT));
        }

        return weightedAverage(subScores);
    }

    private static Double averageParkingAids(Features features) {
        int count = 0;
        double total = 0.0;

        if (features.getParkingSensorsFront() != null) {
            count++;
            total += Boolean.TRUE.equals(features.getParkingSensorsFront()) ? 100.0 : 0.0;
        }
        if (features.getParkingSensorsRear() != null) {
            count++;
            total += Boolean.TRUE.equals(features.getParkingSensorsRear()) ? 100.0 : 0.0;
        }
        if (features.getReverseCamera() != null) {
            count++;
            total += Boolean.TRUE.equals(features.getReverseCamera()) ? 100.0 : 0.0;
        }

        if (count == 0) {
            return null;
        }
        return total / count;
    }
}
