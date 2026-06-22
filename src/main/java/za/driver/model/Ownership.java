package za.driver.model;

public class Ownership {

    private Integer warrantyYears;
    private Integer warrantyKm;
    private Integer servicePlanYears;
    private Integer servicePlanKm;
    private Integer serviceIntervalKm;
    private Integer maintenancePlanYears;
    private Integer maintenancePlanKm;
    private Integer partsSupportScore;
    private Boolean localProduction;

    public Ownership() {
    }

    public Integer getWarrantyYears() {
        return warrantyYears;
    }

    public void setWarrantyYears(Integer warrantyYears) {
        this.warrantyYears = warrantyYears;
    }

    public Integer getWarrantyKm() {
        return warrantyKm;
    }

    public void setWarrantyKm(Integer warrantyKm) {
        this.warrantyKm = warrantyKm;
    }

    public Integer getServicePlanYears() {
        return servicePlanYears;
    }

    public void setServicePlanYears(Integer servicePlanYears) {
        this.servicePlanYears = servicePlanYears;
    }

    public Integer getServicePlanKm() {
        return servicePlanKm;
    }

    public void setServicePlanKm(Integer servicePlanKm) {
        this.servicePlanKm = servicePlanKm;
    }

    public Integer getServiceIntervalKm() {
        return serviceIntervalKm;
    }

    public void setServiceIntervalKm(Integer serviceIntervalKm) {
        this.serviceIntervalKm = serviceIntervalKm;
    }

    public Integer getMaintenancePlanYears() {
        return maintenancePlanYears;
    }

    public void setMaintenancePlanYears(Integer maintenancePlanYears) {
        this.maintenancePlanYears = maintenancePlanYears;
    }

    public Integer getMaintenancePlanKm() {
        return maintenancePlanKm;
    }

    public void setMaintenancePlanKm(Integer maintenancePlanKm) {
        this.maintenancePlanKm = maintenancePlanKm;
    }

    public Integer getPartsSupportScore() {
        return partsSupportScore;
    }

    public void setPartsSupportScore(Integer partsSupportScore) {
        this.partsSupportScore = partsSupportScore;
    }

    public Boolean getLocalProduction() {
        return localProduction;
    }

    public void setLocalProduction(Boolean localProduction) {
        this.localProduction = localProduction;
    }
}
