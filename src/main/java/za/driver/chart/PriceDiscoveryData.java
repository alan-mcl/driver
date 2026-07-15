package za.driver.chart;

import java.awt.Color;
import java.util.List;
import java.util.Map;

public record PriceDiscoveryData(
        PriceDiscoveryVehicle benchmark,
        List<PriceDiscoveryVehicle> subjects,
        List<PriceDiscoveryCrossover> crossovers,
        double xMin,
        double xMax,
        double yMin,
        double yMax,
        Map<String, Color> legend,
        Map<String, String> legendLabels,
        int skippedCount) {

    public static PriceDiscoveryData empty(int skippedCount) {
        return new PriceDiscoveryData(null, List.of(), List.of(), 0, 1, 0, 1, Map.of(), Map.of(), skippedCount);
    }
}
