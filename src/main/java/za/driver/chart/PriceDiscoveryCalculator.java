package za.driver.chart;

import java.util.ArrayList;
import java.util.List;

public final class PriceDiscoveryCalculator {

    private static final double HUNDRED_THOUSAND = 100_000.0;

    private PriceDiscoveryCalculator() {
    }

    public static Double scorePer100kAtPrice(Double overallScore, double price) {
        if (overallScore == null || price <= 0.0) {
            return null;
        }
        return overallScore / price * HUNDRED_THOUSAND;
    }

    public static Double crossoverPrice(Double subjectScore, double benchmarkPrice, Double benchmarkScore) {
        if (subjectScore == null || benchmarkScore == null || benchmarkPrice <= 0.0 || benchmarkScore <= 0.0) {
            return null;
        }
        return subjectScore * benchmarkPrice / benchmarkScore;
    }

    public static double discountZar(double listPrice, double crossoverPrice) {
        return listPrice - crossoverPrice;
    }

    public static double discountPct(double listPrice, double crossoverPrice) {
        if (listPrice <= 0.0) {
            return 0.0;
        }
        return discountZar(listPrice, crossoverPrice) / listPrice * 100.0;
    }

    public static boolean beatsAtList(double crossoverPrice, double listPrice) {
        return crossoverPrice >= listPrice;
    }

    public static List<PriceDiscoveryPoint> sampleCurve(Double overallScore, double xMin, double xMax, int sampleCount) {
        if (overallScore == null || sampleCount < 2 || xMax <= xMin || xMin <= 0.0) {
            return List.of();
        }
        List<PriceDiscoveryPoint> points = new ArrayList<>(sampleCount);
        double step = (xMax - xMin) / (sampleCount - 1);
        for (int i = 0; i < sampleCount; i++) {
            double price = xMin + step * i;
            Double scorePer100k = scorePer100kAtPrice(overallScore, price);
            if (scorePer100k != null) {
                points.add(new PriceDiscoveryPoint(price, scorePer100k));
            }
        }
        return List.copyOf(points);
    }

    public static double[] paddedRange(double min, double max) {
        if (min == max) {
            double delta = min == 0 ? 1.0 : Math.abs(min) * 0.05;
            return new double[] {min - delta, max + delta};
        }
        double padding = (max - min) * 0.05;
        return new double[] {min - padding, max + padding};
    }
}
