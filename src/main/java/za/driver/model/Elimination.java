package za.driver.model;

import java.time.LocalDate;
import java.util.UUID;

public class Elimination {

    private UUID vehicleId;
    private LocalDate eliminatedDate;
    private String reason;

    public Elimination() {
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(UUID vehicleId) {
        this.vehicleId = vehicleId;
    }

    public LocalDate getEliminatedDate() {
        return eliminatedDate;
    }

    public void setEliminatedDate(LocalDate eliminatedDate) {
        this.eliminatedDate = eliminatedDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
