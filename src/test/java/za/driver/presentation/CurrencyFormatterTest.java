package za.driver.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import za.driver.model.CurrencyPreset;
import za.driver.model.DisplayPreferences;

class CurrencyFormatterTest {

    @Test
    void defaults_formatsZarWithSpace() {
        CurrencyFormatter formatter = CurrencyFormatter.defaults();

        assertTrue(formatter.format(BigDecimal.valueOf(350_000)).startsWith("R "));
        assertTrue(formatter.format(BigDecimal.valueOf(350_000)).contains("350"));
        assertEquals("List price (ZAR)", formatter.priceFieldLabel("List price"));
        assertEquals("Score/R100k", formatter.scorePer100kLabel());
    }

    @Test
    void usdPreset_formatsWithDollarSymbol() {
        DisplayPreferences prefs = new DisplayPreferences();
        prefs.setPreset(CurrencyPreset.USD);
        CurrencyFormatter formatter = new CurrencyFormatter(prefs);

        assertTrue(formatter.format(BigDecimal.valueOf(350_000)).startsWith("$ "));
        assertTrue(formatter.format(BigDecimal.valueOf(350_000)).contains("350"));
        assertEquals("Price (USD)", formatter.priceFieldLabel("Price"));
        assertEquals("Score/$100k", formatter.scorePer100kLabel());
    }

    @Test
    void customPreset_usesCustomSymbolAndLocale() {
        DisplayPreferences prefs = new DisplayPreferences();
        prefs.setPreset(CurrencyPreset.CUSTOM);
        prefs.setCustomSymbol("CHF");
        prefs.setCustomLocaleTag("de-CH");
        CurrencyFormatter formatter = new CurrencyFormatter(prefs);

        String formatted = formatter.format(BigDecimal.valueOf(350_000));
        assertTrue(formatted.startsWith("CHF "));
    }

    @Test
    void customPreset_missingFields_fallsBackToZar() {
        DisplayPreferences prefs = new DisplayPreferences();
        prefs.setPreset(CurrencyPreset.CUSTOM);
        CurrencyFormatter formatter = new CurrencyFormatter(prefs);

        assertEquals("R 100", formatter.format(BigDecimal.valueOf(100)));
    }

    @Test
    void formatWithPrefix_includesPrefix() {
        CurrencyFormatter formatter = CurrencyFormatter.defaults();

        String formatted = formatter.formatWithPrefix("< ", BigDecimal.valueOf(2_000_000));
        assertTrue(formatted.startsWith("< R "));
        assertTrue(formatted.contains("2"));
    }

    @Test
    void formatDiscount_includesSymbolAndPercent() {
        CurrencyFormatter formatter = CurrencyFormatter.defaults();

        String formatted = formatter.formatDiscount(50_000, 12.5);
        assertTrue(formatted.startsWith("−R"));
        assertTrue(formatted.contains("50"));
        assertTrue(formatted.endsWith("12.5%"));
    }
}
