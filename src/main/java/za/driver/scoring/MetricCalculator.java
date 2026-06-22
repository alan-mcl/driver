package za.driver.scoring;

import za.driver.model.Metric;
import za.driver.model.Vehicle;

public interface MetricCalculator {

    Metric metric();

    Double calculate(Vehicle vehicle);
}
