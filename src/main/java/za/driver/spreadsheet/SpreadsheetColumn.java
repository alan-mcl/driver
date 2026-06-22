package za.driver.spreadsheet;

public record SpreadsheetColumn(String header, ColumnType type, Class<? extends Enum<?>> enumType) {

    public SpreadsheetColumn(String header, ColumnType type) {
        this(header, type, null);
    }
}
