package za.driver.scoring;

import static za.driver.scoring.ScoringConstants.RELIABILITY_HEURISTIC_WEIGHT;
import static za.driver.scoring.ScoringConstants.RELIABILITY_MANUAL_WEIGHT;

public final class ReliabilityScoreBlender {

    private ReliabilityScoreBlender() {
    }

    public static Double blend(Double heuristic, Double manualEstimate) {
        if (heuristic != null && manualEstimate != null) {
            return (double) Math.round(ScoreUtil.clamp(
                    heuristic * RELIABILITY_HEURISTIC_WEIGHT + manualEstimate * RELIABILITY_MANUAL_WEIGHT));
        }
        if (heuristic != null) {
            return ScoreUtil.clamp(heuristic);
        }
        if (manualEstimate != null) {
            return ScoreUtil.clamp(manualEstimate);
        }
        return null;
    }
}
