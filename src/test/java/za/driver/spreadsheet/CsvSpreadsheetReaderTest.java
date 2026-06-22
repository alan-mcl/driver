package za.driver.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class CsvSpreadsheetReaderTest {

    @Test
    void parseRow_handlesQuotedCommasAndEscapedQuotes() {
        assertEquals(
                List.of("a", "b,c", "d\"e"),
                CsvSpreadsheetReader.parseRow("a,\"b,c\",\"d\"\"e\""));
    }
}
