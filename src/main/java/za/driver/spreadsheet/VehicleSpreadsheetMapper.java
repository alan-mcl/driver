package za.driver.spreadsheet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import za.driver.model.Aspiration;
import za.driver.model.BodyType;
import za.driver.model.ClimateControlType;
import za.driver.model.Dimensions;
import za.driver.model.DrivetrainType;
import za.driver.model.Economy;
import za.driver.model.Engine;
import za.driver.model.Features;
import za.driver.model.FuelType;
import za.driver.model.Infotainment;
import za.driver.model.ManualScoreOverrides;
import za.driver.model.Ownership;
import za.driver.model.Performance;
import za.driver.model.Pricing;
import za.driver.model.Safety;
import za.driver.model.Source;
import za.driver.model.SourceType;
import za.driver.model.Towing;
import za.driver.model.Transmission;
import za.driver.model.TransmissionType;
import za.driver.model.Vehicle;
import za.driver.model.VehicleStatus;
import za.driver.model.Wheels;
import za.driver.scoring.ScoringOverrides;

public final class VehicleSpreadsheetMapper {

    private static final String RELIABILITY_MANUAL_ESTIMATE_HEADER = "manualScoreOverrides.reliabilityManualEstimate";
    private static final String RELIABILITY_HEURISTIC_HEADER = "derivedMetrics.reliabilityHeuristic";
    private static final String RELIABILITY_SCORE_HEADER = "derivedMetrics.reliabilityScore";
    private static final String PRESTIGE_MANUAL_HEADER = "manualScoreOverrides.prestigeScore";
    private static final String PRESTIGE_SCORE_HEADER = "derivedMetrics.prestigeScore";

    private VehicleSpreadsheetMapper() {
    }

    public static List<String> toRowValues(Vehicle vehicle) {
        List<String> values = new ArrayList<>(VehicleSpreadsheetSchema.columns().size());
        for (SpreadsheetColumn column : VehicleSpreadsheetSchema.columns()) {
            values.add(formatValue(readValue(vehicle, column.header())));
        }
        return values;
    }

    public static Map<String, String> toRowMap(Vehicle vehicle) {
        Map<String, String> row = new LinkedHashMap<>();
        for (SpreadsheetColumn column : VehicleSpreadsheetSchema.columns()) {
            row.put(column.header(), formatValue(readValue(vehicle, column.header())));
        }
        return row;
    }

    public static Vehicle fromRowValues(List<String> values, boolean includeStatus) {
        Map<String, String> row = new LinkedHashMap<>();
        List<SpreadsheetColumn> columns = VehicleSpreadsheetSchema.columns();
        for (int i = 0; i < columns.size(); i++) {
            String value = i < values.size() ? values.get(i) : "";
            row.put(columns.get(i).header(), value == null ? "" : value);
        }
        return fromRowMap(row, includeStatus);
    }

