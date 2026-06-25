package za.driver.scoring;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import za.driver.model.BrandReliabilityConfig;
import za.driver.model.BrandReliabilityEntry;

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
            return fromConfig(readConfig(input));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load brand reliability config", ex);
        }
    }

    public static BrandReliabilityConfig loadBundledConfig() {
        try (InputStream input = BrandReliabilityLookup.class.getResourceAsStream("/config/brand-reliability.json")) {
            if (input == null) {
                throw new IllegalStateException("Missing classpath resource /config/brand-reliability.json");
            }
            return readConfig(input);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load brand reliability config", ex);
        }
    }

    public static BrandReliabilityLookup merge(BrandReliabilityConfig bundled, BrandReliabilityConfig user) {
        return fromConfig(overlayConfigs(bundled, user));
    }

    public static BrandReliabilityConfig overlayConfigs(BrandReliabilityConfig bundled, BrandReliabilityConfig user) {
        BrandReliabilityConfig merged = new BrandReliabilityConfig();
        Map<String, BrandReliabilityEntry> brands = new LinkedHashMap<>();
        if (bundled != null && bundled.getBrands() != null) {
            brands.putAll(bundled.getBrands());
        }
        if (user != null && user.getBrands() != null) {
            brands.putAll(user.getBrands());
        }
        merged.setBrands(brands);

        Map<String, String> aliases = new LinkedHashMap<>();
        if (bundled != null && bundled.getAliases() != null) {
            aliases.putAll(bundled.getAliases());
        }
        if (user != null && user.getAliases() != null) {
            aliases.putAll(user.getAliases());
        }
        merged.setAliases(aliases);
        return merged;
    }

    public static BrandReliabilityLookup fromConfig(BrandReliabilityConfig config) {
        if (config == null || config.getBrands() == null) {
            return new BrandReliabilityLookup(Map.of());
        }

        Map<String, BrandEntry> brands = new HashMap<>();
        config.getBrands().forEach((name, entry) -> {
            if (entry == null) {
                return;
            }
            brands.put(normalizeMake(name), new BrandEntry(name, entry.getReliability(), entry.getConfidence()));
        });

        Map<String, String> aliases = config.getAliases();
        if (aliases != null) {
            aliases.forEach((alias, canonicalName) -> {
                BrandEntry canonical = brands.get(normalizeMake(canonicalName));
                if (canonical != null) {
                    brands.put(normalizeMake(alias), canonical);
                }
            });
        }

        return new BrandReliabilityLookup(brands);
    }

    static BrandReliabilityLookup load(InputStream input) throws IOException {
        return fromConfig(readConfig(input));
    }

    private static BrandReliabilityConfig readConfig(InputStream input) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(input, BrandReliabilityConfig.class);
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
