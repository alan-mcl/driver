package za.driver.chart;

import java.awt.Color;
import java.util.List;
import java.util.Map;

public record ScatterPlotData(
        List<ScatterPlotPoint> points,
        Map<String, Color> legend,
        Map<String, String> legendLabels,
        int skippedCount) {
}
