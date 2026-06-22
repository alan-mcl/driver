package za.driver.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.fasterxml.jackson.core.JsonProcessingException;

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
import za.driver.model.ManualScoreOverrides;
import za.driver.model.Ownership;
import za.driver.model.Performance;
import za.driver.model.Pricing;
import za.driver.model.ScoringProfile;
import za.driver.model.Safety;
import za.driver.model.Source;
import za.driver.model.SourceType;
import za.driver.model.Towing;
import za.driver.model.Transmission;
import za.driver.model.TransmissionType;
import za.driver.model.Vehicle;
import za.driver.model.VehicleStatus;
import za.driver.model.Wheels;
import za.driver.persistence.JsonStore;
import za.driver.scoring.MetricScores;
import za.driver.scoring.ReliabilityConfidenceUtil;
import za.driver.scoring.ScoringDataReportService;
import za.driver.scoring.ScoringOverrides;
import za.driver.scoring.TopWeightedMetrics;

public class VehicleDetailPanel extends JPanel {

    private final ScoringDataReportService scoringDataReportService;
    private ScoringProfile activeProfile;
    private List<Metric> topMetrics = List.of();

    private final StarRatingPanel overallStarPanel = new StarRatingPanel("Overall");
    private final List<StarRatingPanel> metricStarPanels = new ArrayList<>();

    private Vehicle currentVehicle;
    private boolean persisted;
    private String baselineJson;

    private final JTextField makeField = new JTextField(20);
    private final JTextField modelField = new JTextField(20);
    private final JTextField derivativeField = new JTextField(20);
    private final JTextField modelYearField = new JTextField(8);
    private final JComboBox<BodyType> bodyTypeCombo = enumCombo(BodyType.values());
    private final JComboBox<VehicleStatus> statusCombo = new JComboBox<>(VehicleStatus.values());

    private final JComboBox<FuelType> fuelTypeCombo = enumCombo(FuelType.values());
    private final JTextField displacementField = new JTextField(8);
    private final JTextField cylindersField = new JTextField(8);
    private final JTextField powerKwField = new JTextField(8);
    private final JTextField torqueNmField = new JTextField(8);
    private final JComboBox<Aspiration> aspirationCombo = enumCombo(Aspiration.values());
    private final JCheckBox hybridCheck = new JCheckBox("Hybrid");
    private final JCheckBox phevCheck = new JCheckBox("PHEV");

    private final JComboBox<TransmissionType> transmissionTypeCombo = enumCombo(TransmissionType.values());
    private final JTextField gearsField = new JTextField(8);
    private final JComboBox<DrivetrainType> drivetrainCombo = enumCombo(DrivetrainType.values());
    private final JTextField zeroToHundredField = new JTextField(8);
    private final JTextField topSpeedField = new JTextField(8);

    private final JTextField lengthField = new JTextField(8);
    private final JTextField widthField = new JTextField(8);
    private final JTextField heightField = new JTextField(8);
    private final JTextField wheelbaseField = new JTextField(8);
    private final JTextField groundClearanceField = new JTextField(8);
    private final JTextField turningCircleField = new JTextField(8);
    private final JTextField bootField = new JTextField(8);
    private final JTextField kerbWeightField = new JTextField(8);
    private final JTextField seatsField = new JTextField(8);
    private final JTextField towingBrakedField = new JTextField(8);

    private final JTextField tyreSizeField = new JTextField(20);

    private final JTextField screenSizeField = new JTextField(8);
    private final JTextField speakerCountField = new JTextField(8);

    private final JTextField fuelConsumptionField = new JTextField(8);
    private final JTextField fuelTankField = new JTextField(8);
    private final JTextField co2Field = new JTextField(8);

    private final JTextField ncapStarsField = new JTextField(8);
    private final JTextField airbagsField = new JTextField(8);
    private final JCheckBox absCheck = new JCheckBox("ABS");
    private final JCheckBox espCheck = new JCheckBox("ESP");
    private final JCheckBox tractionCheck = new JCheckBox("Traction control");
    private final JCheckBox aebCheck = new JCheckBox("AEB");
    private final JCheckBox laneAssistCheck = new JCheckBox("Lane assist");
    private final JCheckBox blindSpotCheck = new JCheckBox("Blind spot monitoring");
    private final JCheckBox adaptiveCruiseCheck = new JCheckBox("Adaptive cruise");
    private final JCheckBox rearCrossTrafficCheck = new JCheckBox("Rear cross-traffic alert");

    private final JCheckBox androidAutoCheck = new JCheckBox("Android Auto");
    private final JCheckBox appleCarplayCheck = new JCheckBox("Apple CarPlay");
    private final JCheckBox reverseCameraCheck = new JCheckBox("Reverse camera");
    private final JCheckBox parkingFrontCheck = new JCheckBox("Parking sensors front");
    private final JCheckBox parkingRearCheck = new JCheckBox("Parking sensors rear");
    private final JCheckBox digitalClusterCheck = new JCheckBox("Digital cluster");
    private final JCheckBox keylessEntryCheck = new JCheckBox("Keyless entry");
    private final JCheckBox pushButtonStartCheck = new JCheckBox("Push-button start");
    private final JCheckBox wirelessChargingCheck = new JCheckBox("Wireless charging");
    private final JCheckBox climateControlCheck = new JCheckBox("Climate control");
    private final JComboBox<ClimateControlType> climateControlTypeCombo = enumCombo(ClimateControlType.values());
    private final JCheckBox heatedSeatsCheck = new JCheckBox("Heated seats");
    private final JCheckBox electricSeatsCheck = new JCheckBox("Electric seats");
    private final JCheckBox sunroofCheck = new JCheckBox("Sunroof");
    private final JCheckBox premiumAudioCheck = new JCheckBox("Premium audio");

    private final JTextField warrantyYearsField = new JTextField(8);
    private final JTextField warrantyKmField = new JTextField(8);
    private final JTextField servicePlanYearsField = new JTextField(8);
    private final JTextField servicePlanKmField = new JTextField(8);
    private final JTextField serviceIntervalField = new JTextField(8);
    private final JTextField maintenancePlanYearsField = new JTextField(8);
    private final JTextField maintenancePlanKmField = new JTextField(8);
    private final JTextField partsSupportScoreField = new JTextField(8);
    private final JCheckBox localProductionCheck = new JCheckBox("Locally produced");

