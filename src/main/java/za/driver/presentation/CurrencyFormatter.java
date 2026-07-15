package za.driver.presentation;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import za.driver.model.CurrencyPreset;
import za.driver.model.DisplayPreferences;

public final class CurrencyFormatter {

    private final String symbol;
    private final String code;
    private final NumberFormat numberFormat;

    public CurrencyFormatter(DisplayPreferences preferences) {
        DisplayPreferences prefs = preferences != null ? preferences : new DisplayPreferences();
        CurrencyPreset preset = prefs.getPreset();
        if (preset == CurrencyPreset.CUSTOM) {
            String customSymbol = prefs.getCustomSymbol();
            String customLocale = prefs.getCustomLocaleTag();
            if (customSymbol != null && !customSymbol.isBlank()
                    && customLocale != null && !customLocale.isBlank()) {
                this.symbol = customSymbol.trim();
                this.code = customSymbol.trim();
                this.numberFormat = NumberFormat.getIntegerInstance(Locale.forLanguageTag(customLocale.trim()));
            } else {
                this.symbol = CurrencyPreset.ZAR.symbol();
                this.code = CurrencyPreset.ZAR.code();
                this.numberFormat = NumberFormat.getIntegerInstance(
                        Locale.forLanguageTag(CurrencyPreset.ZAR.localeTag()));
            }
        } else {
            this.symbol = preset.symbol();
            this.code = preset.code();
            this.numberFormat = NumberFormat.getIntegerInstance(Locale.forLanguageTag(preset.localeTag()));
        }
    }

    public static CurrencyFormatter defaults() {
        return new CurrencyFormatter(new DisplayPreferences());
    }

    public String format(BigDecimal value) {
        if (value == null) {
            return "-";
        }
        return symbol + " " + numberFormat.format(value);
    }

    public String format(double value) {
        return symbol + " " + numberFormat.format(Math.round(value));
    }

    public String formatWithPrefix(String prefix, BigDecimal value) {
        if (value == null) {
            return prefix + "-";
        }
        return prefix + symbol + " " + numberFormat.format(value);
    }

    public String formatDiscount(double discountAmount, double discountPercent) {
        return String.format(
                Locale.ROOT,
                "−%s%s / −%.1f%%",
                symbol,
                numberFormat.format(Math.round(discountAmount)),
                discountPercent);
    }

    public String formatNumberOnly(double value) {
        return numberFormat.format(Math.round(value));
    }

    public String priceFieldLabel(String base) {
        return base + " (" + code + ")";
    }

    public String scorePer100kLabel() {
        return "Score/" + symbol + "100k";
    }

    public String symbol() {
        return symbol;
    }

    public String code() {
        return code;
    }
}
