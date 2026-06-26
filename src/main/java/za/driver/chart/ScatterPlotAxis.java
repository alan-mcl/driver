package za.driver.chart;

import java.util.ArrayList;
import java.util.List;

import za.driver.model.Metric;
import za.driver.model.ScoringProfile;
import za.driver.presentation.MetricLabels;
import za.driver.scoring.ProfileScoreMetrics;

public enum ScatterPlotAxis {
    PRICE("Price", null),
    OVERALL_SCORE("Overall Score", null),
    SAFETY("Safety", Metric.SAFETY),
    RUNNING_COST("Running Cost", Metric.RUNNING_COST),
    RELIABILITY("Reliability", Metric.RELIABILITY),
    COMFORT("Comfort", Metric.COMFORT),
    PERFORMANCE("Performance", Metric.PERFORMANCE),
    DAILY_DRIVER("Daily Driver", Metric.DAILY_DRIVER),
    TECHNOLOGY("Technology", Metric.TECHNOLOGY),
    PRESTIGE("Prestige", Metric.PRESTIGE),
    AWESOMENESS("Awesomeness", Metric.AWESOMENESS),
    SCORE_PER_100K("Score/R100k", null);

    private final String defaultLabel;
    private final Metric metric;

    ScatterPlotAxis(String defaultLabel, Metric metric) {
        this.defaultLabel = defaultLabel;
        this.metric = metric;
    }

    public Metric metric() {
        return metric;
    }

    public String label() {
        return defaultLabel;
    }

    public String label(ScoringProfile profile) {
        if (metric != null) {
            return MetricLabels.displayName(metric, profile);
        }
        return defaultLabel;
    }

    public static ScatterPlotAxis fromMetric(Metric metric) {
        if (metric == null) {
            throw new IllegalArgumentException("Metric is required");
        }
        for (ScatterPlotAxis axis : values()) {
            if (metric.equals(axis.metric)) {
                return axis;
            }
        }
        throw new IllegalArgumentException("Unsupported scatter plot metric: " + metric);
    }

    public static List<ScatterPlotAxis> selectableAxes(ScoringProfile profile) {
        List<ScatterPlotAxis> axes = new ArrayList<>();
        axes.add(PRICE);
        axes.add(OVERALL_SCORE);
        for (Metric metric : ProfileScoreMetrics.scoreMetrics(profile)) {
            axes.add(fromMetric(metric));
        }
        axes.add(SCORE_PER_100K);
        return List.copyOf(axes);
    }

    @Override
    public String toString() {
        return defaultLabel;
    }
}
