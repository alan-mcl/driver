package za.driver.garage;

import za.driver.model.Dimensions;
import za.driver.model.GarageDimensions;

public final class GarageClearanceCalculator {

    private GarageClearanceCalculator() {
    }

    public static Integer clearanceMm(GarageDimensions garage, Dimensions dimensions) {
        if (garage == null || dimensions == null) {
            return null;
        }
        Integer widthMm = dimensions.getWidthMm();
        Integer heightMm = dimensions.getHeightMm();
        if (widthMm == null || heightMm == null) {
            return null;
        }
        Integer openingWidth = openingWidthAtHeightMm(heightMm, garage);
        if (openingWidth == null) {
            return null;
        }
        return openingWidth - widthMm;
    }

    static Integer openingWidthAtHeightMm(int heightMm, GarageDimensions garage) {
        if (heightMm <= garage.arcStartHeightMm()) {
            return garage.garageWidthMm();
        }
        int dy = heightMm - garage.arcStartHeightMm();
        int radius = garage.arcRadiusMm();
        if (dy > radius) {
            return null;
        }
        double halfWidth = Math.sqrt((long) radius * radius - (long) dy * dy);
        return (int) Math.round(halfWidth * 2.0);
    }
}
