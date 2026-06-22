package za.driver.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Pricing {

    private BigDecimal priceZar;
    private LocalDate priceDate;

    public Pricing() {
    }

    public BigDecimal getPriceZar() {
        return priceZar;
    }

    public void setPriceZar(BigDecimal priceZar) {
        this.priceZar = priceZar;
    }

    public LocalDate getPriceDate() {
        return priceDate;
    }

    public void setPriceDate(LocalDate priceDate) {
        this.priceDate = priceDate;
    }
}
