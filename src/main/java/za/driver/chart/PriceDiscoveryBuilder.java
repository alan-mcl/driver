package za.driver.chart;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import za.driver.model.DerivedMetrics;
import za.driver.model.Pricing;
import za.driver.model.Vehicle;
import za.driver.model.VehicleIdentity;

public final class PriceDiscoveryBuilder {

    private static final int CURVE_SAMPLE_COUNT = 40;

    private PriceDiscoveryBuilder() {
    }

    public static PriceDiscoveryData build(List<Vehicle> vehicles, UUID benchmarkId) {
        if (vehicles == null || vehicles.isEmpty()) {
            return PriceDiscoveryData.empty(0);
        }

        BrandColorPalette palette = new BrandColorPalette();
        List<PlottableVehicle> plottable = new ArrayList<>();
        int skipped = 0;

        for (Vehicle vehicle : vehicles) {
            PlottableVehicle entry = toPlottable(vehicle, palette);
            if (entry == null) {
                skipped++;
            } else {
                plottable.add(entry);
            }
        }

        if (plottable.isEmpty()) {
            return PriceDiscoveryData.empty(skipped);
        }

        PlottableVehicle benchmarkEntry = selectBenchmark(plottable, benchmarkId);
        List<PlottableVehicle> subjectEntries = plottable.stream()
                .filter(entry -> !entry.vehicle().getId().equals(benchmarkEntry.vehicle().getId()))
                .toList();

        double[] xRange = computeXRange(plottable, benchmarkEntry, subjectEntries);
        double xMin = xRange[0];
        double xMax = xRange[1];

        PriceDiscoveryVehicle benchmark = toDiscoveryVehicle(benchmarkEntry, List.of());
        List<PriceDiscoveryVehicle> subjects = new ArrayList<>();
        List<PriceDiscoveryCrossover> crossovers = new ArrayList<>();

        for (PlottableVehicle subjectEntry : subjectEntries) {
            List<PriceDiscoveryPoint> curvePoints =
                    PriceDiscoveryCalculator.sampleCurve(subjectEntry.overallScore(), xMin, xMax, CURVE_SAMPLE_COUNT);
            subjects.add(toDiscoveryVehicle(subjectEntry, curvePoints));
            crossovers.add(buildCrossover(subjectEntry, benchmarkEntry));
        }

        double[] yRange = computeYRange(benchmarkEntry, plottable, subjects, xMin, xMax);
        double[] paddedX = PriceDiscoveryCalculator.paddedRange(xMin, xMax);
        double[] paddedY = PriceDiscoveryCalculator.paddedRange(yRange[0], yRange[1]);

        return new PriceDiscoveryData(
                benchmark,
                List.copyOf(subjects),
                List.copyOf(crossovers),
                paddedX[0],
                paddedX[1],
                paddedY[0],
                paddedY[1],
                palette.assignedColors(),
                palette.displayNames(),
                skipped);
    }

    public static UUID defaultBenchmarkId(List<Vehicle> vehicles) {
        if (vehicles == null || vehicles.isEmpty()) {
            return null;
        }
        BrandColorPalette palette = new BrandColorPalette();
        return vehicles.stream()
                .map(vehicle -> toPlottable(vehicle, palette))
                .filter(entry -> entry != null)
                .max(Comparator.comparingDouble(PlottableVehicle::listScorePer100k))
                .map(entry -> entry.vehicle().getId())
                .orElse(null);
    }

    private static PlottableVehicle toPlottable(Vehicle vehicle, BrandColorPalette palette) {
        if (vehicle == null) {
            return null;
        }
        DerivedMetrics metrics = vehicle.getDerivedMetrics();
        Pricing pricing = vehicle.getPricing();
        if (metrics == null || metrics.getOverallScore() == null) {
            return null;
        }
        if (pricing == null || pricing.getListPrice() == null) {
            return null;
        }
        double listPrice = pricing.getListPrice().doubleValue();
        if (listPrice <= 0.0) {
            return null;
        }
        double overallScore = metrics.getOverallScore();
        Double listScorePer100k = PriceDiscoveryCalculator.scorePer100kAtPrice(overallScore, listPrice);
        if (listScorePer100k == null) {
            return null;
        }

        String make = vehicle.getMake();
        Color color = palette.colorForMake(make);
        String label = VehicleIdentity.shortLabel(vehicle);
        String tooltip = VehicleIdentity.label(vehicle);
        return new PlottableVehicle(vehicle, color, label, tooltip, listPrice, overallScore, listScorePer100k);
    }

