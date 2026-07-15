package za.driver.model;

public class VehicleSortPreferences {

    private VehicleTableColumn columnKey;
    private Metric metric;
    private Boolean ascending;

    public VehicleTableColumn getColumnKey() {
        return columnKey;
    }

    public void setColumnKey(VehicleTableColumn columnKey) {
        this.columnKey = columnKey;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public Boolean getAscending() {
        return ascending;
    }

    public void setAscending(Boolean ascending) {
        this.ascending = ascending;
    }
}
