package za.driver.scoring;

import za.driver.model.Metric;
import za.driver.model.Vehicle;

public class PrestigeCalculator implements MetricCalculator {

    @Override
    public Metric metric() {
        return Metric.PRESTIGE;
    }

    @Override
    public Double calculate(Vehicle vehicle) {
        return null;
    }
}
