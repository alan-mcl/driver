package za.driver.scoring;

import static za.driver.scoring.ScoringConstants.COMFORT_CLIMATE_WEIGHT;
import static za.driver.scoring.ScoringConstants.COMFORT_ELECTRIC_SEATS_WEIGHT;
import static za.driver.scoring.ScoringConstants.COMFORT_HEATED_SEATS_WEIGHT;
import static za.driver.scoring.ScoringConstants.COMFORT_SEATS_WEIGHT;
import static za.driver.scoring.ScoringConstants.COMFORT_WHEELBASE_WEIGHT;
import static za.driver.scoring.ScoringConstants.DAILY_DRIVER_FUEL_WEIGHT;
import static za.driver.scoring.ScoringConstants.DAILY_DRIVER_LENGTH_WEIGHT;
import static za.driver.scoring.ScoringConstants.DAILY_DRIVER_PARKING_AIDS_WEIGHT;
import static za.driver.scoring.ScoringConstants.DAILY_DRIVER_TURNING_CIRCLE_WEIGHT;
import static za.driver.scoring.ScoringConstants.PERFORMANCE_ACCELERATION_WEIGHT;
import static za.driver.scoring.ScoringConstants.PERFORMANCE_POWER_TO_WEIGHT_WEIGHT;
import static za.driver.scoring.ScoringConstants.PERFORMANCE_TORQUE_TO_WEIGHT_WEIGHT;
import static za.driver.scoring.ScoringConstants.RUNNING_COST_FUEL_WEIGHT;
import static za.driver.scoring.ScoringConstants.RUNNING_COST_MAINTENANCE_PLAN_WEIGHT;
import static za.driver.scoring.ScoringConstants.RUNNING_COST_PARTS_SUPPORT_WEIGHT;
import static za.driver.scoring.ScoringConstants.RUNNING_COST_SERVICE_PLAN_WEIGHT;
import static za.driver.scoring.ScoringConstants.RUNNING_COST_TYRE_COST_WEIGHT;
import static za.driver.scoring.ScoringConstants.RUNNING_COST_WARRANTY_COVERAGE_WEIGHT;
import static za.driver.scoring.ScoringConstants.SAFETY_ABS_WEIGHT;
import static za.driver.scoring.ScoringConstants.SAFETY_AEB_WEIGHT;
import static za.driver.scoring.ScoringConstants.SAFETY_AIRBAGS_WEIGHT;
import static za.driver.scoring.ScoringConstants.SAFETY_BLIND_SPOT_WEIGHT;
import static za.driver.scoring.ScoringConstants.SAFETY_LANE_ASSIST_WEIGHT;
import static za.driver.scoring.ScoringConstants.SAFETY_NCAP_WEIGHT;
import static za.driver.scoring.ScoringConstants.SAFETY_REAR_CROSS_TRAFFIC_WEIGHT;
import static za.driver.scoring.ScoringConstants.SAFETY_STABILITY_WEIGHT;
import static za.driver.scoring.ScoringConstants.TECH_ADAPTIVE_CRUISE_WEIGHT;
import static za.driver.scoring.ScoringConstants.TECH_ANDROID_AUTO_WEIGHT;
import static za.driver.scoring.ScoringConstants.TECH_APPLE_CARPLAY_WEIGHT;
import static za.driver.scoring.ScoringConstants.TECH_DIGITAL_CLUSTER_WEIGHT;
import static za.driver.scoring.ScoringConstants.TECH_KEYLESS_ENTRY_WEIGHT;
import static za.driver.scoring.ScoringConstants.TECH_PUSH_BUTTON_START_WEIGHT;
import static za.driver.scoring.ScoringConstants.TECH_REVERSE_CAMERA_WEIGHT;
import static za.driver.scoring.ScoringConstants.TECH_WIRELESS_CHARGING_WEIGHT;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import za.driver.model.DataQuality;
import za.driver.model.DerivedMetrics;
import za.driver.model.Dimensions;
import za.driver.model.Economy;
import za.driver.model.Engine;
import za.driver.model.Features;
import za.driver.model.Metric;
import za.driver.model.Ownership;
import za.driver.model.Safety;
import za.driver.model.Vehicle;
import za.driver.model.Wheels;
import za.driver.service.BrandReliabilityConfigService;

public class ScoringDataReportService {

    private final BrandReliabilityConfigService brandReliabilityConfigService;

