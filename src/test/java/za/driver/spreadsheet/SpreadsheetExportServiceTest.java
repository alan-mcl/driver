package za.driver.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import za.driver.scoring.ScoringTestFixtures;

class SpreadsheetExportServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void export_writesCsvWithExpectedHeaders() throws IOException {
        Path file = tempDir.resolve("vehicles.csv");
        new SpreadsheetExportService().export(file, List.of(ScoringTestFixtures.fullVehicle()));

        SpreadsheetDataSheet sheet = new CsvSpreadsheetReader().readDataSheet(file);
        assertEquals(VehicleSpreadsheetSchema.headers(), sheet.headers());
        assertEquals(1, sheet.rows().size());
        assertEquals(ScoringTestFixtures.fullVehicle().getId().toString(), sheet.rows().get(0).get("id"));
    }

    @Test
    void export_placesPricingColumnsAfterDerivative() throws IOException {
        List<String> headers = VehicleSpreadsheetSchema.headers();
        assertEquals("derivative", headers.get(3));
        assertEquals("pricing.listPrice", headers.get(4));
        assertEquals("pricing.dealerOffer", headers.get(5));
        assertEquals("pricing.listPriceDate", headers.get(6));
        assertEquals("pricing.dealerOfferDate", headers.get(7));
        assertEquals("modelYear", headers.get(8));
        assertTrue(headers.indexOf("pricing.listPrice") < headers.indexOf("engine.powerKw"));
    }

    @Test
    void export_placesManualScoreColumnsAfterOwnership() {
        List<String> headers = VehicleSpreadsheetSchema.headers();
        int localProduction = headers.indexOf("ownership.localProduction");
        assertEquals("manualScoreOverrides.reliabilityManualEstimate", headers.get(localProduction + 1));
        assertEquals("derivedMetrics.reliabilityHeuristic", headers.get(localProduction + 2));
        assertEquals("derivedMetrics.reliabilityScore", headers.get(localProduction + 3));
        assertEquals("manualScoreOverrides.prestigeScore", headers.get(localProduction + 4));
        assertEquals("derivedMetrics.prestigeScore", headers.get(localProduction + 5));
        assertEquals("source.sourceType", headers.get(localProduction + 6));
    }
}
