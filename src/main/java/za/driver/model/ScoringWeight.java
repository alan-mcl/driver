package za.driver.model;

public class ScoringWeight {

    private Metric metric;
    private Double weight;

    public ScoringWeight() {
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
