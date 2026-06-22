package za.driver.scoring;

import static za.driver.scoring.ScoreUtil.linearScale;

final class CoverageScoreUtil {

    private CoverageScoreUtil() {
    }

    static Double coverageScore(Integer years, Integer km, double yearsMax, double kmMax) {
        Double yearsScore = years != null ? linearScale(years, 0.0, yearsMax) : null;
        Double kmScore = km != null ? linearScale(km, 0.0, kmMax) : null;

        if (yearsScore == null && kmScore == null) {
            return null;
        }
        if (yearsScore == null) {
            return kmScore;
        }
        if (kmScore == null) {
            return yearsScore;
        }
        return (yearsScore + kmScore) / 2.0;
    }
}
