package za.driver.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import za.driver.model.Aspiration;
import za.driver.model.BodyType;
import za.driver.model.ClimateControlType;
import za.driver.model.DerivedMetrics;
import za.driver.model.Dimensions;
import za.driver.model.DrivetrainType;
import za.driver.model.Economy;
import za.driver.model.Engine;
import za.driver.model.Features;
import za.driver.model.FuelType;
import za.driver.model.Infotainment;
import za.driver.model.Metric;
import za.driver.model.Ownership;
import za.driver.model.Performance;
import za.driver.model.Pricing;
import za.driver.model.Safety;
import za.driver.model.ScoringProfile;
import za.driver.model.ScoringWeight;
import za.driver.model.Source;
import za.driver.model.SourceType;
import za.driver.model.TestDrive;
import za.driver.model.Transmission;
import za.driver.model.TransmissionType;
import za.driver.model.Towing;
import za.driver.model.Vehicle;
import za.driver.model.VehicleStatus;
import za.driver.model.Wheels;

final class TestFixtures {

    static final UUID VEHICLE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    static final UUID PROFILE_ID = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
    static final UUID TEST_DRIVE_ID = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");
    static final UUID OTHER_VEHICLE_ID = UUID.fromString("6ba7b812-9dad-11d1-80b4-00c04fd430c8");
    static final UUID OTHER_TEST_DRIVE_ID = UUID.fromString("6ba7b813-9dad-11d1-80b4-00c04fd430c8");

    private TestFixtures() {
    }

    static Vehicle fullVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(VEHICLE_ID);
        vehicle.setMake("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setDerivative("1.8 XS");
        vehicle.setModelYear(2024);
        vehicle.setBodyType(BodyType.SEDAN);
        vehicle.setStatus(VehicleStatus.CANDIDATE);

        Engine engine = new Engine();
        engine.setFuelType(FuelType.PETROL);
        engine.setDisplacementCc(1798);
        engine.setCylinders(4);
        engine.setPowerKw(103.0);
        engine.setTorqueNm(173.0);
        engine.setAspiration(Aspiration.NATURALLY_ASPIRATED);
        engine.setHybrid(false);
        engine.setPhev(false);
        vehicle.setEngine(engine);

        Transmission transmission = new Transmission();
        transmission.setType(TransmissionType.CVT);
        transmission.setGears(7);
        transmission.setDrivetrain(DrivetrainType.FWD);
        vehicle.setTransmission(transmission);

        Performance performance = new Performance();
        performance.setZeroToHundredSeconds(10.2);
        performance.setTopSpeedKmh(190);
        vehicle.setPerformance(performance);

        Dimensions dimensions = new Dimensions();
        dimensions.setLengthMm(4630);
        dimensions.setWidthMm(1780);
        dimensions.setHeightMm(1435);
        dimensions.setWheelbaseMm(2700);
        dimensions.setGroundClearanceMm(140);
        dimensions.setTurningCircleM(10.8);
        dimensions.setBootLitres(470);
        dimensions.setKerbWeightKg(1300);
        dimensions.setSeats(5);
        vehicle.setDimensions(dimensions);

        Towing towing = new Towing();
        towing.setTowingBrakedKg(750);
        vehicle.setTowing(towing);

        Wheels wheels = new Wheels();
        wheels.setTyreSize("205/55 R16");
        vehicle.setWheels(wheels);

        Infotainment infotainment = new Infotainment();
        infotainment.setInfotainmentScreenSizeInches(8.0);
        infotainment.setSpeakerCount(6);
        vehicle.setInfotainment(infotainment);

        Economy economy = new Economy();
        economy.setFuelConsumptionCombined(6.5);
        economy.setFuelTankLitres(50.0);
        economy.setCo2Gkm(152.0);
        vehicle.setEconomy(economy);

        Safety safety = new Safety();
        safety.setNcapStars(5);
        safety.setAirbags(7);
        safety.setAbs(true);
        safety.setEsp(true);
        safety.setTractionControl(true);
        safety.setAeb(true);
        safety.setLaneAssist(true);
        safety.setBlindSpotMonitoring(false);
        safety.setAdaptiveCruiseControl(true);
        safety.setRearCrossTrafficAlert(true);
        vehicle.setSafety(safety);

        Features features = new Features();
        features.setAndroidAuto(true);
        features.setAppleCarplay(true);
        features.setReverseCamera(true);
        features.setParkingSensorsFront(false);
        features.setParkingSensorsRear(true);
        features.setDigitalCluster(false);
        features.setKeylessEntry(true);
        features.setPushButtonStart(true);
        features.setWirelessCharging(false);
        features.setClimateControl(true);
        features.setClimateControlType(ClimateControlType.SINGLE_ZONE_AUTO);
        features.setHeatedSeats(false);
        features.setElectricSeats(false);
        features.setSunroof(false);
        features.setPremiumAudio(true);
        vehicle.setFeatures(features);

        Ownership ownership = new Ownership();
        ownership.setWarrantyYears(3);
        ownership.setWarrantyKm(100000);
        ownership.setServicePlanYears(3);
        ownership.setServicePlanKm(100000);
        ownership.setServiceIntervalKm(15000);
        ownership.setMaintenancePlanYears(5);
        ownership.setMaintenancePlanKm(90000);
        ownership.setPartsSupportScore(92);
        ownership.setLocalProduction(true);
        vehicle.setOwnership(ownership);

        Pricing pricing = new Pricing();
        pricing.setListPrice(new BigDecimal("350000"));
        pricing.setListPriceDate(LocalDate.of(2026, 6, 17));
        vehicle.setPricing(pricing);

        Source source = new Source();
        source.setSourceType(SourceType.WEBSITE);
        source.setSourceName("Toyota SA");
        source.setSourceUrl("https://www.toyota.co.za");
        source.setImportedDate(LocalDateTime.of(2026, 6, 17, 10, 30, 0));
        vehicle.setSource(source);

        DerivedMetrics metrics = new DerivedMetrics();
        metrics.setSafetyScore(85.0);
        metrics.setRunningCostScore(72.0);
        metrics.setReliabilityScore(90.0);
        metrics.setComfortScore(68.0);
        metrics.setPerformanceScore(55.0);
        metrics.setDailyDriverScore(80.0);
        metrics.setTechnologyScore(60.0);
        metrics.setPrestigeScore(50.0);
        metrics.setOverallScore(74.5);
        vehicle.setDerivedMetrics(metrics);

        return vehicle;
    }