    private final JTextField priceZarField = new JTextField(12);
    private final JTextField priceDateField = new JTextField(12);

    private final JComboBox<SourceType> sourceTypeCombo = enumCombo(SourceType.values());
    private final JTextField sourceNameField = new JTextField(20);
    private final JTextField sourceUrlField = new JTextField(30);
    private final JLabel importedDateLabel = new JLabel("-");

    private final JTextField safetyScoreField = readOnlyField();
    private final JTextField runningCostScoreField = readOnlyField();
    private final JTextField reliabilityScoreField = readOnlyField();
    private final JTextField reliabilityConfidenceField = readOnlyField();
    private final JTextField comfortScoreField = readOnlyField();
    private final JTextField performanceScoreField = readOnlyField();
    private final JTextField dailyDriverScoreField = readOnlyField();
    private final JTextField technologyScoreField = readOnlyField();
    private final JTextField awesomenessScoreField = readOnlyField();
    private final JTextField prestigeScoreField = readOnlyField();
    private final JTextField overallScoreField = readOnlyField();
    private final JTextField scorePer100kField = readOnlyField();
    private final JSpinner reliabilityOverrideSpinner = overrideSpinner();
    private final JSpinner prestigeOverrideSpinner = overrideSpinner();
    private final JTextArea notesArea = new JTextArea(8, 40);
    private final JTextArea dataReportArea = new JTextArea();

    public VehicleDetailPanel(ScoringDataReportService scoringDataReportService, ScoringProfile activeProfile) {
        super(new BorderLayout());
        this.scoringDataReportService = scoringDataReportService;
        this.activeProfile = activeProfile;
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.addTab("General", wrapTopLeft(buildGeneralPanel()));
        tabs.addTab("Engine", wrapTopLeft(buildEnginePanel()));
        tabs.addTab("Transmission", wrapTopLeft(buildTransmissionPanel()));
        tabs.addTab("Dimensions", wrapTopLeft(buildDimensionsPanel()));
        tabs.addTab("Wheels", wrapTopLeft(buildWheelsPanel()));
        tabs.addTab("Infotainment", wrapTopLeft(buildInfotainmentPanel()));
        tabs.addTab("Economy", wrapTopLeft(buildEconomyPanel()));
        tabs.addTab("Safety", wrapTopLeft(buildSafetyPanel()));
        tabs.addTab("Features", wrapTopLeft(buildFeaturesPanel()));
        tabs.addTab("Ownership", wrapTopLeft(buildOwnershipPanel()));
        tabs.addTab("Pricing", wrapTopLeft(buildPricingPanel()));
        tabs.addTab("Source", wrapTopLeft(buildSourcePanel()));
        tabs.addTab("Scores", wrapTopLeft(buildScoresPanel()));
        tabs.addTab("Notes", buildNotesPanel());
        tabs.addTab("Data Report", buildDataReportPanel());
        add(tabs, BorderLayout.CENTER);
        setActiveProfile(activeProfile);
    }

    public void setActiveProfile(ScoringProfile profile) {
        this.activeProfile = profile;
        this.topMetrics = TopWeightedMetrics.topN(profile, 5);
        for (int i = 0; i < metricStarPanels.size(); i++) {
            if (i < topMetrics.size()) {
                metricStarPanels.get(i).setLabel(WeightEditor.displayName(topMetrics.get(i)));
            } else {
                metricStarPanels.get(i).setLabel("");
                metricStarPanels.get(i).setScore(null);
            }
        }
        if (currentVehicle != null) {
            updateStarRatings(currentVehicle);
        }
    }

