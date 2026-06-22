package za.driver.model;

public record GarageDimensions(int garageWidthMm, int arcRadiusMm, int arcStartHeightMm) {

    public static final int DEFAULT_GARAGE_WIDTH_MM = 2370;
    public static final int DEFAULT_ARC_RADIUS_MM = 1185;
    public static final int DEFAULT_ARC_START_HEIGHT_MM = 1120;

    public static GarageDimensions defaults() {
        return new GarageDimensions(
                DEFAULT_GARAGE_WIDTH_MM,
                DEFAULT_ARC_RADIUS_MM,
                DEFAULT_ARC_START_HEIGHT_MM);
    }
}
