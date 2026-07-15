package za.driver.model;

public enum CurrencyPreset {
    ZAR("R", "ZAR", "en-ZA"),
    USD("$", "USD", "en-US"),
    EUR("€", "EUR", "de-DE"),
    GBP("£", "GBP", "en-GB"),
    CUSTOM(null, null, null);

    private final String symbol;
    private final String code;
    private final String localeTag;

    CurrencyPreset(String symbol, String code, String localeTag) {
        this.symbol = symbol;
        this.code = code;
        this.localeTag = localeTag;
    }

    public String symbol() {
        return symbol;
    }

    public String code() {
        return code;
    }

    public String localeTag() {
        return localeTag;
    }
}