    public void loadVehicle(Vehicle vehicle, boolean persisted) {
        this.currentVehicle = vehicle;
        this.persisted = persisted;
        clearForm();
        if (vehicle == null) {
            baselineJson = null;
            return;
        }

        makeField.setText(nullToEmpty(vehicle.getMake()));
        modelField.setText(nullToEmpty(vehicle.getModel()));
        derivativeField.setText(nullToEmpty(vehicle.getDerivative()));
        modelYearField.setText(integerToString(vehicle.getModelYear()));
        setComboSelection(bodyTypeCombo, vehicle.getBodyType());
        setComboSelection(statusCombo, vehicle.getStatus());

        Engine engine = vehicle.getEngine();
        if (engine != null) {
            setComboSelection(fuelTypeCombo, engine.getFuelType());
            displacementField.setText(integerToString(engine.getDisplacementCc()));
            cylindersField.setText(integerToString(engine.getCylinders()));
            powerKwField.setText(doubleToString(engine.getPowerKw()));
            torqueNmField.setText(doubleToString(engine.getTorqueNm()));
            setComboSelection(aspirationCombo, engine.getAspiration());
            hybridCheck.setSelected(Boolean.TRUE.equals(engine.getHybrid()));
            phevCheck.setSelected(Boolean.TRUE.equals(engine.getPhev()));
        }

        Transmission transmission = vehicle.getTransmission();
        if (transmission != null) {
            setComboSelection(transmissionTypeCombo, transmission.getType());
            gearsField.setText(integerToString(transmission.getGears()));
            setComboSelection(drivetrainCombo, transmission.getDrivetrain());
        }

        Performance performance = vehicle.getPerformance();
        if (performance != null) {
            zeroToHundredField.setText(doubleToString(performance.getZeroToHundredSeconds()));
            topSpeedField.setText(integerToString(performance.getTopSpeedKmh()));
        }

        Dimensions dimensions = vehicle.getDimensions();
        if (dimensions != null) {
            lengthField.setText(integerToString(dimensions.getLengthMm()));
            widthField.setText(integerToString(dimensions.getWidthMm()));
            heightField.setText(integerToString(dimensions.getHeightMm()));
            wheelbaseField.setText(integerToString(dimensions.getWheelbaseMm()));
            groundClearanceField.setText(integerToString(dimensions.getGroundClearanceMm()));
            turningCircleField.setText(doubleToString(dimensions.getTurningCircleM()));
            bootField.setText(integerToString(dimensions.getBootLitres()));
            kerbWeightField.setText(integerToString(dimensions.getKerbWeightKg()));
            seatsField.setText(integerToString(dimensions.getSeats()));
        }

        Towing towing = vehicle.getTowing();
        if (towing != null) {
            towingBrakedField.setText(integerToString(towing.getTowingBrakedKg()));
        }

        Wheels wheels = vehicle.getWheels();
        if (wheels != null) {
            tyreSizeField.setText(nullToEmpty(wheels.getTyreSize()));
        }

        Infotainment infotainment = vehicle.getInfotainment();
        if (infotainment != null) {
            screenSizeField.setText(doubleToString(infotainment.getInfotainmentScreenSizeInches()));
            speakerCountField.setText(integerToString(infotainment.getSpeakerCount()));
        }

        Economy economy = vehicle.getEconomy();
        if (economy != null) {
            fuelConsumptionField.setText(doubleToString(economy.getFuelConsumptionCombined()));
            fuelTankField.setText(doubleToString(economy.getFuelTankLitres()));
            co2Field.setText(doubleToString(economy.getCo2Gkm()));
        }

        Safety safety = vehicle.getSafety();
        if (safety != null) {
            ncapStarsField.setText(integerToString(safety.getNcapStars()));
            airbagsField.setText(integerToString(safety.getAirbags()));
            absCheck.setSelected(Boolean.TRUE.equals(safety.getAbs()));
            espCheck.setSelected(Boolean.TRUE.equals(safety.getEsp()));
            tractionCheck.setSelected(Boolean.TRUE.equals(safety.getTractionControl()));
            aebCheck.setSelected(Boolean.TRUE.equals(safety.getAeb()));
            laneAssistCheck.setSelected(Boolean.TRUE.equals(safety.getLaneAssist()));
            blindSpotCheck.setSelected(Boolean.TRUE.equals(safety.getBlindSpotMonitoring()));
            adaptiveCruiseCheck.setSelected(Boolean.TRUE.equals(safety.getAdaptiveCruiseControl()));
            rearCrossTrafficCheck.setSelected(Boolean.TRUE.equals(safety.getRearCrossTrafficAlert()));
        }

        Features features = vehicle.getFeatures();
        if (features != null) {
            androidAutoCheck.setSelected(Boolean.TRUE.equals(features.getAndroidAuto()));
            appleCarplayCheck.setSelected(Boolean.TRUE.equals(features.getAppleCarplay()));
            reverseCameraCheck.setSelected(Boolean.TRUE.equals(features.getReverseCamera()));
            parkingFrontCheck.setSelected(Boolean.TRUE.equals(features.getParkingSensorsFront()));
            parkingRearCheck.setSelected(Boolean.TRUE.equals(features.getParkingSensorsRear()));
            digitalClusterCheck.setSelected(Boolean.TRUE.equals(features.getDigitalCluster()));
            keylessEntryCheck.setSelected(Boolean.TRUE.equals(features.getKeylessEntry()));
            pushButtonStartCheck.setSelected(Boolean.TRUE.equals(features.getPushButtonStart()));
            wirelessChargingCheck.setSelected(Boolean.TRUE.equals(features.getWirelessCharging()));
            climateControlCheck.setSelected(Boolean.TRUE.equals(features.getClimateControl()));
            setComboSelection(climateControlTypeCombo, features.getClimateControlType());
            heatedSeatsCheck.setSelected(Boolean.TRUE.equals(features.getHeatedSeats()));
            electricSeatsCheck.setSelected(Boolean.TRUE.equals(features.getElectricSeats()));
            sunroofCheck.setSelected(Boolean.TRUE.equals(features.getSunroof()));
            premiumAudioCheck.setSelected(Boolean.TRUE.equals(features.getPremiumAudio()));
        }

        Ownership ownership = vehicle.getOwnership();
        if (ownership != null) {
            warrantyYearsField.setText(integerToString(ownership.getWarrantyYears()));
            warrantyKmField.setText(integerToString(ownership.getWarrantyKm()));
            servicePlanYearsField.setText(integerToString(ownership.getServicePlanYears()));
            servicePlanKmField.setText(integerToString(ownership.getServicePlanKm()));
            serviceIntervalField.setText(integerToString(ownership.getServiceIntervalKm()));
            maintenancePlanYearsField.setText(integerToString(ownership.getMaintenancePlanYears()));
            maintenancePlanKmField.setText(integerToString(ownership.getMaintenancePlanKm()));
            partsSupportScoreField.setText(integerToString(ownership.getPartsSupportScore()));
            localProductionCheck.setSelected(Boolean.TRUE.equals(ownership.getLocalProduction()));
        }

        Pricing pricing = vehicle.getPricing();
        if (pricing != null) {
            priceZarField.setText(pricing.getPriceZar() != null ? pricing.getPriceZar().toPlainString() : "");
            priceDateField.setText(pricing.getPriceDate() != null ? pricing.getPriceDate().toString() : "");
        }

        Source source = vehicle.getSource();
        if (source != null) {
            setComboSelection(sourceTypeCombo, source.getSourceType());
            sourceNameField.setText(nullToEmpty(source.getSourceName()));
            sourceUrlField.setText(nullToEmpty(source.getSourceUrl()));
            importedDateLabel.setText(source.getImportedDate() != null ? source.getImportedDate().toString() : "-");
        }

        setScores(vehicle);
        loadOverrideSpinners(vehicle);
        updateDataReport(vehicle);
        notesArea.setText(nullToEmpty(vehicle.getNotes()));
        captureBaseline();
    }

    public boolean isDirty() {
        if (currentVehicle == null || baselineJson == null) {
            return false;
        }
        try {
            return !baselineJson.equals(JsonStore.createMapper().writeValueAsString(buildVehicle()));
        } catch (JsonProcessingException ex) {
            return false;
        }
    }

