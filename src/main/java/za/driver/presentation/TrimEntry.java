package za.driver.presentation;

import java.math.BigDecimal;
import java.util.List;

public record TrimEntry(String label, BigDecimal listPriceZar, List<RatingEntry> ratings) {

    public TrimEntry {
        ratings = List.copyOf(ratings);
    }
}
