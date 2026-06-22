package za.driver.scoring;

import static za.driver.scoring.ScoringConstants.POWERTRAIN_AUTOMATIC_ADJ;
import static za.driver.scoring.ScoringConstants.POWERTRAIN_BASE_SCORE;
import static za.driver.scoring.ScoringConstants.POWERTRAIN_CVT_ADJ;
import static za.driver.scoring.ScoringConstants.POWERTRAIN_DCT_ADJ;
import static za.driver.scoring.ScoringConstants.POWERTRAIN_EV_ADJ;
import static za.driver.scoring.ScoringConstants.POWERTRAIN_HYBRID_ADJ;
import static za.driver.scoring.ScoringConstants.POWERTRAIN_MANUAL_ADJ;
import static za.driver.scoring.ScoringConstants.POWERTRAIN_NA_DIESEL_ADJ;
import static za.driver.scoring.ScoringConstants.POWERTRAIN_NA_PETROL_ADJ;
import static za.driver.scoring.ScoringConstants.POWERTRAIN_PHEV_ADJ;
import static za.driver.scoring.ScoringConstants.POWERTRAIN_TURBO_DIESEL_ADJ;
import static za.driver.scoring.ScoringConstants.POWERTRAIN_TURBO_PETROL_ADJ;

import za.driver.model.Aspiration;
import za.driver.model.Engine;
import za.driver.model.FuelType;
import za.driver.model.Transmission;
import za.driver.model.TransmissionType;

public final class PowertrainReliabilityScorer {

    public Double score(Engine engine, Transmission transmission) {
        return scoreDetails(engine, transmission).score();
    }

    public PowertrainScoreDetails scoreDetails(Engine engine, Transmission transmission) {
        if (engine == null) {
            return PowertrainScoreDetails.unavailable();
        }
        if (!hasPowertrainInputs(engine)) {
            return PowertrainScoreDetails.unavailable();
        }

        double score = POWERTRAIN_BASE_SCORE;
        StringBuilder explanation = new StringBuilder("Base ").append(format(score));

        if (isPhev(engine)) {
            score += POWERTRAIN_PHEV_ADJ;
            explanation.append("; PHEV ").append(formatSigned(POWERTRAIN_PHEV_ADJ));
        } else if (isEv(engine)) {
            score += POWERTRAIN_EV_ADJ;
            explanation.append("; EV ").append(formatSigned(POWERTRAIN_EV_ADJ));
        } else {
            if (isHybrid(engine)) {
                score += POWERTRAIN_HYBRID_ADJ;
                explanation.append("; Hybrid ").append(formatSigned(POWERTRAIN_HYBRID_ADJ));
            }
            Double aspirationAdj = aspirationAdjustment(engine);
            if (aspirationAdj != null) {
                score += aspirationAdj;
                explanation.append("; ").append(aspirationLabel(engine)).append(' ')
                        .append(formatSigned(aspirationAdj));
            }
        }

        Double transmissionAdj = transmissionAdjustment(transmission);
        if (transmissionAdj != null) {
            score += transmissionAdj;
            explanation.append("; ").append(transmissionLabel(transmission)).append(' ')
                    .append(formatSigned(transmissionAdj));
        }

        return new PowertrainScoreDetails(ScoreUtil.clamp(score), explanation.toString());
    }

    private static boolean hasPowertrainInputs(Engine engine) {
        return engine.getFuelType() != null || engine.getAspiration() != null
                || Boolean.TRUE.equals(engine.getHybrid()) || Boolean.TRUE.equals(engine.getPhev());
    }

    private static boolean isPhev(Engine engine) {
        return engine.getFuelType() == FuelType.PHEV || Boolean.TRUE.equals(engine.getPhev());
    }

    private static boolean isEv(Engine engine) {
        return engine.getFuelType() == FuelType.EV;
    }

    private static boolean isHybrid(Engine engine) {
        if (isPhev(engine) || isEv(engine)) {
            return false;
        }
        return engine.getFuelType() == FuelType.HYBRID || Boolean.TRUE.equals(engine.getHybrid());
    }

    private static Double aspirationAdjustment(Engine engine) {
        FuelType fuelType = engine.getFuelType();
        Aspiration aspiration = engine.getAspiration();

        if (fuelType == FuelType.DIESEL) {
            if (aspiration == Aspiration.NATURALLY_ASPIRATED || aspiration == null) {
                return POWERTRAIN_NA_DIESEL_ADJ;
            }
            if (aspiration == Aspiration.TURBOCHARGED || aspiration == Aspiration.SUPERCHARGED) {
                return POWERTRAIN_TURBO_DIESEL_ADJ;
            }
        }

        if (fuelType == FuelType.PETROL || fuelType == null) {
            if (aspiration == Aspiration.NATURALLY_ASPIRATED) {
                return POWERTRAIN_NA_PETROL_ADJ;
            }
            if (aspiration == Aspiration.TURBOCHARGED || aspiration == Aspiration.SUPERCHARGED) {
                return POWERTRAIN_TURBO_PETROL_ADJ;
            }
        }

        return null;
    }

    private static String aspirationLabel(Engine engine) {
        FuelType fuelType = engine.getFuelType();
        Aspiration aspiration = engine.getAspiration();
        if (fuelType == FuelType.DIESEL) {
            if (aspiration == Aspiration.TURBOCHARGED || aspiration == Aspiration.SUPERCHARGED) {
                return "Turbo diesel";
            }
            return "NA diesel";
        }
        if (aspiration == Aspiration.TURBOCHARGED || aspiration == Aspiration.SUPERCHARGED) {
            return "Turbo petrol";
        }
        return "NA petrol";
    }

    private static Double transmissionAdjustment(Transmission transmission) {
        if (transmission == null || transmission.getType() == null) {
            return null;
        }
        return switch (transmission.getType()) {
            case MANUAL -> POWERTRAIN_MANUAL_ADJ;
            case AUTOMATIC -> POWERTRAIN_AUTOMATIC_ADJ;
            case CVT -> POWERTRAIN_CVT_ADJ;
            case DCT -> POWERTRAIN_DCT_ADJ;
        };
    }

    private static String transmissionLabel(Transmission transmission) {
        return switch (transmission.getType()) {
            case MANUAL -> "Manual";
            case AUTOMATIC -> "Automatic";
            case CVT -> "CVT";
            case DCT -> "DCT";
        };
    }

    private static String format(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private static String formatSigned(double value) {
        if (value >= 0) {
            return "+" + format(value);
        }
        return format(value);
    }

    public record PowertrainScoreDetails(Double score, String explanation) {
        static PowertrainScoreDetails unavailable() {
            return new PowertrainScoreDetails(null, null);
        }
    }
}
