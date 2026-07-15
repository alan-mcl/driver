package za.driver.model;

public class DisplayPreferences {

    private CurrencyPreset preset = CurrencyPreset.ZAR;
    private String customSymbol;
    private String customLocaleTag;

    public CurrencyPreset getPreset() {
        return preset != null ? preset : CurrencyPreset.ZAR;
    }

    public void setPreset(CurrencyPreset preset) {
        this.preset = preset;
    }

    public String getCustomSymbol() {
        return customSymbol;
    }

    public void setCustomSymbol(String customSymbol) {
        this.customSymbol = customSymbol;
    }

    public String getCustomLocaleTag() {
        return customLocaleTag;
    }

    public void setCustomLocaleTag(String customLocaleTag) {
        this.customLocaleTag = customLocaleTag;
    }
}
