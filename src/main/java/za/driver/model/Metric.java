package za.driver.model;

import java.util.EnumSet;
import java.util.Set;

public enum Metric {
    SAFETY,
    RUNNING_COST,
    RELIABILITY,
    COMFORT,
    PERFORMANCE,
    DAILY_DRIVER,
    TECHNOLOGY,
    PRESTIGE,
    AWESOMENESS;

    public static final Set<Metric> BASE_METRICS = EnumSet.of(
            SAFETY,
            RUNNING_COST,
            RELIABILITY,
            COMFORT,
            PERFORMANCE,
            DAILY_DRIVER,
            TECHNOLOGY,
            PRESTIGE);

    public static final Set<Metric> PROFILE_WEIGHT_METRICS = EnumSet.of(
            SAFETY,
            RUNNING_COST,
            RELIABILITY,
            PERFORMANCE,
            AWESOMENESS);
}
