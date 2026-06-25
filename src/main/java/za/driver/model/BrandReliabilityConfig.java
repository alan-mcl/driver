package za.driver.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class BrandReliabilityConfig {

    private int schemaVersion = 1;
    private Map<String, BrandReliabilityEntry> brands = new LinkedHashMap<>();
    private Map<String, String> aliases = new LinkedHashMap<>();

    public BrandReliabilityConfig() {
    }

    public static BrandReliabilityConfig empty() {
        return new BrandReliabilityConfig();
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public Map<String, BrandReliabilityEntry> getBrands() {
        return brands;
    }

    public void setBrands(Map<String, BrandReliabilityEntry> brands) {
        this.brands = brands == null ? new LinkedHashMap<>() : brands;
    }

    public Map<String, String> getAliases() {
        return aliases;
    }

    public void setAliases(Map<String, String> aliases) {
        this.aliases = aliases == null ? new LinkedHashMap<>() : aliases;
    }
}