    private void captureBaseline() {
        if (currentVehicle == null) {
            baselineJson = null;
            return;
        }
        try {
            baselineJson = JsonStore.createMapper().writeValueAsString(buildVehicle());
        } catch (JsonProcessingException ex) {
            baselineJson = null;
        }
    }

    private void loadOverrideSpinners(Vehicle vehicle) {
        ManualScoreOverrides overrides = vehicle != null ? vehicle.getManualScoreOverrides() : null;
        if (overrides != null && overrides.getReliabilityScore() != null) {
            reliabilityOverrideSpinner.setValue(overrides.getReliabilityScore().intValue());
        } else {
            reliabilityOverrideSpinner.setValue(-1);
        }
        if (overrides != null && overrides.getPrestigeScore() != null) {
            prestigeOverrideSpinner.setValue(overrides.getPrestigeScore().intValue());
        } else {
            prestigeOverrideSpinner.setValue(-1);
        }
    }

    public Vehicle buildVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(currentVehicle != null ? currentVehicle.getId() : UUID.randomUUID());
        vehicle.setDataQuality(currentVehicle != null ? currentVehicle.getDataQuality() : null);

        vehicle.setMake(trimToNull(makeField.getText()));
        vehicle.setModel(trimToNull(modelField.getText()));
        vehicle.setDerivative(trimToNull(derivativeField.getText()));
        vehicle.setModelYear(parseInteger(modelYearField.getText()));
        vehicle.setBodyType((BodyType) bodyTypeCombo.getSelectedItem());
        vehicle.setStatus((VehicleStatus) statusCombo.getSelectedItem());
        vehicle.setNotes(trimToNull(notesArea.getText()));

        if (hasEngineData()) {
            Engine engine = new Engine();
            engine.setFuelType((FuelType) fuelTypeCombo.getSelectedItem());
            engine.setDisplacementCc(parseInteger(displacementField.getText()));
            engine.setCylinders(parseInteger(cylindersField.getText()));
            engine.setPowerKw(parseDouble(powerKwField.getText()));
            engine.setTorqueNm(parseDouble(torqueNmField.getText()));
            engine.setAspiration((Aspiration) aspirationCombo.getSelectedItem());
            engine.setHybrid(hybridCheck.isSelected());
            engine.setPhev(phevCheck.isSelected());
            vehicle.setEngine(engine);
        }

        if (hasTransmissionData()) {
            Transmission transmission = new Transmission();
            transmission.setType((TransmissionType) transmissionTypeCombo.getSelectedItem());
            transmission.setGears(parseInteger(gearsField.getText()));
            transmission.setDrivetrain((DrivetrainType) drivetrainCombo.getSelectedItem());
            vehicle.setTransmission(transmission);
        }

        if (hasPerformanceData()) {
            Performance performance = new Performance();
            performance.setZeroToHundredSeconds(parseDouble(zeroToHundredField.getText()));
            performance.setTopSpeedKmh(parseInteger(topSpeedField.getText()));
            vehicle.setPerformance(performance);
        }

        if (hasDimensionsData()) {
            Dimensions dimensions = new Dimensions();
            dimensions.setLengthMm(parseInteger(lengthField.getText()));
            dimensions.setWidthMm(parseInteger(widthField.getText()));
            dimensions.setHeightMm(parseInteger(heightField.getText()));
            dimensions.setWheelbaseMm(parseInteger(wheelbaseField.getText()));
            dimensions.setGroundClearanceMm(parseInteger(groundClearanceField.getText()));
            dimensions.setTurningCircleM(parseDouble(turningCircleField.getText()));
            dimensions.setBootLitres(parseInteger(bootField.getText()));
            dimensions.setKerbWeightKg(parseInteger(kerbWeightField.getText()));
            dimensions.setSeats(parseInteger(seatsField.getText()));
            vehicle.setDimensions(dimensions);
        }

        if (hasTowingData()) {
            Towing towing = new Towing();
            towing.setTowingBrakedKg(parseInteger(towingBrakedField.getText()));
            vehicle.setTowing(towing);
        }

        if (hasWheelsData()) {
            Wheels wheels = new Wheels();
            wheels.setTyreSize(trimToNull(tyreSizeField.getText()));
            vehicle.setWheels(wheels);
        }

        if (hasInfotainmentData()) {
            Infotainment infotainment = new Infotainment();
            infotainment.setInfotainmentScreenSizeInches(parseDouble(screenSizeField.getText()));
            infotainment.setSpeakerCount(parseInteger(speakerCountField.getText()));
            vehicle.setInfotainment(infotainment);
        }

        if (hasEconomyData()) {
            Economy economy = new Economy();
            economy.setFuelConsumptionCombined(parseDouble(fuelConsumptionField.getText()));
            economy.setFuelTankLitres(parseDouble(fuelTankField.getText()));
            economy.setCo2Gkm(parseDouble(co2Field.getText()));
            vehicle.setEconomy(economy);
        }

        if (hasSafetyData()) {
            Safety safety = new Safety();
            safety.setNcapStars(parseInteger(ncapStarsField.getText()));
            safety.setAirbags(parseInteger(airbagsField.getText()));
            safety.setAbs(absCheck.isSelected());
            safety.setEsp(espCheck.isSelected());
            safety.setTractionControl(tractionCheck.isSelected());
            safety.setAeb(aebCheck.isSelected());
            safety.setLaneAssist(laneAssistCheck.isSelected());
            safety.setBlindSpotMonitoring(blindSpotCheck.isSelected());
            safety.setAdaptiveCruiseControl(adaptiveCruiseCheck.isSelected());
            safety.setRearCrossTrafficAlert(rearCrossTrafficCheck.isSelected());
            vehicle.setSafety(safety);
        }

