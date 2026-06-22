package za.driver.spreadsheet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import za.driver.model.Vehicle;

public final class SpreadsheetExportService {

    private final CsvSpreadsheetWriter writer = new CsvSpreadsheetWriter();

    public void export(Path file, List<Vehicle> vehicles) throws IOException {
        List<List<String>> dataRows = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            dataRows.add(VehicleSpreadsheetMapper.toRowValues(vehicle));
        }
        writer.write(file, VehicleSpreadsheetSchema.headers(), dataRows);
    }
}
