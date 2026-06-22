package za.driver.spreadsheet;

import java.util.List;

import za.driver.model.Aspiration;
import za.driver.model.BodyType;
import za.driver.model.ClimateControlType;
import za.driver.model.DrivetrainType;
import za.driver.model.FuelType;
import za.driver.model.SourceType;
import za.driver.model.TransmissionType;
import za.driver.model.VehicleStatus;

public final class VehicleSpreadsheetSchema {

    public static final int SCHEMA_VERSION = 1;

    private static final List<SpreadsheetColumn> COLUMNS = List.of(
            col("id", ColumnType.UUID),
            col("make", ColumnType.STRING),
            col("model", ColumnType.STRING),
            col("derivative", ColumnType.STRING),
            col("pricing.priceZar", ColumnType.DOUBLE),
            col("pricing.priceDate", ColumnType.DATE),
            col("modelYear", ColumnType.INTEGER),
            enumCol("bodyType", BodyType.class),
            enumCol("status", VehicleStatus.class),
            col("notes", ColumnType.STRING),
            enumCol("engine.fuelType", FuelType.class),
            col("engine.displacementCc", ColumnType.INTEGER),
            col("engine.cylinders", ColumnType.INTEGER),
            col("engine.powerKw", ColumnType.DOUBLE),
            col("engine.torqueNm", ColumnType.DOUBLE),
            enumCol("engine.aspiration", Aspiration.class),
            col("engine.hybrid", ColumnType.BOOLEAN),
            col("engine.phev", ColumnType.BOOLEAN),
            enumCol("transmission.type", TransmissionType.class),
            col("transmission.gears", ColumnType.INTEGER),
            enumCol("transmission.drivetrain", DrivetrainType.class),
            col("performance.zeroToHundredSeconds", ColumnType.DOUBLE),
            col("performance.topSpeedKmh", ColumnType.INTEGER),
            col("dimensions.lengthMm", ColumnType.INTEGER),
            col("dimensions.widthMm", ColumnType.INTEGER),
            col("dimensions.heightMm", ColumnType.INTEGER),
            col("dimensions.wheelbaseMm", ColumnType.INTEGER),
            col("dimensions.groundClearanceMm", ColumnType.INTEGER),
            col("dimensions.turningCircleM", ColumnType.DOUBLE),
            col("dimensions.bootLitres", ColumnType.INTEGER),
            col("dimensions.kerbWeightKg", ColumnType.INTEGER),
            col("dimensions.seats", ColumnType.INTEGER),
            col("towing.towingBrakedKg", ColumnType.INTEGER),
            col("wheels.tyreSize", ColumnType.STRING),
            col("infotainment.infotainmentScreenSizeInches", ColumnType.DOUBLE),
            col("infotainment.speakerCount", ColumnType.INTEGER),
            col("economy.fuelConsumptionCombined", ColumnType.DOUBLE),
            col("economy.fuelTankLitres", ColumnType.DOUBLE),
            col("economy.co2Gkm", ColumnType.DOUBLE),
            col("safety.ncapStars", ColumnType.INTEGER),
            col("safety.airbags", ColumnType.INTEGER),
            col("safety.abs", ColumnType.BOOLEAN),
            col("safety.esp", ColumnType.BOOLEAN),
            col("safety.tractionControl", ColumnType.BOOLEAN),
            col("safety.aeb", ColumnType.BOOLEAN),
            col("safety.laneAssist", ColumnType.BOOLEAN),
            col("safety.blindSpotMonitoring", ColumnType.BOOLEAN),
            col("safety.adaptiveCruiseControl", ColumnType.BOOLEAN),
            col("safety.rearCrossTrafficAlert", ColumnType.BOOLEAN),
            col("features.androidAuto", ColumnType.BOOLEAN),
            col("features.appleCarplay", ColumnType.BOOLEAN),
            col("features.reverseCamera", ColumnType.BOOLEAN),
            col("features.parkingSensorsFront", ColumnType.BOOLEAN),
            col("features.parkingSensorsRear", ColumnType.BOOLEAN),
            col("features.digitalCluster", ColumnType.BOOLEAN),
            col("features.keylessEntry", ColumnType.BOOLEAN),
            col("features.pushButtonStart", ColumnType.BOOLEAN),
            col("features.wirelessCharging", ColumnType.BOOLEAN),
            col("features.climateControl", ColumnType.BOOLEAN),
            enumCol("features.climateControlType", ClimateControlType.class),
            col("features.heatedSeats", ColumnType.BOOLEAN),
            col("features.electricSeats", ColumnType.BOOLEAN),
            col("features.sunroof", ColumnType.BOOLEAN),
            col("features.premiumAudio", ColumnType.BOOLEAN),
            col("ownership.warrantyYears", ColumnType.INTEGER),
            col("ownership.warrantyKm", ColumnType.INTEGER),
            col("ownership.servicePlanYears", ColumnType.INTEGER),
            col("ownership.servicePlanKm", ColumnType.INTEGER),
            col("ownership.serviceIntervalKm", ColumnType.INTEGER),
            col("ownership.maintenancePlanYears", ColumnType.INTEGER),
            col("ownership.maintenancePlanKm", ColumnType.INTEGER),
            col("ownership.partsSupportScore", ColumnType.INTEGER),
            col("ownership.localProduction", ColumnType.BOOLEAN),
            col("derivedMetrics.reliabilityScore", ColumnType.DOUBLE),
            col("derivedMetrics.prestigeScore", ColumnType.DOUBLE),
            enumCol("source.sourceType", SourceType.class),
            col("source.sourceName", ColumnType.STRING),
            col("source.sourceUrl", ColumnType.STRING));

    private VehicleSpreadsheetSchema() {
    }

    public static List<SpreadsheetColumn> columns() {
        return COLUMNS;
    }

    public static List<String> headers() {
        return COLUMNS.stream().map(SpreadsheetColumn::header).toList();
    }

    public static int columnIndex(String header) {
        for (int i = 0; i < COLUMNS.size(); i++) {
            if (COLUMNS.get(i).header().equals(header)) {
                return i;
            }
        }
        return -1;
    }

    private static SpreadsheetColumn col(String header, ColumnType type) {
        return new SpreadsheetColumn(header, type);
    }

    private static SpreadsheetColumn enumCol(String header, Class<? extends Enum<?>> enumType) {
        return new SpreadsheetColumn(header, ColumnType.ENUM, enumType);
    }
}