    private static PlottableVehicle selectBenchmark(List<PlottableVehicle> plottable, UUID benchmarkId) {
        if (benchmarkId != null) {
            for (PlottableVehicle entry : plottable) {
                if (benchmarkId.equals(entry.vehicle().getId())) {
                    return entry;
                }
            }
        }
        return plottable.stream()
                .max(Comparator.comparingDouble(PlottableVehicle::listScorePer100k))
                .orElse(plottable.get(0));
    }

    private static PriceDiscoveryVehicle toDiscoveryVehicle(PlottableVehicle entry, List<PriceDiscoveryPoint> curvePoints) {
        return new PriceDiscoveryVehicle(
                entry.vehicle(),
                entry.color(),
                entry.label(),
                entry.tooltip(),
                entry.listPrice(),
                entry.overallScore(),
                entry.listScorePer100k(),
                curvePoints);
    }

    private static PriceDiscoveryCrossover buildCrossover(PlottableVehicle subject, PlottableVehicle benchmark) {
        Double crossoverPrice = PriceDiscoveryCalculator.crossoverPrice(
                subject.overallScore(),
                benchmark.listPrice(),
                benchmark.overallScore());
        double crossover = crossoverPrice != null ? crossoverPrice : 0.0;
        Double crossoverScore = PriceDiscoveryCalculator.scorePer100kAtPrice(subject.overallScore(), crossover);
        boolean beatsAtList = crossoverPrice != null
                && PriceDiscoveryCalculator.beatsAtList(crossover, subject.listPrice());
        Double dealerOffer = dealerOffer(subject.vehicle());
        return new PriceDiscoveryCrossover(
                subject.vehicle(),
                subject.label(),
                subject.listPrice(),
                dealerOffer,
                subject.listScorePer100k(),
                crossover,
                crossoverScore != null ? crossoverScore : benchmark.listScorePer100k(),
                crossoverPrice != null ? PriceDiscoveryCalculator.discountAmount(subject.listPrice(), crossover) : 0.0,
                crossoverPrice != null ? PriceDiscoveryCalculator.discountPct(subject.listPrice(), crossover) : 0.0,
                beatsAtList);
    }

    private static Double dealerOffer(Vehicle vehicle) {
        if (vehicle == null || vehicle.getPricing() == null || vehicle.getPricing().getDealerOffer() == null) {
            return null;
        }
        return vehicle.getPricing().getDealerOffer().doubleValue();
    }

    private static double[] computeXRange(
            List<PlottableVehicle> plottable,
            PlottableVehicle benchmark,
            List<PlottableVehicle> subjects) {
        double minPrice = plottable.stream().mapToDouble(PlottableVehicle::listPrice).min().orElse(0.0);
        double maxPrice = plottable.stream().mapToDouble(PlottableVehicle::listPrice).max().orElse(1.0);

        double minCrossover = subjects.stream()
                .mapToDouble(subject -> {
                    Double crossover = PriceDiscoveryCalculator.crossoverPrice(
                            subject.overallScore(),
                            benchmark.listPrice(),
                            benchmark.overallScore());
                    return crossover != null ? crossover : Double.MAX_VALUE;
                })
                .filter(value -> value != Double.MAX_VALUE)
                .min()
                .orElse(minPrice);

        double xMin = Math.min(minPrice, minCrossover) * 0.85;
        double xMax = maxPrice * 1.05;
        if (xMin <= 0.0) {
            xMin = minPrice * 0.85;
        }
        if (xMax <= xMin) {
            xMax = xMin + 1.0;
        }
        return new double[] {xMin, xMax};
    }

    private static double[] computeYRange(
            PlottableVehicle benchmark,
            List<PlottableVehicle> plottable,
            List<PriceDiscoveryVehicle> subjects,
            double xMin,
            double xMax) {
        double yMin = benchmark.listScorePer100k();
        double yMax = benchmark.listScorePer100k();

        for (PlottableVehicle entry : plottable) {
            yMin = Math.min(yMin, entry.listScorePer100k());
            yMax = Math.max(yMax, entry.listScorePer100k());
        }

        for (PriceDiscoveryVehicle subject : subjects) {
            for (PriceDiscoveryPoint point : subject.curvePoints()) {
                if (point.price() >= xMin && point.price() <= xMax) {
                    yMin = Math.min(yMin, point.scorePer100k());
                    yMax = Math.max(yMax, point.scorePer100k());
                }
            }
        }

        if (yMin == yMax) {
            yMax = yMin + 1.0;
        }
        return new double[] {yMin, yMax};
    }

    private record PlottableVehicle(
            Vehicle vehicle,
            Color color,
            String label,
            String tooltip,
            double listPrice,
            double overallScore,
            double listScorePer100k) {
    }
}
