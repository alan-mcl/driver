package za.driver.model;

public class Safety {

    private Integer ncapStars;
    private Integer airbags;
    private Boolean abs;
    private Boolean esp;
    private Boolean tractionControl;
    private Boolean aeb;
    private Boolean laneAssist;
    private Boolean blindSpotMonitoring;
    private Boolean adaptiveCruiseControl;
    private Boolean rearCrossTrafficAlert;

    public Safety() {
    }

    public Integer getNcapStars() {
        return ncapStars;
    }

    public void setNcapStars(Integer ncapStars) {
        this.ncapStars = ncapStars;
    }

    public Integer getAirbags() {
        return airbags;
    }

    public void setAirbags(Integer airbags) {
        this.airbags = airbags;
    }

    public Boolean getAbs() {
        return abs;
    }

    public void setAbs(Boolean abs) {
        this.abs = abs;
    }

    public Boolean getEsp() {
        return esp;
    }

    public void setEsp(Boolean esp) {
        this.esp = esp;
    }

    public Boolean getTractionControl() {
        return tractionControl;
    }

    public void setTractionControl(Boolean tractionControl) {
        this.tractionControl = tractionControl;
    }

    public Boolean getAeb() {
        return aeb;
    }

    public void setAeb(Boolean aeb) {
        this.aeb = aeb;
    }

    public Boolean getLaneAssist() {
        return laneAssist;
    }

    public void setLaneAssist(Boolean laneAssist) {
        this.laneAssist = laneAssist;
    }

    public Boolean getBlindSpotMonitoring() {
        return blindSpotMonitoring;
    }

    public void setBlindSpotMonitoring(Boolean blindSpotMonitoring) {
        this.blindSpotMonitoring = blindSpotMonitoring;
    }

    public Boolean getAdaptiveCruiseControl() {
        return adaptiveCruiseControl;
    }

    public void setAdaptiveCruiseControl(Boolean adaptiveCruiseControl) {
        this.adaptiveCruiseControl = adaptiveCruiseControl;
    }

    public Boolean getRearCrossTrafficAlert() {
        return rearCrossTrafficAlert;
    }

    public void setRearCrossTrafficAlert(Boolean rearCrossTrafficAlert) {
        this.rearCrossTrafficAlert = rearCrossTrafficAlert;
    }
}
