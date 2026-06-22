package za.driver.scoring;

import java.util.List;

final class ScoreUtil {

    private ScoreUtil() {
    }

    record SubScore(Double score, double weight) {
    }

    static double clamp(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }

    static Double linearScale(double value, double min, double max) {
        if (max <= min) {
            return null;
        }
        double scaled = (value - min) / (max - min) * 100.0;
        return clamp(scaled);
    }

    static Double inverseScale(double value, double min, double max) {
        if (max <= min) {
            return null;
        }
        double scaled = (max - value) / (max - min) * 100.0;
        return clamp(scaled);
    }

    static Double piecewiseLinear(double value, double[] breakpoints, double[] scores) {
        if (breakpoints == null || scores == null || breakpoints.length == 0
                || breakpoints.length != scores.length) {
            return null;
        }
        if (value <= breakpoints[0]) {
            return clamp(scores[0]);
        }
        int last = breakpoints.length - 1;
        if (value >= breakpoints[last]) {
            return clamp(scores[last]);
        }
        for (int i = 0; i < last; i++) {
            double low = breakpoints[i];
            double high = breakpoints[i + 1];
            if (value >= low && value <= high) {
                double fraction = (value - low) / (high - low);
                double interpolated = scores[i] + fraction * (scores[i + 1] - scores[i]);
                return clamp(interpolated);
            }
        }
        return clamp(scores[last]);
    }

    static Double booleanScore(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? 100.0 : 0.0;
    }

    static Double weightedAverage(List<SubScore> subScores) {
        double totalWeight = 0.0;
        double weightedSum = 0.0;

        for (SubScore subScore : subScores) {
            if (subScore.score() == null) {
                continue;
            }
            weightedSum += subScore.score() * subScore.weight();
            totalWeight += subScore.weight();
        }

        if (totalWeight == 0.0) {
            return null;
        }

        return clamp(weightedSum / totalWeight);
    }

    static SubScore subScore(Double score, double weight) {
        return new SubScore(score, weight);
    }

    static SubScore subScore(Boolean value, double weight) {
        return new SubScore(booleanScore(value), weight);
    }
}