    public ScoringDataReportService() {
        this((BrandReliabilityConfigService) null);
    }

    public ScoringDataReportService(BrandReliabilityConfigService brandReliabilityConfigService) {
        this.brandReliabilityConfigService = brandReliabilityConfigService;
    }

    private ReliabilityCalculator reliabilityCalculator() {
        if (brandReliabilityConfigService != null) {
            return new ReliabilityCalculator(
                    brandReliabilityConfigService.getMergedLookup(),
                    new PowertrainReliabilityScorer());
        }
        return new ReliabilityCalculator();
    }

    private record MissingInput(String fieldPath, double weight) {
    }

    public String generateReport(Vehicle vehicle, ScoringOverrides overrides) {
        if (vehicle == null) {
            return "No vehicle selected.";
        }

        StringBuilder report = new StringBuilder();
        report.append("Data completeness report — ").append(formatVehicleName(vehicle)).append("\n\n");

        Map<String, DataQuality> dataQuality = vehicle.getDataQuality();
        DerivedMetrics metrics = vehicle.getDerivedMetrics();

        appendMetricSection(report, "Safety", scoreFor(metrics, Metric.SAFETY),
                collectSafetyGaps(vehicle), dataQuality);
        appendMetricSection(report, "Running Cost", scoreFor(metrics, Metric.RUNNING_COST),
                collectRunningCostGaps(vehicle), dataQuality);
        appendReliabilitySection(report, vehicle, metrics, overrides);
        appendMetricSection(report, "Comfort", scoreFor(metrics, Metric.COMFORT),
                collectComfortGaps(vehicle), dataQuality);
        appendMetricSection(report, "Performance", scoreFor(metrics, Metric.PERFORMANCE),
                collectPerformanceGaps(vehicle), dataQuality);
        appendMetricSection(report, "Daily Driver", scoreFor(metrics, Metric.DAILY_DRIVER),
                collectDailyDriverGaps(vehicle), dataQuality);
        appendMetricSection(report, "Technology", scoreFor(metrics, Metric.TECHNOLOGY),
                collectTechnologyGaps(vehicle), dataQuality);
        appendManualMetricSection(report, "Prestige",
                manualScore(metrics, overrides, Metric.PRESTIGE),
                overrides != null && overrides.getPrestigeScore() != null);
        appendAwesomenessSection(report, metrics);

        report.append('\n');
        if (metrics != null && metrics.getOverallScore() != null) {
            report.append(String.format(Locale.ROOT, "Overall score: %.1f%n", metrics.getOverallScore()));
            report.append("  Profile weights renormalize over available metrics only.");
        } else {
            report.append("Overall score: not available");
        }

        return report.toString();
    }

    private static void appendMetricSection(
            StringBuilder report,
            String label,
            Double score,
            List<MissingInput> gaps,
            Map<String, DataQuality> dataQuality) {
        if (score != null) {
            report.append(String.format(Locale.ROOT, "%s (score: %.1f)%n", label, score));
        } else {
            report.append(label).append(" (score: not available)\n");
        }

        if (gaps.isEmpty()) {
            report.append("  No missing scoring inputs.\n\n");
            return;
        }

        report.append("  Missing inputs (weight excluded from metric):\n");
        for (MissingInput gap : gaps) {
            report.append(String.format(Locale.ROOT, "    • %s — %.0f%% of %s weight%n",
                    gap.fieldPath(), gap.weight(), label));
            appendDataQualityHint(report, gap.fieldPath(), dataQuality);
        }
        report.append('\n');
    }

