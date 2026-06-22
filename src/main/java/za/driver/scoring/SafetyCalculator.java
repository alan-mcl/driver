package za.driver.scoring;

import static za.driver.scoring.ScoreUtil.subScore;
import static za.driver.scoring.ScoreUtil.weightedAverage;
import static za.driver.scoring.ScoringConstants.SAFETY_ABS_WEIGHT;
import static za.driver.scoring.ScoringConstants.SAFETY_AEB_WEIGHT;
import static za.driver.scoring.ScoringConstants.SAFETY_AIRBAGS_WEIGHT;
import static za.driver.scoring.ScoringConstants.SAFETY_BLIND_SPOT_WEIGHT;
import static za.driver.scoring.ScoringConstants.SAFETY_LANE_ASSIST_WEIGHT;
import static za.driver.scoring.ScoringConstants.SAFETY_NCAP_WEIGHT;
import static za.driver.scoring.ScoringConstants.SAFETY_REAR_CROSS_TRAFFIC_WEIGHT;
import static za.driver.scoring.ScoringConstants.SAFETY_STABILITY_WEIGHT;

import java.util.ArrayList;
import java.util.List;

import za.driver.model.Metric;
import za.driver.model.Safety;
import za.driver.model.Vehicle;

public class SafetyCalculator implements MetricCalculator {

    @Override
    public Metric metric() {
        return Metric.SAFETY;
    }

    @Override
    public Double calculate(Vehicle vehicle) {
        Safety safety = vehicle.getSafety();
        if (safety == null) {
            return null;
        }

        List<ScoreUtil.SubScore> subScores = new ArrayList<>();
        if (safety.getNcapStars() != null) {
            subScores.add(subScore(safety.getNcapStars() / 5.0 * 100.0, SAFETY_NCAP_WEIGHT));
        }
        if (safety.getAirbags() != null) {
            subScores.add(subScore(Math.min(safety.getAirbags(), 10) / 10.0 * 100.0, SAFETY_AIRBAGS_WEIGHT));
        }
        subScores.add(subScore(safety.getAbs(), SAFETY_ABS_WEIGHT));

        Double stabilityScore = null;
        if (safety.getEsp() != null || safety.getTractionControl() != null) {
            boolean hasStability = Boolean.TRUE.equals(safety.getEsp())
                    || Boolean.TRUE.equals(safety.getTractionControl());
            stabilityScore = hasStability ? 100.0 : 0.0;
        }
        subScores.add(subScore(stabilityScore, SAFETY_STABILITY_WEIGHT));

        subScores.add(subScore(safety.getAeb(), SAFETY_AEB_WEIGHT));
        subScores.add(subScore(safety.getLaneAssist(), SAFETY_LANE_ASSIST_WEIGHT));
        subScores.add(subScore(safety.getBlindSpotMonitoring(), SAFETY_BLIND_SPOT_WEIGHT));
        subScores.add(subScore(safety.getRearCrossTrafficAlert(), SAFETY_REAR_CROSS_TRAFFIC_WEIGHT));

        return weightedAverage(subScores);
    }
}
