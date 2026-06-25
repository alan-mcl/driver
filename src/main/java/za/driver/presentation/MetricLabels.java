package za.driver.presentation;

import za.driver.model.Metric;

public final class MetricLabels {

    private MetricLabels() {
    }

    public static String displayName(Metric metric) {
        return switch (metric) {
            case SAFETY -> "Safety";
            case RUNNING_COST -> "Running Cost";
            case RELIABILITY -> "Reliability";
            case COMFORT -> "Comfort";
            case PERFORMANCE -> "Performance";
            case DAILY_DRIVER -> "Daily Driver";
            case TECHNOLOGY -> "Technology";
            case PRESTIGE -> "Prestige";
            case AWESOMENESS -> "Awesomeness";
        };
    }
}
