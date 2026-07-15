package za.driver.scoring;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import za.driver.model.Aspiration;
import za.driver.model.BodyType;
import za.driver.model.Dimensions;
import za.driver.model.Economy;
import za.driver.model.Engine;
import za.driver.model.Features;
import za.driver.model.FuelType;
import za.driver.model.Metric;
import za.driver.model.Ownership;
import za.driver.model.Pricing;
import za.driver.model.Safety;
import za.driver.model.ScoringProfile;
import za.driver.model.ScoringWeight;
import za.driver.model.Source;
import za.driver.model.SourceType;
import za.driver.model.Transmission;
import za.driver.model.TransmissionType;
import za.driver.model.Vehicle;
import za.driver.model.VehicleStatus;
import za.driver.model.Wheels;

public final class ScoringTestFixtures {

    static final UUID VEHICLE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    static final UUID PROFILE_ID = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");

    private ScoringTestFixtures() {
    }

    public static Vehicle fullVehicle() {
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
        vehicle.setTransmission(transmission);

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

        Wheels wheels = new Wheels();
        wheels.setTyreSize("195/60 R16");
        vehicle.setWheels(wheels);

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
        features.setHeatedSeats(false);
        features.setElectricSeats(false);
        vehicle.setFeatures(features);

        Ownership ownership = new Ownership();
        ownership.setWarrantyYears(3);
        ownership.setWarrantyKm(100000);
        ownership.setServicePlanYears(3);
        ownership.setServicePlanKm(100000);
        ownership.setServiceIntervalKm(15000);
        ownership.setPartsSupportScore(90);
        ownership.setLocalProduction(true);
        vehicle.setOwnership(ownership);

        Pricing pricing = new Pricing();
        pricing.setListPriceZar(new BigDecimal("350000"));
        pricing.setPriceDate(LocalDate.of(2026, 6, 17));
        vehicle.setPricing(pricing);

        Source source = new Source();
        source.setSourceType(SourceType.WEBSITE);
        source.setSourceName("Toyota SA");
        source.setSourceUrl("https://www.toyota.co.za");
        source.setImportedDate(LocalDateTime.of(2026, 6, 17, 10, 30, 0));
        vehicle.setSource(source);

        return vehicle;
    }

    static Vehicle partialVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7"));
        vehicle.setMake("Volkswagen");
        vehicle.setModel("Polo");
        vehicle.setStatus(VehicleStatus.CANDIDATE);
        return vehicle;
    }

    public static ScoringProfile familyFocusedProfile() {
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

    static ScoringProfile equalWeightProfile() {
        ScoringProfile profile = new ScoringProfile();
        profile.setId(UUID.randomUUID());
        profile.setName("Equal");

        List<ScoringWeight> weights = new ArrayList<>();
        for (Metric metric : Metric.PROFILE_WEIGHT_METRICS) {
            weights.add(weight(metric, 20.0));
        }
        profile.setWeights(weights);
        return profile;
    }

    static ScoringProfile emptyProfile() {
        ScoringProfile profile = new ScoringProfile();
        profile.setId(UUID.randomUUID());
        profile.setName("Empty");
        profile.setWeights(new ArrayList<>());
        return profile;
    }

    private static ScoringWeight weight(Metric metric, double value) {
        ScoringWeight scoringWeight = new ScoringWeight();
        scoringWeight.setMetric(metric);
        scoringWeight.setWeight(value);
        return scoringWeight;
    }
}
