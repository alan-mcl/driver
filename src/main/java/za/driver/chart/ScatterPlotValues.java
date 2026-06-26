package za.driver.chart;

import java.math.BigDecimal;
import java.util.OptionalDouble;

import za.driver.model.DerivedMetrics;
import za.driver.model.Metric;
import za.driver.model.Pricing;
import za.driver.model.Vehicle;
import za.driver.scoring.MetricScores;

public final class ScatterPlotValues {

    private ScatterPlotValues() {
    }

    public static OptionalDouble value(Vehicle vehicle, ScatterPlotAxis axis) {
        if (vehicle == null || axis == null) {
            return OptionalDouble.empty();
        }
        return switch (axis) {
            case PRICE -> priceValue(vehicle.getPricing());
            case OVERALL_SCORE -> scoreValue(vehicle.getDerivedMetrics(), DerivedMetrics::getOverallScore);
            case SCORE_PER_100K -> scoreValue(vehicle.getDerivedMetrics(), DerivedMetrics::getScorePer100k);
            default -> metricValue(vehicle, axis.metric());
        };
    }

    private static OptionalDouble priceValue(Pricing pricing) {
        if (pricing == null || pricing.getPriceZar() == null) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(pricing.getPriceZar().doubleValue());
    }

    private static OptionalDouble metricValue(Vehicle vehicle, Metric metric) {
        Double score = MetricScores.displayScore(vehicle, vehicle.getDerivedMetrics(), metric);
        return score == null ? OptionalDouble.empty() : OptionalDouble.of(score);
    }

    private static OptionalDouble scoreValue(DerivedMetrics metrics, java.util.function.Function<DerivedMetrics, Double> getter) {
        if (metrics == null) {
            return OptionalDouble.empty();
        }
        Double score = getter.apply(metrics);
        return score == null ? OptionalDouble.empty() : OptionalDouble.of(score);
    }
}
