package za.driver.model;

public class Transmission {

    private TransmissionType type;
    private Integer gears;
    private DrivetrainType drivetrain;

    public Transmission() {
    }

    public TransmissionType getType() {
        return type;
    }

    public void setType(TransmissionType type) {
        this.type = type;
    }

    public Integer getGears() {
        return gears;
    }

    public void setGears(Integer gears) {
        this.gears = gears;
    }

    public DrivetrainType getDrivetrain() {
        return drivetrain;
    }

    public void setDrivetrain(DrivetrainType drivetrain) {
        this.drivetrain = drivetrain;
    }
}