    private void appendReliabilitySection(
            StringBuilder report,
            Vehicle vehicle,
            DerivedMetrics metrics,
            ScoringOverrides overrides) {
        Double score = resolvedReliabilityScore(vehicle, metrics, overrides);
        if (score != null) {
            report.append(String.format(Locale.ROOT, "Reliability (score: %.0f)%n", score));
        } else {
            report.append("Reliability (score: not available)\n");
        }

        ReliabilityCalculator.ReliabilityBreakdown breakdown = reliabilityCalculator().breakdown(vehicle);
        if (breakdown.brandScore() != null && breakdown.brandName() != null) {
            report.append(String.format(Locale.ROOT,
                    "  Brand (%s): %d × 50%% = %.1f%n",
                    breakdown.brandName(),
                    breakdown.brandScore(),
                    breakdown.brandScore() * ScoringConstants.RELIABILITY_BRAND_WEIGHT));
        } else if (vehicle.getMake() != null && !vehicle.getMake().isBlank()) {
            report.append("  Brand: not in lookup table (make: ").append(vehicle.getMake().trim()).append(")\n");
        } else {
            report.append("  Brand: missing make\n");
        }

        if (breakdown.powertrainScore() != null) {
            report.append(String.format(Locale.ROOT,
                    "  Powertrain: %.0f × 20%% = %.1f%n",
                    breakdown.powertrainScore(),
                    breakdown.powertrainScore() * ScoringConstants.RELIABILITY_POWERTRAIN_WEIGHT));
            if (breakdown.powertrainExplanation() != null) {
                report.append("    ").append(breakdown.powertrainExplanation()).append('\n');
            }
        } else {
            report.append("  Powertrain: missing engine fuel type or aspiration\n");
        }

        if (breakdown.partsSupportScore() != null) {
            report.append(String.format(Locale.ROOT,
                    "  Parts support: %d × 30%% = %.1f%n",
                    breakdown.partsSupportScore(),
                    breakdown.partsSupportScore() * ScoringConstants.RELIABILITY_PARTS_SUPPORT_WEIGHT));
        } else {
            report.append("  Parts support: ownership.partsSupportScore not set\n");
        }

        Integer confidence = ReliabilityConfidenceUtil.resolve(vehicle, metrics);
        ReliabilityConfidenceBand band = ReliabilityConfidenceBand.fromScore(confidence);
        if (confidence != null && band != null) {
            report.append(String.format(Locale.ROOT,
                    "  Confidence: %d (%s)%n",
                    confidence,
                    band.displayLabel()));
        } else {
            report.append("  Confidence: not available\n");
        }

        if (overrides != null && overrides.getReliabilityManualEstimate() != null) {
            report.append(String.format(Locale.ROOT,
                    "  Manual estimate: %.0f (blended 50/50 with heuristic when both present)%n",
                    overrides.getReliabilityManualEstimate()));
        }
        if (metrics != null && metrics.getReliabilityHeuristic() != null) {
            report.append(String.format(Locale.ROOT,
                    "  Heuristic: %.0f%n",
                    metrics.getReliabilityHeuristic()));
        }
        report.append('\n');
    }

    private static void appendAwesomenessSection(StringBuilder report, DerivedMetrics metrics) {
        Double score = metrics != null ? metrics.getAwesomenessScore() : null;
        if (score != null) {
            report.append(String.format(Locale.ROOT, "Awesomeness (score: %.1f)%n", score));
            report.append("  Combines Prestige (55%), Comfort (15%), Daily Driver (15%), Technology (15%).\n\n");
            return;
        }
        report.append("Awesomeness (score: not available)\n");
        report.append("  Requires at least one component metric (Prestige, Comfort, Daily Driver, Technology).\n\n");
    }

    private static void appendManualMetricSection(
            StringBuilder report,
            String label,
            Double score,
            boolean overrideSet) {
        if (score != null) {
            report.append(String.format(Locale.ROOT, "%s (score: %.1f)%n", label, score));
            report.append("  Manual override set.\n\n");
            return;
        }

        report.append(label).append(" (score: not available)\n");
        if (!overrideSet) {
            report.append("  • Manual override not set — metric excluded from overall score\n\n");
        }
    }

    private static void appendDataQualityHint(
            StringBuilder report,
            String fieldPath,
            Map<String, DataQuality> dataQuality) {
        if (dataQuality == null) {
            return;
        }
        DataQuality quality = dataQuality.get(fieldPath);
        if (quality == DataQuality.MISSING) {
            report.append("      flagged MISSING at import\n");
        }
    }

