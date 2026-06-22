package za.driver.spreadsheet;

import java.util.List;
import java.util.Map;

public record SpreadsheetDataSheet(List<String> headers, List<Map<String, String>> rows) {
}