    public static Vehicle fromRowMap(Map<String, String> row, boolean includeStatus) {
        Vehicle vehicle = new Vehicle();
        List<String> errors = new ArrayList<>();
        Map<String, String> normalizedRow = normalizeLegacyHeaders(row);

        for (SpreadsheetColumn column : VehicleSpreadsheetSchema.columns()) {
            String header = column.header();
            if (!includeStatus && "status".equals(header)) {
                continue;
            }
            if (isExportOnlyHeader(header)) {
                continue;
            }
            String raw = normalizedRow.getOrDefault(header, "");
            if (isBlank(raw)) {
                continue;
            }
            try {
                applyValue(vehicle, header, raw.trim());
            } catch (IllegalArgumentException ex) {
                errors.add(header + ": " + ex.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }
        return vehicle;
    }

    public static int countNonBlankSpecFields(Map<String, String> row) {
        int count = 0;
        for (SpreadsheetColumn column : VehicleSpreadsheetSchema.columns()) {
            if ("id".equals(column.header())) {
                continue;
            }
            if (!isBlank(row.getOrDefault(column.header(), ""))) {
                count++;
            }
        }
        return count;
    }

    public static boolean hasImportableData(Map<String, String> row) {
        return countNonBlankSpecFields(row) > 0;
    }

    public static ScoringOverrides scoringOverridesFromRow(Map<String, String> row) {
        Double reliability = parseOptionalDouble(row, RELIABILITY_MANUAL_ESTIMATE_HEADER);
        Double prestige = parseOptionalDouble(row, PRESTIGE_MANUAL_HEADER);
        if (reliability == null && prestige == null) {
            return ScoringOverrides.none();
        }
        return ScoringOverrides.of(reliability, prestige);
    }

    private static boolean isExportOnlyHeader(String header) {
        return RELIABILITY_HEURISTIC_HEADER.equals(header)
                || RELIABILITY_SCORE_HEADER.equals(header)
                || PRESTIGE_SCORE_HEADER.equals(header);
    }

    private static Map<String, String> normalizeLegacyHeaders(Map<String, String> row) {
        Map<String, String> normalized = new LinkedHashMap<>(row);
        boolean changed = false;
        if (normalized.containsKey("pricing.priceZar") && !normalized.containsKey("pricing.listPrice")) {
            normalized.put("pricing.listPrice", normalized.get("pricing.priceZar"));
            changed = true;
        }
        if (normalized.containsKey("pricing.listPriceZar") && !normalized.containsKey("pricing.listPrice")) {
            normalized.put("pricing.listPrice", normalized.get("pricing.listPriceZar"));
            changed = true;
        }
        if (normalized.containsKey("pricing.dealerOfferZar") && !normalized.containsKey("pricing.dealerOffer")) {
            normalized.put("pricing.dealerOffer", normalized.get("pricing.dealerOfferZar"));
            changed = true;
        }
        if (normalized.containsKey("pricing.priceDate") && !normalized.containsKey("pricing.listPriceDate")) {
            normalized.put("pricing.listPriceDate", normalized.get("pricing.priceDate"));
            changed = true;
        }
        return changed ? normalized : row;
    }

    static Object readValue(Vehicle vehicle, String header) {
        if (vehicle == null) {
            return null;
        }
        return switch (header) {
            case "id" -> vehicle.getId();
            case "make" -> vehicle.getMake();
            case "model" -> vehicle.getModel();
            case "derivative" -> vehicle.getDerivative();
            case "modelYear" -> vehicle.getModelYear();
            case "bodyType" -> vehicle.getBodyType();
            case "status" -> vehicle.getStatus();
            case "notes" -> vehicle.getNotes();
            case "engine.fuelType" -> nestedEngine(vehicle).getFuelType();
            case "engine.displacementCc" -> nestedEngine(vehicle).getDisplacementCc();
            case "engine.cylinders" -> nestedEngine(vehicle).getCylinders();
            case "engine.powerKw" -> nestedEngine(vehicle).getPowerKw();
            case "engine.torqueNm" -> nestedEngine(vehicle).getTorqueNm();
            case "engine.aspiration" -> nestedEngine(vehicle).getAspiration();
            case "engine.hybrid" -> nestedEngine(vehicle).getHybrid();
            case "engine.phev" -> nestedEngine(vehicle).getPhev();
            case "transmission.type" -> nestedTransmission(vehicle).getType();
            case "transmission.gears" -> nestedTransmission(vehicle).getGears();
            case "transmission.drivetrain" -> nestedTransmission(vehicle).getDrivetrain();
            case "performance.zeroToHundredSeconds" -> nestedPerformance(vehicle).getZeroToHundredSeconds();
            case "performance.topSpeedKmh" -> nestedPerformance(vehicle).getTopSpeedKmh();
            case "dimensions.lengthMm" -> nestedDimensions(vehicle).getLengthMm();
            case "dimensions.widthMm" -> nestedDimensions(vehicle).getWidthMm();
            case "dimensions.heightMm" -> nestedDimensions(vehicle).getHeightMm();
            case "dimensions.wheelbaseMm" -> nestedDimensions(vehicle).getWheelbaseMm();
            case "dimensions.groundClearanceMm" -> nestedDimensions(vehicle).getGroundClearanceMm();
            case "dimensions.turningCircleM" -> nestedDimensions(vehicle).getTurningCircleM();
            case "dimensions.bootLitres" -> nestedDimensions(vehicle).getBootLitres();
            case "dimensions.kerbWeightKg" -> nestedDimensions(vehicle).getKerbWeightKg();
            case "dimensions.seats" -> nestedDimensions(vehicle).getSeats();
            case "towing.towingBrakedKg" -> nestedTowing(vehicle).getTowingBrakedKg();
            case "wheels.tyreSize" -> nestedWheels(vehicle).getTyreSize();
            case "infotainment.infotainmentScreenSizeInches" ->
                    nestedInfotainment(vehicle).getInfotainmentScreenSizeInches();
            case "infotainment.speakerCount" -> nestedInfotainment(vehicle).getSpeakerCount();
            case "economy.fuelConsumptionCombined" -> nestedEconomy(vehicle).getFuelConsumptionCombined();
            case "economy.fuelTankLitres" -> nestedEconomy(vehicle).getFuelTankLitres();
            case "economy.co2Gkm" -> nestedEconomy(vehicle).getCo2Gkm();
            case "safety.ncapStars" -> nestedSafety(vehicle).getNcapStars();
            case "safety.airbags" -> nestedSafety(vehicle).getAirbags();
            case "safety.abs" -> nestedSafety(vehicle).getAbs();
            case "safety.esp" -> nestedSafety(vehicle).getEsp();
            case "safety.tractionControl" -> nestedSafety(vehicle).getTractionControl();
            case "safety.aeb" -> nestedSafety(vehicle).getAeb();
            case "safety.laneAssist" -> nestedSafety(vehicle).getLaneAssist();
            case "safety.blindSpotMonitoring" -> nestedSafety(vehicle).getBlindSpotMonitoring();
            case "safety.adaptiveCruiseControl" -> nestedSafety(vehicle).getAdaptiveCruiseControl();
            case "safety.rearCrossTrafficAlert" -> nestedSafety(vehicle).getRearCrossTrafficAlert();
            case "features.androidAuto" -> nestedFeatures(vehicle).getAndroidAuto();
            case "features.appleCarplay" -> nestedFeatures(vehicle).getAppleCarplay();
            case "features.reverseCamera" -> nestedFeatures(vehicle).getReverseCamera();
            case "features.parkingSensorsFront" -> nestedFeatures(vehicle).getParkingSensorsFront();
            case "features.parkingSensorsRear" -> nestedFeatures(vehicle).getParkingSensorsRear();
            case "features.digitalCluster" -> nestedFeatures(vehicle).getDigitalCluster();
            case "features.keylessEntry" -> nestedFeatures(vehicle).getKeylessEntry();
            case "features.pushButtonStart" -> nestedFeatures(vehicle).getPushButtonStart();
            case "features.wirelessCharging" -> nestedFeatures(vehicle).getWirelessCharging();
            case "features.climateControl" -> nestedFeatures(vehicle).getClimateControl();
            case "features.climateControlType" -> nestedFeatures(vehicle).getClimateControlType();
            case "features.heatedSeats" -> nestedFeatures(vehicle).getHeatedSeats();
            case "features.electricSeats" -> nestedFeatures(vehicle).getElectricSeats();
            case "features.sunroof" -> nestedFeatures(vehicle).getSunroof();
            case "features.premiumAudio" -> nestedFeatures(vehicle).getPremiumAudio();
            case "ownership.warrantyYears" -> nestedOwnership(vehicle).getWarrantyYears();
            case "ownership.warrantyKm" -> nestedOwnership(vehicle).getWarrantyKm();
            case "ownership.servicePlanYears" -> nestedOwnership(vehicle).getServicePlanYears();
            case "ownership.servicePlanKm" -> nestedOwnership(vehicle).getServicePlanKm();
            case "ownership.serviceIntervalKm" -> nestedOwnership(vehicle).getServiceIntervalKm();
            case "ownership.maintenancePlanYears" -> nestedOwnership(vehicle).getMaintenancePlanYears();
            case "ownership.maintenancePlanKm" -> nestedOwnership(vehicle).getMaintenancePlanKm();
            case "ownership.partsSupportScore" -> nestedOwnership(vehicle).getPartsSupportScore();
            case "ownership.localProduction" -> nestedOwnership(vehicle).getLocalProduction();
            case RELIABILITY_MANUAL_ESTIMATE_HEADER -> manualReliabilityEstimate(vehicle);
            case RELIABILITY_HEURISTIC_HEADER -> derivedReliabilityHeuristic(vehicle);
            case RELIABILITY_SCORE_HEADER -> derivedReliabilityScore(vehicle);
            case PRESTIGE_MANUAL_HEADER -> manualPrestigeOverride(vehicle);
            case PRESTIGE_SCORE_HEADER -> derivedPrestigeScore(vehicle);
            case "pricing.listPrice", "pricing.listPriceZar", "pricing.priceZar" -> nestedPricing(vehicle).getListPrice();
            case "pricing.dealerOffer", "pricing.dealerOfferZar" -> nestedPricing(vehicle).getDealerOffer();
            case "pricing.listPriceDate", "pricing.priceDate" -> nestedPricing(vehicle).getListPriceDate();
            case "pricing.dealerOfferDate" -> nestedPricing(vehicle).getDealerOfferDate();
            case "source.sourceType" -> nestedSource(vehicle).getSourceType();
            case "source.sourceName" -> nestedSource(vehicle).getSourceName();
            case "source.sourceUrl" -> nestedSource(vehicle).getSourceUrl();
            default -> throw new IllegalArgumentException("Unknown header: " + header);
        };
    }

    private static void applyValue(Vehicle vehicle, String header, String raw) {
        switch (header) {
            case "id" -> vehicle.setId(UUID.fromString(raw));
            case "make" -> vehicle.setMake(raw);
            case "model" -> vehicle.setModel(raw);
            case "derivative" -> vehicle.setDerivative(raw);
            case "modelYear" -> vehicle.setModelYear(Integer.valueOf(raw));
            case "bodyType" -> vehicle.setBodyType(BodyType.valueOf(raw));
            case "status" -> vehicle.setStatus(VehicleStatus.valueOf(raw));
            case "notes" -> vehicle.setNotes(raw);
            case "engine.fuelType" -> nestedEngine(vehicle).setFuelType(FuelType.valueOf(raw));
            case "engine.displacementCc" -> nestedEngine(vehicle).setDisplacementCc(Integer.valueOf(raw));
            case "engine.cylinders" -> nestedEngine(vehicle).setCylinders(Integer.valueOf(raw));
            case "engine.powerKw" -> nestedEngine(vehicle).setPowerKw(Double.valueOf(raw));
            case "engine.torqueNm" -> nestedEngine(vehicle).setTorqueNm(Double.valueOf(raw));
            case "engine.aspiration" -> nestedEngine(vehicle).setAspiration(Aspiration.valueOf(raw));
            case "engine.hybrid" -> nestedEngine(vehicle).setHybrid(parseBoolean(raw));
            case "engine.phev" -> nestedEngine(vehicle).setPhev(parseBoolean(raw));
            case "transmission.type" -> nestedTransmission(vehicle).setType(TransmissionType.valueOf(raw));
            case "transmission.gears" -> nestedTransmission(vehicle).setGears(Integer.valueOf(raw));
            case "transmission.drivetrain" -> nestedTransmission(vehicle).setDrivetrain(DrivetrainType.valueOf(raw));
            case "performance.zeroToHundredSeconds" ->
                    nestedPerformance(vehicle).setZeroToHundredSeconds(Double.valueOf(raw));
            case "performance.topSpeedKmh" -> nestedPerformance(vehicle).setTopSpeedKmh(Integer.valueOf(raw));
            case "dimensions.lengthMm" -> nestedDimensions(vehicle).setLengthMm(Integer.valueOf(raw));
            case "dimensions.widthMm" -> nestedDimensions(vehicle).setWidthMm(Integer.valueOf(raw));
            case "dimensions.heightMm" -> nestedDimensions(vehicle).setHeightMm(Integer.valueOf(raw));
            case "dimensions.wheelbaseMm" -> nestedDimensions(vehicle).setWheelbaseMm(Integer.valueOf(raw));
            case "dimensions.groundClearanceMm" -> nestedDimensions(vehicle).setGroundClearanceMm(Integer.valueOf(raw));
            case "dimensions.turningCircleM" -> nestedDimensions(vehicle).setTurningCircleM(Double.valueOf(raw));
            case "dimensions.bootLitres" -> nestedDimensions(vehicle).setBootLitres(Integer.valueOf(raw));
            case "dimensions.kerbWeightKg" -> nestedDimensions(vehicle).setKerbWeightKg(Integer.valueOf(raw));
            case "dimensions.seats" -> nestedDimensions(vehicle).setSeats(Integer.valueOf(raw));
            case "towing.towingBrakedKg" -> nestedTowing(vehicle).setTowingBrakedKg(Integer.valueOf(raw));
            case "wheels.tyreSize" -> nestedWheels(vehicle).setTyreSize(raw);
            case "infotainment.infotainmentScreenSizeInches" ->
                    nestedInfotainment(vehicle).setInfotainmentScreenSizeInches(Double.valueOf(raw));
            case "infotainment.speakerCount" -> nestedInfotainment(vehicle).setSpeakerCount(Integer.valueOf(raw));
            case "economy.fuelConsumptionCombined" ->
                    nestedEconomy(vehicle).setFuelConsumptionCombined(Double.valueOf(raw));
            case "economy.fuelTankLitres" -> nestedEconomy(vehicle).setFuelTankLitres(Double.valueOf(raw));
            case "economy.co2Gkm" -> nestedEconomy(vehicle).setCo2Gkm(Double.valueOf(raw));
            case "safety.ncapStars" -> nestedSafety(vehicle).setNcapStars(Integer.valueOf(raw));
            case "safety.airbags" -> nestedSafety(vehicle).setAirbags(Integer.valueOf(raw));
            case "safety.abs" -> nestedSafety(vehicle).setAbs(parseBoolean(raw));
            case "safety.esp" -> nestedSafety(vehicle).setEsp(parseBoolean(raw));
            case "safety.tractionControl" -> nestedSafety(vehicle).setTractionControl(parseBoolean(raw));
            case "safety.aeb" -> nestedSafety(vehicle).setAeb(parseBoolean(raw));
            case "safety.laneAssist" -> nestedSafety(vehicle).setLaneAssist(parseBoolean(raw));
            case "safety.blindSpotMonitoring" -> nestedSafety(vehicle).setBlindSpotMonitoring(parseBoolean(raw));
            case "safety.adaptiveCruiseControl" -> nestedSafety(vehicle).setAdaptiveCruiseControl(parseBoolean(raw));
            case "safety.rearCrossTrafficAlert" -> nestedSafety(vehicle).setRearCrossTrafficAlert(parseBoolean(raw));
            case "features.androidAuto" -> nestedFeatures(vehicle).setAndroidAuto(parseBoolean(raw));
            case "features.appleCarplay" -> nestedFeatures(vehicle).setAppleCarplay(parseBoolean(raw));
            case "features.reverseCamera" -> nestedFeatures(vehicle).setReverseCamera(parseBoolean(raw));
            case "features.parkingSensorsFront" -> nestedFeatures(vehicle).setParkingSensorsFront(parseBoolean(raw));
            case "features.parkingSensorsRear" -> nestedFeatures(vehicle).setParkingSensorsRear(parseBoolean(raw));
            case "features.digitalCluster" -> nestedFeatures(vehicle).setDigitalCluster(parseBoolean(raw));
            case "features.keylessEntry" -> nestedFeatures(vehicle).setKeylessEntry(parseBoolean(raw));
            case "features.pushButtonStart" -> nestedFeatures(vehicle).setPushButtonStart(parseBoolean(raw));
            case "features.wirelessCharging" -> nestedFeatures(vehicle).setWirelessCharging(parseBoolean(raw));
            case "features.climateControl" -> nestedFeatures(vehicle).setClimateControl(parseBoolean(raw));
            case "features.climateControlType" -> nestedFeatures(vehicle).setClimateControlType(ClimateControlType.valueOf(raw));
            case "features.heatedSeats" -> nestedFeatures(vehicle).setHeatedSeats(parseBoolean(raw));
            case "features.electricSeats" -> nestedFeatures(vehicle).setElectricSeats(parseBoolean(raw));
            case "features.sunroof" -> nestedFeatures(vehicle).setSunroof(parseBoolean(raw));
            case "features.premiumAudio" -> nestedFeatures(vehicle).setPremiumAudio(parseBoolean(raw));
            case "ownership.warrantyYears" -> nestedOwnership(vehicle).setWarrantyYears(Integer.valueOf(raw));
            case "ownership.warrantyKm" -> nestedOwnership(vehicle).setWarrantyKm(Integer.valueOf(raw));
            case "ownership.servicePlanYears" -> nestedOwnership(vehicle).setServicePlanYears(Integer.valueOf(raw));
            case "ownership.servicePlanKm" -> nestedOwnership(vehicle).setServicePlanKm(Integer.valueOf(raw));
            case "ownership.serviceIntervalKm" -> nestedOwnership(vehicle).setServiceIntervalKm(Integer.valueOf(raw));
            case "ownership.maintenancePlanYears" -> nestedOwnership(vehicle).setMaintenancePlanYears(Integer.valueOf(raw));
            case "ownership.maintenancePlanKm" -> nestedOwnership(vehicle).setMaintenancePlanKm(Integer.valueOf(raw));
            case "ownership.partsSupportScore" -> nestedOwnership(vehicle).setPartsSupportScore(Integer.valueOf(raw));
            case "ownership.localProduction" -> nestedOwnership(vehicle).setLocalProduction(parseBoolean(raw));
            case RELIABILITY_MANUAL_ESTIMATE_HEADER ->
                    nestedManualScoreOverrides(vehicle).setReliabilityManualEstimate(Double.valueOf(raw));
            case PRESTIGE_MANUAL_HEADER -> nestedManualScoreOverrides(vehicle).setPrestigeScore(Double.valueOf(raw));
            case RELIABILITY_HEURISTIC_HEADER, RELIABILITY_SCORE_HEADER, PRESTIGE_SCORE_HEADER -> {
            }
            case "pricing.listPrice", "pricing.listPriceZar", "pricing.priceZar" -> nestedPricing(vehicle).setListPrice(new BigDecimal(raw));
            case "pricing.dealerOffer", "pricing.dealerOfferZar" -> nestedPricing(vehicle).setDealerOffer(new BigDecimal(raw));
            case "pricing.listPriceDate", "pricing.priceDate" -> nestedPricing(vehicle).setListPriceDate(parseDate(raw));
            case "pricing.dealerOfferDate" -> nestedPricing(vehicle).setDealerOfferDate(parseDate(raw));
            case "source.sourceType" -> nestedSource(vehicle).setSourceType(SourceType.valueOf(raw));
            case "source.sourceName" -> nestedSource(vehicle).setSourceName(raw);
            case "source.sourceUrl" -> nestedSource(vehicle).setSourceUrl(raw);
            default -> throw new IllegalArgumentException("Unknown header: " + header);
        }
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        if (value instanceof LocalDate date) {
            return date.toString();
        }
        if (value instanceof UUID uuid) {
            return uuid.toString();
        }
        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        return value.toString();
    }

    private static Boolean parseBoolean(String raw) {
        if ("true".equalsIgnoreCase(raw)) {
            return true;
        }
        if ("false".equalsIgnoreCase(raw)) {
            return false;
        }
        throw new IllegalArgumentException("Invalid boolean: " + raw);
    }

    private static LocalDate parseDate(String raw) {
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date: " + raw);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static Double parseOptionalDouble(Map<String, String> row, String header) {
        String raw = row.getOrDefault(header, "");
        if (isBlank(raw)) {
            return null;
        }
        return Double.valueOf(raw.trim());
    }

    private static Double derivedReliabilityHeuristic(Vehicle vehicle) {
        if (vehicle.getDerivedMetrics() != null && vehicle.getDerivedMetrics().getReliabilityHeuristic() != null) {
            return vehicle.getDerivedMetrics().getReliabilityHeuristic();
        }
        return null;
    }

    private static Double derivedReliabilityScore(Vehicle vehicle) {
        if (vehicle.getDerivedMetrics() != null && vehicle.getDerivedMetrics().getReliabilityScore() != null) {
            return vehicle.getDerivedMetrics().getReliabilityScore();
        }
        return null;
    }

    private static Double derivedPrestigeScore(Vehicle vehicle) {
        if (vehicle.getDerivedMetrics() != null && vehicle.getDerivedMetrics().getPrestigeScore() != null) {
            return vehicle.getDerivedMetrics().getPrestigeScore();
        }
        return null;
    }

    private static Double manualReliabilityEstimate(Vehicle vehicle) {
        ManualScoreOverrides overrides = vehicle.getManualScoreOverrides();
        return overrides == null ? null : overrides.getReliabilityManualEstimate();
    }

    private static Double manualPrestigeOverride(Vehicle vehicle) {
        ManualScoreOverrides overrides = vehicle.getManualScoreOverrides();
        return overrides == null ? null : overrides.getPrestigeScore();
    }

    private static ManualScoreOverrides nestedManualScoreOverrides(Vehicle vehicle) {
        if (vehicle.getManualScoreOverrides() == null) {
            vehicle.setManualScoreOverrides(new ManualScoreOverrides());
        }
        return vehicle.getManualScoreOverrides();
    }

    private static Engine nestedEngine(Vehicle vehicle) {
        if (vehicle.getEngine() == null) {
            vehicle.setEngine(new Engine());
        }
        return vehicle.getEngine();
    }

    private static Transmission nestedTransmission(Vehicle vehicle) {
        if (vehicle.getTransmission() == null) {
            vehicle.setTransmission(new Transmission());
        }
        return vehicle.getTransmission();
    }

    private static Performance nestedPerformance(Vehicle vehicle) {
        if (vehicle.getPerformance() == null) {
            vehicle.setPerformance(new Performance());
        }
        return vehicle.getPerformance();
    }

    private static Dimensions nestedDimensions(Vehicle vehicle) {
        if (vehicle.getDimensions() == null) {
            vehicle.setDimensions(new Dimensions());
        }
        return vehicle.getDimensions();
    }

    private static Towing nestedTowing(Vehicle vehicle) {
        if (vehicle.getTowing() == null) {
            vehicle.setTowing(new Towing());
        }
        return vehicle.getTowing();
    }

    private static Wheels nestedWheels(Vehicle vehicle) {
        if (vehicle.getWheels() == null) {
            vehicle.setWheels(new Wheels());
        }
        return vehicle.getWheels();
    }

    private static Infotainment nestedInfotainment(Vehicle vehicle) {
        if (vehicle.getInfotainment() == null) {
            vehicle.setInfotainment(new Infotainment());
        }
        return vehicle.getInfotainment();
    }

    private static Economy nestedEconomy(Vehicle vehicle) {
        if (vehicle.getEconomy() == null) {
            vehicle.setEconomy(new Economy());
        }
        return vehicle.getEconomy();
    }

    private static Safety nestedSafety(Vehicle vehicle) {
        if (vehicle.getSafety() == null) {
            vehicle.setSafety(new Safety());
        }
        return vehicle.getSafety();
    }

    private static Features nestedFeatures(Vehicle vehicle) {
        if (vehicle.getFeatures() == null) {
            vehicle.setFeatures(new Features());
        }
        return vehicle.getFeatures();
    }

    private static Ownership nestedOwnership(Vehicle vehicle) {
        if (vehicle.getOwnership() == null) {
            vehicle.setOwnership(new Ownership());
        }
        return vehicle.getOwnership();
    }

    private static Pricing nestedPricing(Vehicle vehicle) {
        if (vehicle.getPricing() == null) {
            vehicle.setPricing(new Pricing());
        }
        return vehicle.getPricing();
    }

    private static Source nestedSource(Vehicle vehicle) {
        if (vehicle.getSource() == null) {
            vehicle.setSource(new Source());
        }
        return vehicle.getSource();
    }
}
