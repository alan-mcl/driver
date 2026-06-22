package za.driver.chart;

import java.awt.Color;

import za.driver.model.Vehicle;

public record ScatterPlotPoint(
        Vehicle vehicle,
        double x,
        double y,
        Color color,
        String label,
        String tooltipText,
        int pixelX,
        int pixelY) {

    public ScatterPlotPoint withPixelPosition(int pixelX, int pixelY) {
        return new ScatterPlotPoint(vehicle, x, y, color, label, tooltipText, pixelX, pixelY);
    }
}