    private static List<MissingInput> collectSafetyGaps(Vehicle vehicle) {
        List<MissingInput> gaps = new ArrayList<>();
        Safety safety = vehicle.getSafety();
        if (safety == null) {
            gaps.add(new MissingInput("safety", 100.0));
            return gaps;
        }

        if (safety.getNcapStars() == null) {
            gaps.add(new MissingInput("safety.ncapStars", SAFETY_NCAP_WEIGHT));
        }
        if (safety.getAirbags() == null) {
            gaps.add(new MissingInput("safety.airbags", SAFETY_AIRBAGS_WEIGHT));
        }
        if (safety.getAbs() == null) {
            gaps.add(new MissingInput("safety.abs", SAFETY_ABS_WEIGHT));
        }
        if (safety.getEsp() == null && safety.getTractionControl() == null) {
            gaps.add(new MissingInput("safety.esp / safety.tractionControl", SAFETY_STABILITY_WEIGHT));
        }
        if (safety.getAeb() == null) {
            gaps.add(new MissingInput("safety.aeb", SAFETY_AEB_WEIGHT));
        }
        if (safety.getLaneAssist() == null) {
            gaps.add(new MissingInput("safety.laneAssist", SAFETY_LANE_ASSIST_WEIGHT));
        }
        if (safety.getBlindSpotMonitoring() == null) {
            gaps.add(new MissingInput("safety.blindSpotMonitoring", SAFETY_BLIND_SPOT_WEIGHT));
        }
        if (safety.getRearCrossTrafficAlert() == null) {
            gaps.add(new MissingInput("safety.rearCrossTrafficAlert", SAFETY_REAR_CROSS_TRAFFIC_WEIGHT));
        }
        return gaps;
    }

    private static List<MissingInput> collectRunningCostGaps(Vehicle vehicle) {
        List<MissingInput> gaps = new ArrayList<>();

        Economy economy = vehicle.getEconomy();
        if (economy == null || economy.getFuelConsumptionCombined() == null) {
            gaps.add(new MissingInput("economy.fuelConsumptionCombined", RUNNING_COST_FUEL_WEIGHT));
        }

        Ownership ownership = vehicle.getOwnership();
        boolean warrantyCoverageAvailable = ownership != null
                && (ownership.getWarrantyYears() != null || ownership.getWarrantyKm() != null);
        if (!warrantyCoverageAvailable) {
            gaps.add(new MissingInput("ownership.warrantyYears / ownership.warrantyKm",
                    RUNNING_COST_WARRANTY_COVERAGE_WEIGHT));
        }

        boolean servicePlanCoverageAvailable = ownership != null
                && (ownership.getServicePlanYears() != null || ownership.getServicePlanKm() != null);
        if (!servicePlanCoverageAvailable) {
            gaps.add(new MissingInput("ownership.servicePlanYears / ownership.servicePlanKm",
                    RUNNING_COST_SERVICE_PLAN_WEIGHT));
        }

        boolean maintenancePlanCoverageAvailable = ownership != null
                && (ownership.getMaintenancePlanYears() != null || ownership.getMaintenancePlanKm() != null);
        if (!maintenancePlanCoverageAvailable) {
            gaps.add(new MissingInput("ownership.maintenancePlanYears / ownership.maintenancePlanKm",
                    RUNNING_COST_MAINTENANCE_PLAN_WEIGHT));
        }

        if (ownership == null || ownership.getPartsSupportScore() == null) {
            gaps.add(new MissingInput("ownership.partsSupportScore", RUNNING_COST_PARTS_SUPPORT_WEIGHT));
        }

        Wheels wheels = vehicle.getWheels();
        if (wheels == null || wheels.getTyreSize() == null || wheels.getTyreSize().isBlank()) {
            gaps.add(new MissingInput("wheels.tyreSize", RUNNING_COST_TYRE_COST_WEIGHT));
        }
        return gaps;
    }

    private static List<MissingInput> collectComfortGaps(Vehicle vehicle) {
        List<MissingInput> gaps = new ArrayList<>();
        Features features = vehicle.getFeatures();
        if (features == null) {
            gaps.add(new MissingInput("features.climateControl", COMFORT_CLIMATE_WEIGHT));
            gaps.add(new MissingInput("features.heatedSeats", COMFORT_HEATED_SEATS_WEIGHT));
            gaps.add(new MissingInput("features.electricSeats", COMFORT_ELECTRIC_SEATS_WEIGHT));
        } else {
            if (features.getClimateControl() == null) {
                gaps.add(new MissingInput("features.climateControl", COMFORT_CLIMATE_WEIGHT));
            }
            if (features.getHeatedSeats() == null) {
                gaps.add(new MissingInput("features.heatedSeats", COMFORT_HEATED_SEATS_WEIGHT));
            }
            if (features.getElectricSeats() == null) {
                gaps.add(new MissingInput("features.electricSeats", COMFORT_ELECTRIC_SEATS_WEIGHT));
            }
        }

        Dimensions dimensions = vehicle.getDimensions();
        if (dimensions == null || dimensions.getSeats() == null) {
            gaps.add(new MissingInput("dimensions.seats", COMFORT_SEATS_WEIGHT));
        }
        if (dimensions == null || dimensions.getWheelbaseMm() == null) {
            gaps.add(new MissingInput("dimensions.wheelbaseMm", COMFORT_WHEELBASE_WEIGHT));
        }
        return gaps;
    }

