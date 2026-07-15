package za.driver.presentation;

public final class PriceBandLabels {

    static final String UNPRICED_SECTION = "Price TBC";

    private PriceBandLabels() {
    }

    public static String displayName(long upperBound, CurrencyFormatter formatter) {
        CurrencyFormatter fmt = formatter != null ? formatter : CurrencyFormatter.defaults();
        return "< " + fmt.symbol() + fmt.formatNumberOnly(upperBound / 1000.0) + "k";
    }
}
