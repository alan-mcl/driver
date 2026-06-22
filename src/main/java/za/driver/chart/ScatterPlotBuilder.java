package za.driver.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import za.driver.model.Vehicle;
import za.driver.model.VehicleIdentity;

public final class ScatterPlotBuilder {

    private ScatterPlotBuilder() {
    }

    public static ScatterPlotData build(List<Vehicle> vehicles, ScatterPlotAxis xAxis, ScatterPlotAxis yAxis) {
        BrandColorPalette palette = new BrandColorPalette();
        List<ScatterPlotPoint> points = new ArrayList<>();
        int skipped = 0;

        for (Vehicle vehicle : vehicles) {
            OptionalDouble x = ScatterPlotValues.value(vehicle, xAxis);
            OptionalDouble y = ScatterPlotValues.value(vehicle, yAxis);
            if (x.isEmpty() || y.isEmpty()) {
                skipped++;
                continue;
            }
            String make = vehicle.getMake();
            palette.registerMake(make);
            points.add(new ScatterPlotPoint(
                    vehicle,
                    x.getAsDouble(),
                    y.getAsDouble(),
                    palette.colorForMake(make),
                    VehicleIdentity.shortLabel(vehicle),
                    VehicleIdentity.label(vehicle),
                    0,
                    0));
        }

        return new ScatterPlotData(points, palette.assignedColors(), palette.displayNames(), skipped);
    }
}
