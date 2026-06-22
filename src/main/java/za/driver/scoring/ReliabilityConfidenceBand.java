package za.driver.scoring;

public enum ReliabilityConfidenceBand {
    HIGH,
    MEDIUM,
    LOW;

    public static ReliabilityConfidenceBand fromScore(Integer confidence) {
        if (confidence == null) {
            return null;
        }
        if (confidence >= 80) {
            return HIGH;
        }
        if (confidence >= 50) {
            return MEDIUM;
        }
        return LOW;
    }

    public String displayLabel() {
        return switch (this) {
            case HIGH -> "High";
            case MEDIUM -> "Medium";
            case LOW -> "Low";
        };
    }
}
