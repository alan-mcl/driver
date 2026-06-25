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
import za.driver.model.ScoringWeight;

final class AwesomenessCalculator {

    private AwesomenessCalculator() {
    }

    static Double calculate(Map<Metric, Double> scores) {
        return calculate(scores, null);
    }

    static Double calculate(Map<Metric, Double> scores, List<ScoringWeight> aggregateComponents) {
        if (scores == null) {
            return null;
        }

        List<ScoreUtil.SubScore> subScores = buildSubScores(scores, aggregateComponents);
        return weightedAverage(subScores);
    }

    private static List<ScoreUtil.SubScore> buildSubScores(
            Map<Metric, Double> scores,
            List<ScoringWeight> aggregateComponents) {
        if (aggregateComponents != null && !aggregateComponents.isEmpty()) {
            List<ScoreUtil.SubScore> subScores = new ArrayList<>();
            for (ScoringWeight scoringWeight : aggregateComponents) {
                if (scoringWeight.getMetric() == null || scoringWeight.getWeight() == null) {
                    continue;
                }
                subScores.add(subScore(scores.get(scoringWeight.getMetric()), scoringWeight.getWeight()));
            }
            return subScores;
        }

        List<ScoreUtil.SubScore> subScores = new ArrayList<>();
        subScores.add(subScore(scores.get(Metric.PRESTIGE), AWESOMENESS_PRESTIGE_WEIGHT));
        subScores.add(subScore(scores.get(Metric.COMFORT), AWESOMENESS_COMFORT_WEIGHT));
        subScores.add(subScore(scores.get(Metric.DAILY_DRIVER), AWESOMENESS_DAILY_DRIVER_WEIGHT));
        subScores.add(subScore(scores.get(Metric.TECHNOLOGY), AWESOMENESS_TECHNOLOGY_WEIGHT));
        return subScores;
    }
}