    static Vehicle minimalVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7"));
        vehicle.setMake("Volkswagen");
        vehicle.setModel("Polo");
        vehicle.setStatus(VehicleStatus.CANDIDATE);
        return vehicle;
    }

    static ScoringProfile scoringProfile() {
        ScoringProfile profile = new ScoringProfile();
        profile.setId(PROFILE_ID);
        profile.setName("Family Focused");

        List<ScoringWeight> weights = new ArrayList<>();
        weights.add(weight(Metric.SAFETY, 25.0));
        weights.add(weight(Metric.RUNNING_COST, 15.0));
        weights.add(weight(Metric.RELIABILITY, 15.0));
        weights.add(weight(Metric.PERFORMANCE, 5.0));
        weights.add(weight(Metric.AWESOMENESS, 40.0));
        profile.setWeights(weights);

        List<ScoringWeight> aggregateComponents = new ArrayList<>();
        aggregateComponents.add(weight(Metric.PRESTIGE, 55.0));
        aggregateComponents.add(weight(Metric.COMFORT, 15.0));
        aggregateComponents.add(weight(Metric.DAILY_DRIVER, 15.0));
        aggregateComponents.add(weight(Metric.TECHNOLOGY, 15.0));
        profile.setAggregateName("Awesomeness");
        profile.setAggregateComponents(aggregateComponents);

        return profile;
    }

    static TestDrive testDrive() {
        TestDrive testDrive = new TestDrive();
        testDrive.setId(TEST_DRIVE_ID);
        testDrive.setVehicleId(VEHICLE_ID);
        testDrive.setDriveDate(LocalDate.of(2026, 5, 20));
        testDrive.setComfortRating(8);
        testDrive.setVisibilityRating(7);
        testDrive.setHandlingRating(9);
        testDrive.setSpouseApprovalRating(8);
        testDrive.setOverallImpression(8);
        testDrive.setNotes("Smooth ride, good visibility.");
        return testDrive;
    }

    static TestDrive testDriveForOtherVehicle() {
        TestDrive testDrive = new TestDrive();
        testDrive.setId(OTHER_TEST_DRIVE_ID);
        testDrive.setVehicleId(OTHER_VEHICLE_ID);
        testDrive.setDriveDate(LocalDate.of(2026, 5, 21));
        testDrive.setComfortRating(6);
        testDrive.setVisibilityRating(6);
        testDrive.setHandlingRating(7);
        testDrive.setSpouseApprovalRating(5);
        testDrive.setOverallImpression(6);
        testDrive.setNotes("Firm suspension.");
        return testDrive;
    }

    private static ScoringWeight weight(Metric metric, double value) {
        ScoringWeight scoringWeight = new ScoringWeight();
        scoringWeight.setMetric(metric);
        scoringWeight.setWeight(value);
        return scoringWeight;
    }
}
