package za.driver.scoring;

import java.util.List;
import java.util.function.Predicate;

import za.driver.model.Dimensions;
import za.driver.model.Economy;
import za.driver.model.Engine;
import za.driver.model.Features;
import za.driver.model.Ownership;
import za.driver.model.Performance;
import za.driver.model.Safety;
import za.driver.model.Transmission;
import za.driver.model.Vehicle;
import za.driver.model.Wheels;

public final class ScoringInputCoverage {

    private static final List<Predicate<Vehicle>> FIELD_CHECKS = List.of(
            ScoringInputCoverage::hasNcapStars,
            ScoringInputCoverage::hasAirbags,
            ScoringInputCoverage::hasAbs,
            ScoringInputCoverage::hasStability,
            ScoringInputCoverage::hasAeb,
            ScoringInputCoverage::hasLaneAssist,
            ScoringInputCoverage::hasBlindSpotMonitoring,
            ScoringInputCoverage::hasRearCrossTrafficAlert,
            ScoringInputCoverage::hasFuelConsumptionCombined,
            ScoringInputCoverage::hasWarrantyCoverage,
            ScoringInputCoverage::hasServicePlanCoverage,
            ScoringInputCoverage::hasPartsSupportScore,
            ScoringInputCoverage::hasTyreSize,
            ScoringInputCoverage::hasClimateControl,
            ScoringInputCoverage::hasHeatedSeats,
            ScoringInputCoverage::hasElectricSeats,
            ScoringInputCoverage::hasSeats,
            ScoringInputCoverage::hasWheelbaseMm,
            ScoringInputCoverage::hasPowerKw,
            ScoringInputCoverage::hasTorqueNm,
            ScoringInputCoverage::hasKerbWeightKg,
            ScoringInputCoverage::hasZeroToHundredSeconds,
            ScoringInputCoverage::hasTransmissionType,
            ScoringInputCoverage::hasTurningCircleM,
            ScoringInputCoverage::hasLengthMm,
            ScoringInputCoverage::hasParkingSensorsFront,
            ScoringInputCoverage::hasParkingSensorsRear,
            ScoringInputCoverage::hasReverseCamera,
            ScoringInputCoverage::hasAndroidAuto,
            ScoringInputCoverage::hasAppleCarplay,
            ScoringInputCoverage::hasDigitalCluster,
            ScoringInputCoverage::hasWirelessCharging,
            ScoringInputCoverage::hasKeylessEntry,
            ScoringInputCoverage::hasPushButtonStart,
            ScoringInputCoverage::hasAdaptiveCruiseControl);

    private ScoringInputCoverage() {
    }

    public static int totalFieldCount() {
        return FIELD_CHECKS.size();
    }

    public static int populatedFieldCount(Vehicle vehicle) {
        if (vehicle == null) {
            return 0;
        }
        int count = 0;
        for (Predicate<Vehicle> check : FIELD_CHECKS) {
            if (check.test(vehicle)) {
                count++;
            }
        }
        return count;
    }

    public static double completenessPercent(Vehicle vehicle) {
        if (vehicle == null || FIELD_CHECKS.isEmpty()) {
            return 0.0;
        }
        return populatedFieldCount(vehicle) * 100.0 / FIELD_CHECKS.size();
    }

    private static boolean hasNcapStars(Vehicle vehicle) {
        Safety safety = vehicle.getSafety();
        return safety != null && safety.getNcapStars() != null;
    }

    private static boolean hasAirbags(Vehicle vehicle) {
        Safety safety = vehicle.getSafety();
        return safety != null && safety.getAirbags() != null;
    }

    private static boolean hasAbs(Vehicle vehicle) {
        Safety safety = vehicle.getSafety();
        return safety != null && safety.getAbs() != null;
    }

    private static boolean hasStability(Vehicle vehicle) {
        Safety safety = vehicle.getSafety();
        return safety != null
                && (safety.getEsp() != null || safety.getTractionControl() != null);
    }

    private static boolean hasAeb(Vehicle vehicle) {
        Safety safety = vehicle.getSafety();
        return safety != null && safety.getAeb() != null;
    }

    private static boolean hasLaneAssist(Vehicle vehicle) {
        Safety safety = vehicle.getSafety();
        return safety != null && safety.getLaneAssist() != null;
    }

    private static boolean hasBlindSpotMonitoring(Vehicle vehicle) {
        Safety safety = vehicle.getSafety();
        return safety != null && safety.getBlindSpotMonitoring() != null;
    }

    private static boolean hasRearCrossTrafficAlert(Vehicle vehicle) {
        Safety safety = vehicle.getSafety();
        return safety != null && safety.getRearCrossTrafficAlert() != null;
    }

    private static boolean hasFuelConsumptionCombined(Vehicle vehicle) {
        Economy economy = vehicle.getEconomy();
        return economy != null && economy.getFuelConsumptionCombined() != null;
    }

