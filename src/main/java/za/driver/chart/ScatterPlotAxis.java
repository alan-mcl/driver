package za.driver.chart;

public enum ScatterPlotAxis {
    PRICE("Price"),
    OVERALL_SCORE("Overall Score"),
    SAFETY("Safety"),
    RUNNING_COST("Running Cost"),
    RELIABILITY("Reliability"),
    PERFORMANCE("Performance"),
    AWESOMENESS("Awesomeness"),
    SCORE_PER_100K("Score/R100k");

    private final String label;

    ScatterPlotAxis(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
