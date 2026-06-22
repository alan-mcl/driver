package za.driver.model;

public class Engine {

    private FuelType fuelType;
    private Integer displacementCc;
    private Integer cylinders;
    private Double powerKw;
    private Double torqueNm;
    private Aspiration aspiration;
    private Boolean hybrid;
    private Boolean phev;

    public Engine() {
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public Integer getDisplacementCc() {
        return displacementCc;
    }

    public void setDisplacementCc(Integer displacementCc) {
        this.displacementCc = displacementCc;
    }

    public Integer getCylinders() {
        return cylinders;
    }

    public void setCylinders(Integer cylinders) {
        this.cylinders = cylinders;
    }

    public Double getPowerKw() {
        return powerKw;
    }

    public void setPowerKw(Double powerKw) {
        this.powerKw = powerKw;
    }

    public Double getTorqueNm() {
        return torqueNm;
    }

    public void setTorqueNm(Double torqueNm) {
        this.torqueNm = torqueNm;
    }

    public Aspiration getAspiration() {
        return aspiration;
    }

    public void setAspiration(Aspiration aspiration) {
        this.aspiration = aspiration;
    }

    public Boolean getHybrid() {
        return hybrid;
    }

    public void setHybrid(Boolean hybrid) {
        this.hybrid = hybrid;
    }

    public Boolean getPhev() {
        return phev;
    }

    public void setPhev(Boolean phev) {
        this.phev = phev;
    }
}
