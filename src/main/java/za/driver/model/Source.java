package za.driver.model;

import java.time.LocalDateTime;

public class Source {

    private SourceType sourceType;
    private String sourceName;
    private String sourceUrl;
    private LocalDateTime importedDate;

    public Source() {
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public LocalDateTime getImportedDate() {
        return importedDate;
    }

    public void setImportedDate(LocalDateTime importedDate) {
        this.importedDate = importedDate;
    }
}
