package za.driver.scoring;

import static za.driver.scoring.ScoreUtil.linearScale;
import static za.driver.scoring.ScoreUtil.subScore;
import static za.driver.scoring.ScoreUtil.weightedAverage;
import static za.driver.scoring.ScoringConstants.COMFORT_CLIMATE_WEIGHT;
import static za.driver.scoring.ScoringConstants.COMFORT_ELECTRIC_SEATS_WEIGHT;
import static za.driver.scoring.ScoringConstants.COMFORT_HEATED_SEATS_WEIGHT;
import static za.driver.scoring.ScoringConstants.COMFORT_SEATS_WEIGHT;
import static za.driver.scoring.ScoringConstants.COMFORT_WHEELBASE_WEIGHT;
import static za.driver.scoring.ScoringConstants.SEATS_MAX;
import static za.driver.scoring.ScoringConstants.SEATS_MIN;
import static za.driver.scoring.ScoringConstants.WHEELBASE_MAX;
import static za.driver.scoring.ScoringConstants.WHEELBASE_MIN;

import java.util.ArrayList;
import java.util.List;

import za.driver.model.Dimensions;
import za.driver.model.Features;
import za.driver.model.Metric;
import za.driver.model.Vehicle;

public class ComfortCalculator implements MetricCalculator {

    @Override
    public Metric metric() {
        return Metric.COMFORT;
    }

    @Override
    public Double calculate(Vehicle vehicle) {
        List<ScoreUtil.SubScore> subScores = new ArrayList<>();

        Features features = vehicle.getFeatures();
        if (features != null) {
            subScores.add(subScore(features.getClimateControl(), COMFORT_CLIMATE_WEIGHT));
            subScores.add(subScore(features.getHeatedSeats(), COMFORT_HEATED_SEATS_WEIGHT));
            subScores.add(subScore(features.getElectricSeats(), COMFORT_ELECTRIC_SEATS_WEIGHT));
        }

        Dimensions dimensions = vehicle.getDimensions();
        if (dimensions != null) {
            if (dimensions.getSeats() != null) {
                subScores.add(subScore(linearScale(dimensions.getSeats(), SEATS_MIN, SEATS_MAX), COMFORT_SEATS_WEIGHT));
            }
            if (dimensions.getWheelbaseMm() != null) {
                subScores.add(subScore(
                        linearScale(dimensions.getWheelbaseMm(), WHEELBASE_MIN, WHEELBASE_MAX),
                        COMFORT_WHEELBASE_WEIGHT));
            }
        }

        return weightedAverage(subScores);
    }
}
