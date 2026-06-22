package za.driver.chart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ScatterPlotStatistics {

    private ScatterPlotStatistics() {
    }

    public record LinearFit(double slope, double intercept) {

        public double yAt(double x) {
            return slope * x + intercept;
        }
    }

    public static double median(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return Double.NaN;
        }
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int count = sorted.size();
        if (count % 2 == 1) {
            return sorted.get(count / 2);
        }
        return (sorted.get(count / 2 - 1) + sorted.get(count / 2)) / 2.0;
    }

    public static Optional<LinearFit> linearFit(List<ScatterPlotPoint> points) {
        if (points == null || points.size() < 2) {
            return Optional.empty();
        }
        double sumX = 0;
        double sumY = 0;
        double sumXy = 0;
        double sumXx = 0;
        int count = points.size();
        for (ScatterPlotPoint point : points) {
            sumX += point.x();
            sumY += point.y();
            sumXy += point.x() * point.y();
            sumXx += point.x() * point.x();
        }
        double denominator = count * sumXx - sumX * sumX;
        if (denominator == 0) {
            return Optional.empty();
        }
        double slope = (count * sumXy - sumX * sumY) / denominator;
        double intercept = (sumY - slope * sumX) / count;
        return Optional.of(new LinearFit(slope, intercept));
    }
}
