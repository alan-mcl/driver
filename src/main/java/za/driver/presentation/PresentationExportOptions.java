package za.driver.presentation;

public record PresentationExportOptions(PresentationGroupingMode groupingMode) {

    public static PresentationExportOptions defaults() {
        return new PresentationExportOptions(PresentationGroupingMode.BODY_TYPE);
    }
}
