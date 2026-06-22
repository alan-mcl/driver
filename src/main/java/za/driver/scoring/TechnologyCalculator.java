package za.driver.scoring;

import static za.driver.scoring.ScoreUtil.subScore;
import static za.driver.scoring.ScoreUtil.weightedAverage;
import static za.driver.scoring.ScoringConstants.TECH_ADAPTIVE_CRUISE_WEIGHT;
import static za.driver.scoring.ScoringConstants.TECH_ANDROID_AUTO_WEIGHT;
import static za.driver.scoring.ScoringConstants.TECH_APPLE_CARPLAY_WEIGHT;
import static za.driver.scoring.ScoringConstants.TECH_DIGITAL_CLUSTER_WEIGHT;
import static za.driver.scoring.ScoringConstants.TECH_KEYLESS_ENTRY_WEIGHT;
import static za.driver.scoring.ScoringConstants.TECH_PUSH_BUTTON_START_WEIGHT;
import static za.driver.scoring.ScoringConstants.TECH_REVERSE_CAMERA_WEIGHT;
import static za.driver.scoring.ScoringConstants.TECH_WIRELESS_CHARGING_WEIGHT;

import java.util.ArrayList;
import java.util.List;

import za.driver.model.Features;
import za.driver.model.Metric;
import za.driver.model.Safety;
import za.driver.model.Vehicle;

public class TechnologyCalculator implements MetricCalculator {

    @Override
    public Metric metric() {
        return Metric.TECHNOLOGY;
    }

    @Override
    public Double calculate(Vehicle vehicle) {
        List<ScoreUtil.SubScore> subScores = new ArrayList<>();

        Features features = vehicle.getFeatures();
        if (features != null) {
            subScores.add(subScore(features.getAndroidAuto(), TECH_ANDROID_AUTO_WEIGHT));
            subScores.add(subScore(features.getAppleCarplay(), TECH_APPLE_CARPLAY_WEIGHT));
            subScores.add(subScore(features.getReverseCamera(), TECH_REVERSE_CAMERA_WEIGHT));
            subScores.add(subScore(features.getDigitalCluster(), TECH_DIGITAL_CLUSTER_WEIGHT));
            subScores.add(subScore(features.getWirelessCharging(), TECH_WIRELESS_CHARGING_WEIGHT));
            subScores.add(subScore(features.getKeylessEntry(), TECH_KEYLESS_ENTRY_WEIGHT));
            subScores.add(subScore(features.getPushButtonStart(), TECH_PUSH_BUTTON_START_WEIGHT));
        }

        Safety safety = vehicle.getSafety();
        if (safety != null) {
            subScores.add(subScore(safety.getAdaptiveCruiseControl(), TECH_ADAPTIVE_CRUISE_WEIGHT));
        }

        return weightedAverage(subScores);
    }
}
