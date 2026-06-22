package za.driver.scoring;

import static za.driver.scoring.ScoreUtil.piecewiseLinear;
import static za.driver.scoring.ScoringConstants.PERFORMANCE_ACCEL_TIME_BREAKPOINTS;
import static za.driver.scoring.ScoringConstants.PERFORMANCE_ACCEL_TIME_SCORES;
import static za.driver.scoring.ScoringConstants.PERFORMANCE_EST_ACCEL_PWR_BREAKPOINTS;
import static za.driver.scoring.ScoringConstants.PERFORMANCE_EST_ACCEL_TIME_SECONDS;
import static za.driver.scoring.ScoringConstants.PERFORMANCE_PWR_WEIGHT_BREAKPOINTS;
import static za.driver.scoring.ScoringConstants.PERFORMANCE_PWR_WEIGHT_SCORES;
import static za.driver.scoring.ScoringConstants.PERFORMANCE_TRANSMISSION_NEUTRAL_SCORE;
import static za.driver.scoring.ScoringConstants.PERFORMANCE_TRQ_WEIGHT_BREAKPOINTS;
import static za.driver.scoring.ScoringConstants.PERFORMANCE_TRQ_WEIGHT_SCORES;

import za.driver.model.TransmissionType;

final class PerformanceScoreUtil {

    private PerformanceScoreUtil() {
    }

    static Double calculatePowerToWeight(Double powerKw, Integer kerbWeightKg) {
        if (powerKw == null || kerbWeightKg == null || kerbWeightKg <= 0) {
            return null;
        }
        return powerKw / kerbWeightKg * 1000.0;
    }

    static Double calculateTorqueToWeight(Double torqueNm, Integer kerbWeightKg) {
        if (torqueNm == null || kerbWeightKg == null || kerbWeightKg <= 0) {
            return null;
        }
        return torqueNm / kerbWeightKg * 1000.0;
    }

    static double estimateZeroToHundred(double powerToWeight) {
        Double seconds = piecewiseLinear(
                powerToWeight,
                PERFORMANCE_EST_ACCEL_PWR_BREAKPOINTS,
                PERFORMANCE_EST_ACCEL_TIME_SECONDS);
        return seconds != null ? seconds : PERFORMANCE_EST_ACCEL_TIME_SECONDS[0];
    }

    static double calculateTransmissionScore(TransmissionType type) {
        if (type == null) {
            return PERFORMANCE_TRANSMISSION_NEUTRAL_SCORE;
        }
        return switch (type) {
            case DCT -> 100.0;
            case AUTOMATIC -> 80.0;
            case MANUAL -> 70.0;
            case CVT -> 60.0;
        };
    }

    static Double powerWeightScore(double powerToWeight) {
        return piecewiseLinear(
                powerToWeight,
                PERFORMANCE_PWR_WEIGHT_BREAKPOINTS,
                PERFORMANCE_PWR_WEIGHT_SCORES);
    }

    static Double torqueWeightScore(double torqueToWeight) {
        return piecewiseLinear(
                torqueToWeight,
                PERFORMANCE_TRQ_WEIGHT_BREAKPOINTS,
                PERFORMANCE_TRQ_WEIGHT_SCORES);
    }

    static Double accelerationScore(double zeroToHundredSeconds) {
        return piecewiseLinear(
                zeroToHundredSeconds,
                PERFORMANCE_ACCEL_TIME_BREAKPOINTS,
                PERFORMANCE_ACCEL_TIME_SCORES);
    }
}