    private static List<MissingInput> collectPerformanceGaps(Vehicle vehicle) {
        List<MissingInput> gaps = new ArrayList<>();
        Engine engine = vehicle.getEngine();
        if (engine == null || engine.getPowerKw() == null) {
            gaps.add(new MissingInput("engine.powerKw",
                    PERFORMANCE_ACCELERATION_WEIGHT + PERFORMANCE_POWER_TO_WEIGHT_WEIGHT));
        }
        if (engine == null || engine.getTorqueNm() == null) {
            gaps.add(new MissingInput("engine.torqueNm", PERFORMANCE_TORQUE_TO_WEIGHT_WEIGHT));
        }

        Dimensions dimensions = vehicle.getDimensions();
        boolean kerbWeightAvailable = dimensions != null
                && dimensions.getKerbWeightKg() != null
                && dimensions.getKerbWeightKg() > 0;
        if (!kerbWeightAvailable) {
            gaps.add(new MissingInput("dimensions.kerbWeightKg",
                    PERFORMANCE_ACCELERATION_WEIGHT
                            + PERFORMANCE_POWER_TO_WEIGHT_WEIGHT
                            + PERFORMANCE_TORQUE_TO_WEIGHT_WEIGHT));
        }
        return gaps;
    }

    private static List<MissingInput> collectDailyDriverGaps(Vehicle vehicle) {
        List<MissingInput> gaps = new ArrayList<>();

        Dimensions dimensions = vehicle.getDimensions();
        if (dimensions == null || dimensions.getTurningCircleM() == null) {
            gaps.add(new MissingInput("dimensions.turningCircleM", DAILY_DRIVER_TURNING_CIRCLE_WEIGHT));
        }
        if (dimensions == null || dimensions.getLengthMm() == null) {
            gaps.add(new MissingInput("dimensions.lengthMm", DAILY_DRIVER_LENGTH_WEIGHT));
        }

        Economy economy = vehicle.getEconomy();
        if (economy == null || economy.getFuelConsumptionCombined() == null) {
            gaps.add(new MissingInput("economy.fuelConsumptionCombined", DAILY_DRIVER_FUEL_WEIGHT));
        }

        Features features = vehicle.getFeatures();
        if (features == null) {
            gaps.add(new MissingInput("features.parkingSensorsFront", DAILY_DRIVER_PARKING_AIDS_WEIGHT / 3.0));
            gaps.add(new MissingInput("features.parkingSensorsRear", DAILY_DRIVER_PARKING_AIDS_WEIGHT / 3.0));
            gaps.add(new MissingInput("features.reverseCamera", DAILY_DRIVER_PARKING_AIDS_WEIGHT / 3.0));
        } else {
            if (features.getParkingSensorsFront() == null) {
                gaps.add(new MissingInput("features.parkingSensorsFront", DAILY_DRIVER_PARKING_AIDS_WEIGHT / 3.0));
            }
            if (features.getParkingSensorsRear() == null) {
                gaps.add(new MissingInput("features.parkingSensorsRear", DAILY_DRIVER_PARKING_AIDS_WEIGHT / 3.0));
            }
            if (features.getReverseCamera() == null) {
                gaps.add(new MissingInput("features.reverseCamera", DAILY_DRIVER_PARKING_AIDS_WEIGHT / 3.0));
            }
        }
        return gaps;
    }