    private static boolean hasWarrantyCoverage(Vehicle vehicle) {
        Ownership ownership = vehicle.getOwnership();
        return ownership != null
                && (ownership.getWarrantyYears() != null || ownership.getWarrantyKm() != null);
    }

    private static boolean hasServicePlanCoverage(Vehicle vehicle) {
        Ownership ownership = vehicle.getOwnership();
        return ownership != null
                && (ownership.getServicePlanYears() != null || ownership.getServicePlanKm() != null);
    }

    private static boolean hasPartsSupportScore(Vehicle vehicle) {
        Ownership ownership = vehicle.getOwnership();
        return ownership != null && ownership.getPartsSupportScore() != null;
    }

    private static boolean hasTyreSize(Vehicle vehicle) {
        Wheels wheels = vehicle.getWheels();
        return wheels != null && wheels.getTyreSize() != null && !wheels.getTyreSize().isBlank();
    }

    private static boolean hasClimateControl(Vehicle vehicle) {
        Features features = vehicle.getFeatures();
        return features != null && features.getClimateControl() != null;
    }

    private static boolean hasHeatedSeats(Vehicle vehicle) {
        Features features = vehicle.getFeatures();
        return features != null && features.getHeatedSeats() != null;
    }

    private static boolean hasElectricSeats(Vehicle vehicle) {
        Features features = vehicle.getFeatures();
        return features != null && features.getElectricSeats() != null;
    }

    private static boolean hasSeats(Vehicle vehicle) {
        Dimensions dimensions = vehicle.getDimensions();
        return dimensions != null && dimensions.getSeats() != null;
    }

    private static boolean hasWheelbaseMm(Vehicle vehicle) {
        Dimensions dimensions = vehicle.getDimensions();
        return dimensions != null && dimensions.getWheelbaseMm() != null;
    }

    private static boolean hasPowerKw(Vehicle vehicle) {
        Engine engine = vehicle.getEngine();
        return engine != null && engine.getPowerKw() != null;
    }

    private static boolean hasTorqueNm(Vehicle vehicle) {
        Engine engine = vehicle.getEngine();
        return engine != null && engine.getTorqueNm() != null;
    }

    private static boolean hasKerbWeightKg(Vehicle vehicle) {
        Dimensions dimensions = vehicle.getDimensions();
        return dimensions != null
                && dimensions.getKerbWeightKg() != null
                && dimensions.getKerbWeightKg() > 0;
    }

    private static boolean hasZeroToHundredSeconds(Vehicle vehicle) {
        Performance performance = vehicle.getPerformance();
        return performance != null && performance.getZeroToHundredSeconds() != null;
    }

    private static boolean hasTransmissionType(Vehicle vehicle) {
        Transmission transmission = vehicle.getTransmission();
        return transmission != null && transmission.getType() != null;
    }

    private static boolean hasTurningCircleM(Vehicle vehicle) {
        Dimensions dimensions = vehicle.getDimensions();
        return dimensions != null && dimensions.getTurningCircleM() != null;
    }

    private static boolean hasLengthMm(Vehicle vehicle) {
        Dimensions dimensions = vehicle.getDimensions();
        return dimensions != null && dimensions.getLengthMm() != null;
    }

    private static boolean hasParkingSensorsFront(Vehicle vehicle) {
        Features features = vehicle.getFeatures();
        return features != null && features.getParkingSensorsFront() != null;
    }

    private static boolean hasParkingSensorsRear(Vehicle vehicle) {
        Features features = vehicle.getFeatures();
        return features != null && features.getParkingSensorsRear() != null;
    }

    private static boolean hasReverseCamera(Vehicle vehicle) {
        Features features = vehicle.getFeatures();
        return features != null && features.getReverseCamera() != null;
    }

    private static boolean hasAndroidAuto(Vehicle vehicle) {
        Features features = vehicle.getFeatures();
        return features != null && features.getAndroidAuto() != null;
    }

    private static boolean hasAppleCarplay(Vehicle vehicle) {
        Features features = vehicle.getFeatures();
        return features != null && features.getAppleCarplay() != null;
    }

    private static boolean hasDigitalCluster(Vehicle vehicle) {
        Features features = vehicle.getFeatures();
        return features != null && features.getDigitalCluster() != null;
    }

    private static boolean hasWirelessCharging(Vehicle vehicle) {
        Features features = vehicle.getFeatures();
        return features != null && features.getWirelessCharging() != null;
    }

    private static boolean hasKeylessEntry(Vehicle vehicle) {
        Features features = vehicle.getFeatures();
        return features != null && features.getKeylessEntry() != null;
    }

    private static boolean hasPushButtonStart(Vehicle vehicle) {
        Features features = vehicle.getFeatures();
        return features != null && features.getPushButtonStart() != null;
    }

    private static boolean hasAdaptiveCruiseControl(Vehicle vehicle) {
        Safety safety = vehicle.getSafety();
        return safety != null && safety.getAdaptiveCruiseControl() != null;
    }
}
