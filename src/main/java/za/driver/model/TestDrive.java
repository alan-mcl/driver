package za.driver.model;

import java.time.LocalDate;
import java.util.UUID;

public class TestDrive {

    private UUID id;
    private UUID vehicleId;
    private LocalDate driveDate;
    private Integer comfortRating;
    private Integer visibilityRating;
    private Integer handlingRating;
    private Integer spouseApprovalRating;
    private Integer overallImpression;
    private String notes;

    public TestDrive() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(UUID vehicleId) {
        this.vehicleId = vehicleId;
    }

    public LocalDate getDriveDate() {
        return driveDate;
    }

    public void setDriveDate(LocalDate driveDate) {
        this.driveDate = driveDate;
    }

    public Integer getComfortRating() {
        return comfortRating;
    }

    public void setComfortRating(Integer comfortRating) {
        this.comfortRating = comfortRating;
    }

    public Integer getVisibilityRating() {
        return visibilityRating;
    }

    public void setVisibilityRating(Integer visibilityRating) {
        this.visibilityRating = visibilityRating;
    }

    public Integer getHandlingRating() {
        return handlingRating;
    }

    public void setHandlingRating(Integer handlingRating) {
        this.handlingRating = handlingRating;
    }

    public Integer getSpouseApprovalRating() {
        return spouseApprovalRating;
    }

    public void setSpouseApprovalRating(Integer spouseApprovalRating) {
        this.spouseApprovalRating = spouseApprovalRating;
    }

    public Integer getOverallImpression() {
        return overallImpression;
    }

    public void setOverallImpression(Integer overallImpression) {
        this.overallImpression = overallImpression;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
