package za.driver.scoring;

import static za.driver.scoring.ScoringConstants.AWESOMENESS_COMFORT_WEIGHT;
import static za.driver.scoring.ScoringConstants.AWESOMENESS_DAILY_DRIVER_WEIGHT;
import static za.driver.scoring.ScoringConstants.AWESOMENESS_PRESTIGE_WEIGHT;
import static za.driver.scoring.ScoringConstants.AWESOMENESS_TECHNOLOGY_WEIGHT;
import static za.driver.scoring.ScoreUtil.subScore;
import static za.driver.scoring.ScoreUtil.weightedAverage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import za.driver.model.Metric;

final class AwesomenessCalculator {

    private AwesomenessCalculator() {
    }

    static Double calculate(Map<Metric, Double> scores) {
        if (scores == null) {
            return null;
        }

        List<ScoreUtil.SubScore> subScores = new ArrayList<>();
        subScores.add(subScore(scores.get(Metric.PRESTIGE), AWESOMENESS_PRESTIGE_WEIGHT));
        subScores.add(subScore(scores.get(Metric.COMFORT), AWESOMENESS_COMFORT_WEIGHT));
        subScores.add(subScore(scores.get(Metric.DAILY_DRIVER), AWESOMENESS_DAILY_DRIVER_WEIGHT));
        subScores.add(subScore(scores.get(Metric.TECHNOLOGY), AWESOMENESS_TECHNOLOGY_WEIGHT));
        return weightedAverage(subScores);
    }
}
