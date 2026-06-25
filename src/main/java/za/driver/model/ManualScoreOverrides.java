package za.driver.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class ManualScoreOverrides {

    @JsonAlias("reliabilityScore")
    private Double reliabilityManualEstimate;
    private Double prestigeScore;

    public ManualScoreOverrides() {
    }

    public Double getReliabilityManualEstimate() {
        return reliabilityManualEstimate;
    }

    public void setReliabilityManualEstimate(Double reliabilityManualEstimate) {
        this.reliabilityManualEstimate = reliabilityManualEstimate;
    }

    public Double getPrestigeScore() {
        return prestigeScore;
    }

    public void setPrestigeScore(Double prestigeScore) {
        this.prestigeScore = prestigeScore;
    }
}
