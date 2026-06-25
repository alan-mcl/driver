package za.driver.model;

public class BrandReliabilityEntry {

    private int reliability;
    private int confidence;

    public BrandReliabilityEntry() {
    }

    public BrandReliabilityEntry(int reliability, int confidence) {
        this.reliability = reliability;
        this.confidence = confidence;
    }

    public int getReliability() {
        return reliability;
    }

    public void setReliability(int reliability) {
        this.reliability = reliability;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }
}