    private static List<MissingInput> collectTechnologyGaps(Vehicle vehicle) {
        List<MissingInput> gaps = new ArrayList<>();
        Features features = vehicle.getFeatures();
        if (features == null) {
            gaps.add(new MissingInput("features.androidAuto", TECH_ANDROID_AUTO_WEIGHT));
            gaps.add(new MissingInput("features.appleCarplay", TECH_APPLE_CARPLAY_WEIGHT));
            gaps.add(new MissingInput("features.reverseCamera", TECH_REVERSE_CAMERA_WEIGHT));
            gaps.add(new MissingInput("features.digitalCluster", TECH_DIGITAL_CLUSTER_WEIGHT));
            gaps.add(new MissingInput("features.wirelessCharging", TECH_WIRELESS_CHARGING_WEIGHT));
            gaps.add(new MissingInput("features.keylessEntry", TECH_KEYLESS_ENTRY_WEIGHT));
            gaps.add(new MissingInput("features.pushButtonStart", TECH_PUSH_BUTTON_START_WEIGHT));
        } else {
            if (features.getAndroidAuto() == null) {
                gaps.add(new MissingInput("features.androidAuto", TECH_ANDROID_AUTO_WEIGHT));
            }
            if (features.getAppleCarplay() == null) {
                gaps.add(new MissingInput("features.appleCarplay", TECH_APPLE_CARPLAY_WEIGHT));
            }
            if (features.getReverseCamera() == null) {
                gaps.add(new MissingInput("features.reverseCamera", TECH_REVERSE_CAMERA_WEIGHT));
            }
            if (features.getDigitalCluster() == null) {
                gaps.add(new MissingInput("features.digitalCluster", TECH_DIGITAL_CLUSTER_WEIGHT));
            }
            if (features.getWirelessCharging() == null) {
                gaps.add(new MissingInput("features.wirelessCharging", TECH_WIRELESS_CHARGING_WEIGHT));
            }
            if (features.getKeylessEntry() == null) {
                gaps.add(new MissingInput("features.keylessEntry", TECH_KEYLESS_ENTRY_WEIGHT));
            }
            if (features.getPushButtonStart() == null) {
                gaps.add(new MissingInput("features.pushButtonStart", TECH_PUSH_BUTTON_START_WEIGHT));
            }
        }

        Safety safety = vehicle.getSafety();
        if (safety == null || safety.getAdaptiveCruiseControl() == null) {
            gaps.add(new MissingInput("safety.adaptiveCruiseControl", TECH_ADAPTIVE_CRUISE_WEIGHT));
        }
        return gaps;
    }

    private static Double scoreFor(DerivedMetrics metrics, Metric metric) {
        if (metrics == null) {
            return null;
        }
        return switch (metric) {
            case SAFETY -> metrics.getSafetyScore();
            case RUNNING_COST -> metrics.getRunningCostScore();
            case RELIABILITY -> metrics.getReliabilityScore();
            case COMFORT -> metrics.getComfortScore();
            case PERFORMANCE -> metrics.getPerformanceScore();
            case DAILY_DRIVER -> metrics.getDailyDriverScore();
            case TECHNOLOGY -> metrics.getTechnologyScore();
            case PRESTIGE -> metrics.getPrestigeScore();
            case AWESOMENESS -> metrics.getAwesomenessScore();
        };
    }

    private static Double manualScore(DerivedMetrics metrics, ScoringOverrides overrides, Metric metric) {
        if (overrides != null) {
            Double overrideScore = metric == Metric.RELIABILITY
                    ? overrides.getReliabilityManualEstimate()
                    : overrides.getPrestigeScore();
            if (overrideScore != null) {
                return overrideScore;
            }
        }
        return scoreFor(metrics, metric);
    }

    private Double resolvedReliabilityScore(Vehicle vehicle, DerivedMetrics metrics, ScoringOverrides overrides) {
        if (metrics != null && metrics.getReliabilityScore() != null) {
            return metrics.getReliabilityScore();
        }
        Double manualEstimate = overrides != null ? overrides.getReliabilityManualEstimate() : null;
        if (manualEstimate == null && vehicle != null && vehicle.getManualScoreOverrides() != null) {
            manualEstimate = vehicle.getManualScoreOverrides().getReliabilityManualEstimate();
        }
        Double heuristic = metrics != null ? metrics.getReliabilityHeuristic() : null;
        if (heuristic == null) {
            heuristic = reliabilityCalculator().calculate(vehicle);
        }
        return ReliabilityScoreBlender.blend(heuristic, manualEstimate);
    }

    private static String formatVehicleName(Vehicle vehicle) {
        String make = vehicle.getMake() != null ? vehicle.getMake() : "";
        String model = vehicle.getModel() != null ? vehicle.getModel() : "";
        String derivative = vehicle.getDerivative() != null ? vehicle.getDerivative() : "";
        String name = (make + " " + model).trim();
        if (!derivative.isBlank()) {
            name = name.isEmpty() ? derivative : name + " " + derivative;
        }
        return name.isBlank() ? "Unnamed vehicle" : name.trim();
    }
}
