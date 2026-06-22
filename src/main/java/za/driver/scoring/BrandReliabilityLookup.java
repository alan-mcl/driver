package za.driver.scoring;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class BrandReliabilityLookup {

    private static final BrandReliabilityLookup DEFAULT = loadDefault();

    private final Map<String, BrandEntry> brandsByNormalizedName;

    public BrandReliabilityLookup(Map<String, BrandEntry> brandsByNormalizedName) {
        this.brandsByNormalizedName = Map.copyOf(brandsByNormalizedName);
    }

    public static BrandReliabilityLookup getDefault() {
        return DEFAULT;
    }

    public static BrandReliabilityLookup loadDefault() {
        try (InputStream input = BrandReliabilityLookup.class.getResourceAsStream("/config/brand-reliability.json")) {
            if (input == null) {
                throw new IllegalStateException("Missing classpath resource /config/brand-reliability.json");
            }
            return load(input);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load brand reliability config", ex);
        }
    }

    static BrandReliabilityLookup load(InputStream input) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(input);
        JsonNode brandsNode = root.get("brands");
        if (brandsNode == null || !brandsNode.isObject()) {
            throw new IOException("brand-reliability.json must contain a brands object");
        }

        Map<String, BrandEntry> brands = new HashMap<>();
        brandsNode.fields().forEachRemaining(entry -> {
            JsonNode value = entry.getValue();
            int reliability = value.get("reliability").asInt();
            int confidence = value.get("confidence").asInt();
            brands.put(normalizeMake(entry.getKey()), new BrandEntry(entry.getKey(), reliability, confidence));
        });

        JsonNode aliasesNode = root.get("aliases");
        if (aliasesNode != null && aliasesNode.isObject()) {
            aliasesNode.fields().forEachRemaining(entry -> {
                String canonicalName = entry.getValue().asText();
                BrandEntry canonical = brands.get(normalizeMake(canonicalName));
                if (canonical != null) {
                    brands.put(normalizeMake(entry.getKey()), canonical);
                }
            });
        }

        return new BrandReliabilityLookup(brands);
    }

    public Integer reliabilityScore(String make) {
        BrandEntry entry = lookup(make);
        return entry == null ? null : entry.reliability();
    }

    public Integer confidenceScore(String make) {
        BrandEntry entry = lookup(make);
        return entry == null ? null : entry.confidence();
    }

    public String displayName(String make) {
        BrandEntry entry = lookup(make);
        return entry == null ? null : entry.displayName();
    }

    BrandEntry lookup(String make) {
        if (make == null || make.isBlank()) {
            return null;
        }
        return brandsByNormalizedName.get(normalizeMake(make));
    }

    static String normalizeMake(String make) {
        return make.trim().toLowerCase(Locale.ROOT);
    }

    record BrandEntry(String displayName, int reliability, int confidence) {
    }
}
