package za.driver.model;

public class Dimensions {

    private Integer lengthMm;
    private Integer widthMm;
    private Integer heightMm;
    private Integer wheelbaseMm;
    private Integer groundClearanceMm;
    private Double turningCircleM;
    private Integer bootLitres;
    private Integer kerbWeightKg;
    private Integer seats;

    public Dimensions() {
    }

    public Integer getLengthMm() {
        return lengthMm;
    }

    public void setLengthMm(Integer lengthMm) {
        this.lengthMm = lengthMm;
    }

    public Integer getWidthMm() {
        return widthMm;
    }

    public void setWidthMm(Integer widthMm) {
        this.widthMm = widthMm;
    }

    public Integer getHeightMm() {
        return heightMm;
    }

    public void setHeightMm(Integer heightMm) {
        this.heightMm = heightMm;
    }

    public Integer getWheelbaseMm() {
        return wheelbaseMm;
    }

    public void setWheelbaseMm(Integer wheelbaseMm) {
        this.wheelbaseMm = wheelbaseMm;
    }

    public Integer getGroundClearanceMm() {
        return groundClearanceMm;
    }

    public void setGroundClearanceMm(Integer groundClearanceMm) {
        this.groundClearanceMm = groundClearanceMm;
    }

    public Double getTurningCircleM() {
        return turningCircleM;
    }

    public void setTurningCircleM(Double turningCircleM) {
        this.turningCircleM = turningCircleM;
    }

    public Integer getBootLitres() {
        return bootLitres;
    }

    public void setBootLitres(Integer bootLitres) {
        this.bootLitres = bootLitres;
    }

    public Integer getKerbWeightKg() {
        return kerbWeightKg;
    }

    public void setKerbWeightKg(Integer kerbWeightKg) {
        this.kerbWeightKg = kerbWeightKg;
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }
}
