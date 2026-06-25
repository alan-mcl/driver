package za.driver.spreadsheet;

@FunctionalInterface
public interface SpreadsheetImportProgress {

    /**
     * @param current rows processed ({@code 0} while loading existing vehicles)
     * @param total   total data rows in the sheet
     */
    void onProgress(int current, int total);
}
