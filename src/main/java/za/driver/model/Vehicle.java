package za.driver.model;

import java.util.Map;
import java.util.UUID;

public class Vehicle {

    private UUID id;
    private String make;
    private String model;
    private String derivative;
    private Integer modelYear;
    private BodyType bodyType;
    private VehicleStatus status;
    private String notes;
    private Engine engine;
    private Transmission transmission;
    private Performance performance;
    private Dimensions dimensions;
    private Towing towing;
    private Wheels wheels;
    private Infotainment infotainment;
    private Economy economy;
    private Safety safety;
    private Features features;
    private Ownership ownership;
    private Pricing pricing;
    private Source source;
    private ManualScoreOverrides manualScoreOverrides;
    private DerivedMetrics derivedMetrics;
    private Map<String, DataQuality> dataQuality;

    public Vehicle() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDerivative() {
        return derivative;
    }

    public void setDerivative(String derivative) {
        this.derivative = derivative;
    }

    public Integer getModelYear() {
        return modelYear;
    }

    public void setModelYear(Integer modelYear) {
        this.modelYear = modelYear;
    }

    public BodyType getBodyType() {
        return bodyType;
    }

    public void setBodyType(BodyType bodyType) {
        this.bodyType = bodyType;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public Transmission getTransmission() {
        return transmission;
    }

    public void setTransmission(Transmission transmission) {
        this.transmission = transmission;
    }

    public Performance getPerformance() {
        return performance;
    }

    public void setPerformance(Performance performance) {
        this.performance = performance;
    }

    public Dimensions getDimensions() {
        return dimensions;
    }

    public void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    public Towing getTowing() {
        return towing;
    }

    public void setTowing(Towing towing) {
        this.towing = towing;
    }

    public Wheels getWheels() {
        return wheels;
    }

    public void setWheels(Wheels wheels) {
        this.wheels = wheels;
    }

    public Infotainment getInfotainment() {
        return infotainment;
    }

    public void setInfotainment(Infotainment infotainment) {
        this.infotainment = infotainment;
    }

    public Economy getEconomy() {
        return economy;
    }

    public void setEconomy(Economy economy) {
        this.economy = economy;
    }

    public Safety getSafety() {
        return safety;
    }

    public void setSafety(Safety safety) {
        this.safety = safety;
    }

    public Features getFeatures() {
        return features;
    }

    public void setFeatures(Features features) {
        this.features = features;
    }

    public Ownership getOwnership() {
        return ownership;
    }

    public void setOwnership(Ownership ownership) {
        this.ownership = ownership;
    }

    public Pricing getPricing() {
        return pricing;
    }

    public void setPricing(Pricing pricing) {
        this.pricing = pricing;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public ManualScoreOverrides getManualScoreOverrides() {
        return manualScoreOverrides;
    }

    public void setManualScoreOverrides(ManualScoreOverrides manualScoreOverrides) {
        this.manualScoreOverrides = manualScoreOverrides;
    }

    public DerivedMetrics getDerivedMetrics() {
        return derivedMetrics;
    }

    public void setDerivedMetrics(DerivedMetrics derivedMetrics) {
        this.derivedMetrics = derivedMetrics;
    }

    public Map<String, DataQuality> getDataQuality() {
        return dataQuality;
    }

    public void setDataQuality(Map<String, DataQuality> dataQuality) {
        this.dataQuality = dataQuality;
    }
}
