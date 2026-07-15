package za.driver.model;

public class VehicleListPreferences {

    private VehicleFilterPreferences filter = new VehicleFilterPreferences();
    private VehicleSortPreferences sort = new VehicleSortPreferences();

    public VehicleFilterPreferences getFilter() {
        return filter;
    }

    public void setFilter(VehicleFilterPreferences filter) {
        this.filter = filter != null ? filter : new VehicleFilterPreferences();
    }

    public VehicleSortPreferences getSort() {
        return sort;
    }

    public void setSort(VehicleSortPreferences sort) {
        this.sort = sort != null ? sort : new VehicleSortPreferences();
    }
}
