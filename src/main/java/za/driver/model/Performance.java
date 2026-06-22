package za.driver.model;

public class Performance {

    private Double zeroToHundredSeconds;
    private Integer topSpeedKmh;

    public Performance() {
    }

    public Double getZeroToHundredSeconds() {
        return zeroToHundredSeconds;
    }

    public void setZeroToHundredSeconds(Double zeroToHundredSeconds) {
        this.zeroToHundredSeconds = zeroToHundredSeconds;
    }

    public Integer getTopSpeedKmh() {
        return topSpeedKmh;
    }

    public void setTopSpeedKmh(Integer topSpeedKmh) {
        this.topSpeedKmh = topSpeedKmh;
    }
}
