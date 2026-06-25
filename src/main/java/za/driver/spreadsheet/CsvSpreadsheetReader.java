package za.driver.spreadsheet;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CsvSpreadsheetReader {

    private static final char UTF8_BOM = '\uFEFF';

    public SpreadsheetDataSheet readDataSheet(Path file) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (firstLine) {
                    line = stripUtf8Bom(line);
                    firstLine = false;
                }
                rows.add(parseRow(line));
            }
        }

        if (rows.isEmpty()) {
            throw new IOException("CSV file is empty");
        }

        List<String> headers = rows.get(0);
        validateHeaders(headers);

        List<Map<String, String>> dataRows = new ArrayList<>();
        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            List<String> rowValues = rows.get(rowIndex);
            if (isBlankRow(rowValues)) {
                continue;
            }
            Map<String, String> row = new LinkedHashMap<>();
            for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
                String header = headers.get(columnIndex);
                String value = columnIndex < rowValues.size() ? rowValues.get(columnIndex) : "";
                row.put(header, value == null ? "" : value.trim());
            }
            dataRows.add(row);
        }
        return new SpreadsheetDataSheet(headers, dataRows);
    }

    private static void validateHeaders(List<String> headers) throws IOException {
        List<String> expected = VehicleSpreadsheetSchema.headers();
        if (headers.size() != expected.size()) {
            throw new IOException("Expected " + expected.size() + " columns but found " + headers.size());
        }
        for (int i = 0; i < expected.size(); i++) {
            String actual = headers.get(i) == null ? "" : headers.get(i).trim();
            if (!expected.get(i).equals(actual)) {
                throw new IOException("Header mismatch at column " + (i + 1)
                        + ": expected '" + expected.get(i) + "' but found '" + actual + "'");
            }
        }
    }

    static String stripUtf8Bom(String line) {
        if (line != null && !line.isEmpty() && line.charAt(0) == UTF8_BOM) {
            return line.substring(1);
        }
        return line;
    }

    static List<String> parseRow(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (inQuotes) {
                if (character == '"') {
                    if (index + 1 < line.length() && line.charAt(index + 1) == '"') {
                        current.append('"');
                        index++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(character);
                }
                continue;
            }
            if (character == '"') {
                inQuotes = true;
            } else if (character == ',') {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        values.add(current.toString());
        return values;
    }

    private static boolean isBlankRow(List<String> rowValues) {
        if (rowValues == null || rowValues.isEmpty()) {
            return true;
        }
        return rowValues.stream().allMatch(value -> value == null || value.isBlank());
    }
}