        if (hasFeaturesData()) {
            Features features = new Features();
            features.setAndroidAuto(androidAutoCheck.isSelected());
            features.setAppleCarplay(appleCarplayCheck.isSelected());
            features.setReverseCamera(reverseCameraCheck.isSelected());
            features.setParkingSensorsFront(parkingFrontCheck.isSelected());
            features.setParkingSensorsRear(parkingRearCheck.isSelected());
            features.setDigitalCluster(digitalClusterCheck.isSelected());
            features.setKeylessEntry(keylessEntryCheck.isSelected());
            features.setPushButtonStart(pushButtonStartCheck.isSelected());
            features.setWirelessCharging(wirelessChargingCheck.isSelected());
            features.setClimateControl(climateControlCheck.isSelected());
            features.setClimateControlType((ClimateControlType) climateControlTypeCombo.getSelectedItem());
            features.setHeatedSeats(heatedSeatsCheck.isSelected());
            features.setElectricSeats(electricSeatsCheck.isSelected());
            features.setSunroof(sunroofCheck.isSelected());
            features.setPremiumAudio(premiumAudioCheck.isSelected());
            vehicle.setFeatures(features);
        }

        if (hasOwnershipData()) {
            Ownership ownership = new Ownership();
            ownership.setWarrantyYears(parseInteger(warrantyYearsField.getText()));
            ownership.setWarrantyKm(parseInteger(warrantyKmField.getText()));
            ownership.setServicePlanYears(parseInteger(servicePlanYearsField.getText()));
            ownership.setServicePlanKm(parseInteger(servicePlanKmField.getText()));
            ownership.setServiceIntervalKm(parseInteger(serviceIntervalField.getText()));
            ownership.setMaintenancePlanYears(parseInteger(maintenancePlanYearsField.getText()));
            ownership.setMaintenancePlanKm(parseInteger(maintenancePlanKmField.getText()));
            ownership.setPartsSupportScore(parseInteger(partsSupportScoreField.getText()));
            ownership.setLocalProduction(localProductionCheck.isSelected());
            vehicle.setOwnership(ownership);
        }

        if (hasPricingData()) {
            Pricing pricing = new Pricing();
            pricing.setPriceZar(parseBigDecimal(priceZarField.getText()));
            pricing.setPriceDate(parseDate(priceDateField.getText()));
            vehicle.setPricing(pricing);
        }

        if (hasSourceData()) {
            Source source = new Source();
            source.setSourceType((SourceType) sourceTypeCombo.getSelectedItem());
            source.setSourceName(trimToNull(sourceNameField.getText()));
            source.setSourceUrl(trimToNull(sourceUrlField.getText()));
            if (currentVehicle != null && currentVehicle.getSource() != null) {
                source.setImportedDate(currentVehicle.getSource().getImportedDate());
            }
            vehicle.setSource(source);
        }

