package za.driver.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvSpreadsheetReaderTest {

    @Test
    void parseRow_handlesQuotedCommasAndEscapedQuotes() {
        assertEquals(
                List.of("a", "b,c", "d\"e"),
                CsvSpreadsheetReader.parseRow("a,\"b,c\",\"d\"\"e\""));
    }

    @Test
    void stripUtf8Bom_removesLeadingBomCharacter() {
        assertEquals("id,make", CsvSpreadsheetReader.stripUtf8Bom("\uFEFFid,make"));
        assertEquals("id,make", CsvSpreadsheetReader.stripUtf8Bom("id,make"));
    }

    @Test
    void readDataSheet_acceptsUtf8Bom(@TempDir java.nio.file.Path tempDir) throws IOException {
        java.nio.file.Path file = tempDir.resolve("bom.csv");
        String headerLine = String.join(",", VehicleSpreadsheetSchema.headers());
        Files.writeString(file, "\uFEFF" + headerLine + "\n", StandardCharsets.UTF_8);

        SpreadsheetDataSheet sheet = new CsvSpreadsheetReader().readDataSheet(file);
        assertEquals(VehicleSpreadsheetSchema.headers(), sheet.headers());
    }
}
