package za.driver.chart;

import java.awt.Color;
import java.util.List;

import za.driver.model.Vehicle;

public record PriceDiscoveryVehicle(
        Vehicle vehicle,
        Color color,
        String label,
        String tooltipText,
        double listPrice,
        double overallScore,
        double listScorePer100k,
        List<PriceDiscoveryPoint> curvePoints) {
}
