package za.driver.model;

public class Economy {

    private Double fuelConsumptionCombined;
    private Double fuelTankLitres;
    private Double co2Gkm;

    public Economy() {
    }

    public Double getFuelConsumptionCombined() {
        return fuelConsumptionCombined;
    }

    public void setFuelConsumptionCombined(Double fuelConsumptionCombined) {
        this.fuelConsumptionCombined = fuelConsumptionCombined;
    }

    public Double getFuelTankLitres() {
        return fuelTankLitres;
    }

    public void setFuelTankLitres(Double fuelTankLitres) {
        this.fuelTankLitres = fuelTankLitres;
    }

    public Double getCo2Gkm() {
        return co2Gkm;
    }

    public void setCo2Gkm(Double co2Gkm) {
        this.co2Gkm = co2Gkm;
    }
}