        applyManualScoreOverrides(vehicle);
        return vehicle;
    }

    private void applyManualScoreOverrides(Vehicle vehicle) {
        Double reliability = spinnerValue(reliabilityOverrideSpinner);
        Double prestige = spinnerValue(prestigeOverrideSpinner);
        if (reliability == null && prestige == null) {
            vehicle.setManualScoreOverrides(null);
            return;
        }
        ManualScoreOverrides overrides = new ManualScoreOverrides();
        overrides.setReliabilityScore(reliability);
        overrides.setPrestigeScore(prestige);
        vehicle.setManualScoreOverrides(overrides);
    }

    public ScoringOverrides getScoringOverrides() {
        Double reliability = spinnerValue(reliabilityOverrideSpinner);
        Double prestige = spinnerValue(prestigeOverrideSpinner);
        if (reliability == null && prestige == null) {
            return ScoringOverrides.none();
        }
        return ScoringOverrides.of(reliability, prestige);
    }

    public void setScores(Vehicle vehicle) {
        DerivedMetrics metrics = vehicle != null ? vehicle.getDerivedMetrics() : null;
        safetyScoreField.setText(formatScore(metrics != null ? metrics.getSafetyScore() : null));
        runningCostScoreField.setText(formatScore(metrics != null ? metrics.getRunningCostScore() : null));
        reliabilityScoreField.setText(formatScore(MetricScores.displayScore(vehicle, metrics, Metric.RELIABILITY)));
        reliabilityConfidenceField.setText(ReliabilityConfidenceUtil.format(vehicle, metrics));
        comfortScoreField.setText(formatScore(metrics != null ? metrics.getComfortScore() : null));
        performanceScoreField.setText(formatScore(metrics != null ? metrics.getPerformanceScore() : null));
        dailyDriverScoreField.setText(formatScore(metrics != null ? metrics.getDailyDriverScore() : null));
        technologyScoreField.setText(formatScore(metrics != null ? metrics.getTechnologyScore() : null));
        awesomenessScoreField.setText(formatScore(metrics != null ? metrics.getAwesomenessScore() : null));
        prestigeScoreField.setText(formatScore(metrics != null ? metrics.getPrestigeScore() : null));
        overallScoreField.setText(formatScore(metrics != null ? metrics.getOverallScore() : null));
        scorePer100kField.setText(formatScore(metrics != null ? metrics.getScorePer100k() : null));
        updateStarRatings(vehicle);
    }

    private void updateStarRatings(Vehicle vehicle) {
        DerivedMetrics metrics = vehicle != null ? vehicle.getDerivedMetrics() : null;
        overallStarPanel.setScore(metrics != null ? metrics.getOverallScore() : null);
        for (int i = 0; i < metricStarPanels.size(); i++) {
            if (i < topMetrics.size()) {
                metricStarPanels.get(i).setScore(
                        MetricScores.displayScore(vehicle, metrics, topMetrics.get(i)));
            } else {
                metricStarPanels.get(i).setScore(null);
            }
        }
    }

    public void updateDataReport(Vehicle vehicle) {
        if (vehicle == null) {
            dataReportArea.setText("");
            return;
        }
        dataReportArea.setText(scoringDataReportService.generateReport(vehicle, getScoringOverrides()));
    }

    public boolean isNewVehicle() {
        return !persisted;
    }

    public boolean validateForSave() {
        return trimToNull(makeField.getText()) != null && trimToNull(modelField.getText()) != null;
    }

    private JPanel buildGeneralPanel() {
        JPanel panel = formPanel();
        addRow(panel, 0, "Make", makeField);
        addRow(panel, 1, "Model", modelField);
        addRow(panel, 2, "Derivative", derivativeField);
        addRow(panel, 3, "Model year", modelYearField);
        addRow(panel, 4, "Body type", bodyTypeCombo);
        addRow(panel, 5, "Status", statusCombo);

        GridBagConstraints separatorConstraints = new GridBagConstraints();
        separatorConstraints.gridx = 0;
        separatorConstraints.gridy = 6;
        separatorConstraints.gridwidth = 2;
        separatorConstraints.anchor = GridBagConstraints.WEST;
        separatorConstraints.insets = new Insets(12, 8, 4, 8);
        JLabel separator = new JLabel("Score summary");
        separator.setFont(separator.getFont().deriveFont(Font.BOLD));
        panel.add(separator, separatorConstraints);

        JPanel scoreSection = new JPanel();
        scoreSection.setLayout(new BoxLayout(scoreSection, BoxLayout.Y_AXIS));
        scoreSection.setOpaque(false);
        overallStarPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        scoreSection.add(overallStarPanel);
        for (int i = 0; i < 5; i++) {
            StarRatingPanel starPanel = new StarRatingPanel("");
            metricStarPanels.add(starPanel);
            starPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            scoreSection.add(Box.createVerticalStrut(10));
            scoreSection.add(starPanel);
        }

        GridBagConstraints scoreSectionConstraints = new GridBagConstraints();
        scoreSectionConstraints.gridx = 0;
        scoreSectionConstraints.gridy = 7;
        scoreSectionConstraints.gridwidth = 2;
        scoreSectionConstraints.anchor = GridBagConstraints.WEST;
        scoreSectionConstraints.fill = GridBagConstraints.HORIZONTAL;
        scoreSectionConstraints.weightx = 1.0;
        scoreSectionConstraints.insets = new Insets(4, 8, 8, 8);
        panel.add(scoreSection, scoreSectionConstraints);
        return panel;
    }

    private JPanel buildEnginePanel() {
        JPanel panel = formPanel();
        addRow(panel, 0, "Fuel type", fuelTypeCombo);
        addRow(panel, 1, "Displacement (cc)", displacementField);
        addRow(panel, 2, "Cylinders", cylindersField);
        addRow(panel, 3, "Power (kW)", powerKwField);
        addRow(panel, 4, "Torque (Nm)", torqueNmField);
        addRow(panel, 5, "Aspiration", aspirationCombo);
        addRow(panel, 6, "", hybridCheck);
        addRow(panel, 7, "", phevCheck);
        return panel;
    }

    private JPanel buildTransmissionPanel() {
        JPanel panel = formPanel();
        addRow(panel, 0, "Type", transmissionTypeCombo);
        addRow(panel, 1, "Gears", gearsField);
        addRow(panel, 2, "Drivetrain", drivetrainCombo);
        addRow(panel, 3, "0–100 km/h (s)", zeroToHundredField);
        addRow(panel, 4, "Top speed (km/h)", topSpeedField);
        return panel;
    }

    private JPanel buildDimensionsPanel() {
        JPanel panel = formPanel();
        addRow(panel, 0, "Length (mm)", lengthField);
        addRow(panel, 1, "Width (mm)", widthField);
        addRow(panel, 2, "Height (mm)", heightField);
        addRow(panel, 3, "Wheelbase (mm)", wheelbaseField);
        addRow(panel, 4, "Ground clearance (mm)", groundClearanceField);
        addRow(panel, 5, "Turning circle (m)", turningCircleField);
        addRow(panel, 6, "Boot (L)", bootField);
        addRow(panel, 7, "Kerb weight (kg)", kerbWeightField);
        addRow(panel, 8, "Seats", seatsField);
        addRow(panel, 9, "Towing braked (kg)", towingBrakedField);
        return panel;
    }

    private JPanel buildWheelsPanel() {
        JPanel panel = formPanel();
        addRow(panel, 0, "Tyre size", tyreSizeField);
        return panel;
    }

    private JPanel buildInfotainmentPanel() {
        JPanel panel = formPanel();
        addRow(panel, 0, "Screen size (in)", screenSizeField);
        addRow(panel, 1, "Speakers", speakerCountField);
        return panel;
    }

    private JPanel buildEconomyPanel() {
        JPanel panel = formPanel();
        addRow(panel, 0, "Fuel consumption combined", fuelConsumptionField);
        addRow(panel, 1, "Fuel tank (L)", fuelTankField);
        addRow(panel, 2, "CO2 (g/km)", co2Field);
        return panel;
    }

    private JPanel buildSafetyPanel() {
        JPanel panel = formPanel();
        addRow(panel, 0, "NCAP stars", ncapStarsField);
        addRow(panel, 1, "Airbags", airbagsField);
        addRow(panel, 2, "", absCheck);
        addRow(panel, 3, "", espCheck);
        addRow(panel, 4, "", tractionCheck);
        addRow(panel, 5, "", aebCheck);
        addRow(panel, 6, "", laneAssistCheck);
        addRow(panel, 7, "", blindSpotCheck);
        addRow(panel, 8, "", adaptiveCruiseCheck);
        addRow(panel, 9, "", rearCrossTrafficCheck);
        return panel;
    }

    private JPanel buildFeaturesPanel() {
        JPanel panel = formPanel();
        addRow(panel, 0, "", androidAutoCheck);
        addRow(panel, 1, "", appleCarplayCheck);
        addRow(panel, 2, "", reverseCameraCheck);
        addRow(panel, 3, "", parkingFrontCheck);
        addRow(panel, 4, "", parkingRearCheck);
        addRow(panel, 5, "", digitalClusterCheck);
        addRow(panel, 6, "", keylessEntryCheck);
        addRow(panel, 7, "", pushButtonStartCheck);
        addRow(panel, 8, "", wirelessChargingCheck);
        addRow(panel, 9, "", climateControlCheck);
        addRow(panel, 10, "Climate control type", climateControlTypeCombo);
        addRow(panel, 11, "", heatedSeatsCheck);
        addRow(panel, 12, "", electricSeatsCheck);
        addRow(panel, 13, "", sunroofCheck);
        addRow(panel, 14, "", premiumAudioCheck);
        return panel;
    }

    private JPanel buildOwnershipPanel() {
        JPanel panel = formPanel();
        addRow(panel, 0, "Warranty years", warrantyYearsField);
        addRow(panel, 1, "Warranty km", warrantyKmField);
        addRow(panel, 2, "Service plan years", servicePlanYearsField);
        addRow(panel, 3, "Service plan km", servicePlanKmField);
        addRow(panel, 4, "Service interval km", serviceIntervalField);
        addRow(panel, 5, "Maintenance plan years", maintenancePlanYearsField);
        addRow(panel, 6, "Maintenance plan km", maintenancePlanKmField);
        addRow(panel, 7, "Parts support score (0-100)", partsSupportScoreField);
        addRow(panel, 8, "", localProductionCheck);
        return panel;
    }

    private JPanel buildPricingPanel() {
        JPanel panel = formPanel();
        addRow(panel, 0, "Price (ZAR)", priceZarField);
        addRow(panel, 1, "Price date (YYYY-MM-DD)", priceDateField);
        return panel;
    }

    private JPanel buildSourcePanel() {
        JPanel panel = formPanel();
        addRow(panel, 0, "Source type", sourceTypeCombo);
        addRow(panel, 1, "Source name", sourceNameField);
        addRow(panel, 2, "Source URL", sourceUrlField);
        addRow(panel, 3, "Imported date", importedDateLabel);
        return panel;
    }

    private JPanel buildScoresPanel() {
        JPanel panel = formPanel();
        addRow(panel, 0, "Safety", safetyScoreField);
        addRow(panel, 1, "Running cost", runningCostScoreField);
        addRow(panel, 2, "Reliability", reliabilityScoreField);
        addRow(panel, 3, "Reliability confidence", reliabilityConfidenceField);
        addRow(panel, 4, "Comfort", comfortScoreField);
        addRow(panel, 5, "Performance", performanceScoreField);
        addRow(panel, 6, "Daily driver", dailyDriverScoreField);
        addRow(panel, 7, "Technology", technologyScoreField);
        addRow(panel, 8, "Awesomeness", awesomenessScoreField);
        addRow(panel, 9, "Prestige", prestigeScoreField);
        addRow(panel, 10, "Overall", overallScoreField);
        addRow(panel, 11, "Score/R100k", scorePer100kField);
        addRow(panel, 12, "Reliability override", reliabilityOverrideSpinner);
        addRow(panel, 13, "Prestige override", prestigeOverrideSpinner);
        return panel;
    }

    private JPanel buildNotesPanel() {
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(notesArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildDataReportPanel() {
        dataReportArea.setEditable(false);
        dataReportArea.setLineWrap(true);
        dataReportArea.setWrapStyleWord(true);
        dataReportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(dataReportArea), BorderLayout.CENTER);
        return panel;
    }

    private void clearForm() {
        makeField.setText("");
        modelField.setText("");
        derivativeField.setText("");
        modelYearField.setText("");
        bodyTypeCombo.setSelectedIndex(0);
        statusCombo.setSelectedItem(VehicleStatus.CANDIDATE);
        clearEngineFields();
        clearTransmissionFields();
        clearDimensionsFields();
        clearWheelsFields();
        clearInfotainmentFields();
        clearEconomyFields();
        clearSafetyFields();
        clearFeaturesFields();
        clearOwnershipFields();
        priceZarField.setText("");
        priceDateField.setText("");
        sourceTypeCombo.setSelectedIndex(0);
        sourceNameField.setText("");
        sourceUrlField.setText("");
        importedDateLabel.setText("-");
        setScores(null);
        dataReportArea.setText("");
        reliabilityOverrideSpinner.setValue(-1);
        prestigeOverrideSpinner.setValue(-1);
        notesArea.setText("");
    }

    private void clearEngineFields() {
        fuelTypeCombo.setSelectedIndex(0);
        displacementField.setText("");
        cylindersField.setText("");
        powerKwField.setText("");
        torqueNmField.setText("");
        aspirationCombo.setSelectedIndex(0);
        hybridCheck.setSelected(false);
        phevCheck.setSelected(false);
    }

    private void clearTransmissionFields() {
        transmissionTypeCombo.setSelectedIndex(0);
        gearsField.setText("");
        drivetrainCombo.setSelectedIndex(0);
        zeroToHundredField.setText("");
        topSpeedField.setText("");
    }

    private void clearDimensionsFields() {
        lengthField.setText("");
        widthField.setText("");
        heightField.setText("");
        wheelbaseField.setText("");
        groundClearanceField.setText("");
        turningCircleField.setText("");
        bootField.setText("");
        kerbWeightField.setText("");
        seatsField.setText("");
        towingBrakedField.setText("");
    }

    private void clearWheelsFields() {
        tyreSizeField.setText("");
    }

    private void clearInfotainmentFields() {
        screenSizeField.setText("");
        speakerCountField.setText("");
    }

    private void clearEconomyFields() {
        fuelConsumptionField.setText("");
        fuelTankField.setText("");
        co2Field.setText("");
    }

    private void clearSafetyFields() {
        ncapStarsField.setText("");
        airbagsField.setText("");
        absCheck.setSelected(false);
        espCheck.setSelected(false);
        tractionCheck.setSelected(false);
        aebCheck.setSelected(false);
        laneAssistCheck.setSelected(false);
        blindSpotCheck.setSelected(false);
        adaptiveCruiseCheck.setSelected(false);
        rearCrossTrafficCheck.setSelected(false);
    }

    private void clearFeaturesFields() {
        androidAutoCheck.setSelected(false);
        appleCarplayCheck.setSelected(false);
        reverseCameraCheck.setSelected(false);
        parkingFrontCheck.setSelected(false);
        parkingRearCheck.setSelected(false);
        digitalClusterCheck.setSelected(false);
        keylessEntryCheck.setSelected(false);
        pushButtonStartCheck.setSelected(false);
        wirelessChargingCheck.setSelected(false);
        climateControlCheck.setSelected(false);
        climateControlTypeCombo.setSelectedIndex(0);
        heatedSeatsCheck.setSelected(false);
        electricSeatsCheck.setSelected(false);
        sunroofCheck.setSelected(false);
        premiumAudioCheck.setSelected(false);
    }

    private void clearOwnershipFields() {
        warrantyYearsField.setText("");
        warrantyKmField.setText("");
        servicePlanYearsField.setText("");
        servicePlanKmField.setText("");
        serviceIntervalField.setText("");
        maintenancePlanYearsField.setText("");
        maintenancePlanKmField.setText("");
        partsSupportScoreField.setText("");
        localProductionCheck.setSelected(false);
    }

    private boolean hasEngineData() {
        return fuelTypeCombo.getSelectedItem() != null
                || !displacementField.getText().isBlank()
                || !cylindersField.getText().isBlank()
                || !powerKwField.getText().isBlank()
                || !torqueNmField.getText().isBlank()
                || aspirationCombo.getSelectedItem() != null
                || hybridCheck.isSelected()
                || phevCheck.isSelected();
    }

    private boolean hasTransmissionData() {
        return transmissionTypeCombo.getSelectedItem() != null
                || !gearsField.getText().isBlank()
                || drivetrainCombo.getSelectedItem() != null;
    }

    private boolean hasPerformanceData() {
        return !zeroToHundredField.getText().isBlank() || !topSpeedField.getText().isBlank();
    }

    private boolean hasDimensionsData() {
        return !lengthField.getText().isBlank()
                || !widthField.getText().isBlank()
                || !heightField.getText().isBlank()
                || !wheelbaseField.getText().isBlank()
                || !groundClearanceField.getText().isBlank()
                || !turningCircleField.getText().isBlank()
                || !bootField.getText().isBlank()
                || !kerbWeightField.getText().isBlank()
                || !seatsField.getText().isBlank();
    }

    private boolean hasTowingData() {
        return !towingBrakedField.getText().isBlank();
    }

    private boolean hasWheelsData() {
        return !tyreSizeField.getText().isBlank();
    }

    private boolean hasInfotainmentData() {
        return !screenSizeField.getText().isBlank() || !speakerCountField.getText().isBlank();
    }

    private boolean hasEconomyData() {
        return !fuelConsumptionField.getText().isBlank()
                || !fuelTankField.getText().isBlank()
                || !co2Field.getText().isBlank();
    }

    private boolean hasSafetyData() {
        return !ncapStarsField.getText().isBlank()
                || !airbagsField.getText().isBlank()
                || absCheck.isSelected()
                || espCheck.isSelected()
                || tractionCheck.isSelected()
                || aebCheck.isSelected()
                || laneAssistCheck.isSelected()
                || blindSpotCheck.isSelected()
                || adaptiveCruiseCheck.isSelected()
                || rearCrossTrafficCheck.isSelected();
    }

    private boolean hasFeaturesData() {
        return androidAutoCheck.isSelected()
                || appleCarplayCheck.isSelected()
                || reverseCameraCheck.isSelected()
                || parkingFrontCheck.isSelected()
                || parkingRearCheck.isSelected()
                || digitalClusterCheck.isSelected()
                || keylessEntryCheck.isSelected()
                || pushButtonStartCheck.isSelected()
                || wirelessChargingCheck.isSelected()
                || climateControlCheck.isSelected()
                || climateControlTypeCombo.getSelectedItem() != null
                || heatedSeatsCheck.isSelected()
                || electricSeatsCheck.isSelected()
                || sunroofCheck.isSelected()
                || premiumAudioCheck.isSelected();
    }

    private boolean hasOwnershipData() {
        return !warrantyYearsField.getText().isBlank()
                || !warrantyKmField.getText().isBlank()
                || !servicePlanYearsField.getText().isBlank()
                || !servicePlanKmField.getText().isBlank()
                || !serviceIntervalField.getText().isBlank()
                || !maintenancePlanYearsField.getText().isBlank()
                || !maintenancePlanKmField.getText().isBlank()
                || !partsSupportScoreField.getText().isBlank()
                || localProductionCheck.isSelected();
    }

    private boolean hasPricingData() {
        return !priceZarField.getText().isBlank() || !priceDateField.getText().isBlank();
    }

    private boolean hasSourceData() {
        return sourceTypeCombo.getSelectedItem() != null
                || !sourceNameField.getText().isBlank()
                || !sourceUrlField.getText().isBlank();
    }

    private static JPanel formPanel() {
        return new JPanel(new GridBagLayout());
    }

    private static JPanel wrapTopLeft(JPanel form) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.NORTH);
        return wrapper;
    }

    private static void addRow(JPanel panel, int row, String label, java.awt.Component field) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.NORTHWEST;
        labelConstraints.insets = new Insets(4, 8, 4, 8);
        panel.add(new JLabel(label), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.anchor = GridBagConstraints.NORTHWEST;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.insets = new Insets(4, 8, 4, 8);
        panel.add(field, fieldConstraints);
    }

    private static <E extends Enum<E>> JComboBox<E> enumCombo(E[] values) {
        E[] withBlank = java.util.Arrays.copyOf(values, values.length + 1);
        return new JComboBox<>(withBlank);
    }

    private static <E extends Enum<E>> void setComboSelection(JComboBox<E> combo, E value) {
        combo.setSelectedItem(value);
    }

    private static JTextField readOnlyField() {
        JTextField field = new JTextField(10);
        field.setEditable(false);
        return field;
    }

    private static JSpinner overrideSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(-1, -1, 100, 1));
        return spinner;
    }

    private static Double spinnerValue(JSpinner spinner) {
        int value = (Integer) spinner.getValue();
        return value < 0 ? null : (double) value;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String integerToString(Integer value) {
        return value == null ? "" : value.toString();
    }

    private static String doubleToString(Double value) {
        return value == null ? "" : value.toString();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static Integer parseInteger(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        return Integer.valueOf(trimmed);
    }

    private static Double parseDouble(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        return Double.valueOf(trimmed);
    }

    private static BigDecimal parseBigDecimal(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        return new BigDecimal(trimmed);
    }

    private static LocalDate parseDate(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return LocalDate.parse(trimmed);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private static String formatScore(Double score) {
        return score == null ? "-" : String.format("%.1f", score);
    }
}
