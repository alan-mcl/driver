package za.driver.model;

import java.util.UUID;

public class AppConfig {

    private UUID activeProfileId;

    public UUID getActiveProfileId() {
        return activeProfileId;
    }

    public void setActiveProfileId(UUID activeProfileId) {
        this.activeProfileId = activeProfileId;
    }
}
