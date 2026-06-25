package za.driver.presentation;

import java.util.Locale;

public final class ScoreFormatter {

    private ScoreFormatter() {
    }

    public static String formatScore(Double score) {
        if (score == null) {
            return "-";
        }
        return String.format(Locale.US, "%.1f", score);
    }

    public static String formatStarRating(Double overallScore) {
        if (overallScore == null) {
            return "Not yet scored";
        }
        double starsOutOfFive = overallScore / 20.0;
        return String.format(Locale.US, "%.1f/5 %s", starsOutOfFive, starMarkup(starsOutOfFive));
    }

    public static String starMarkup(double starsOutOfFive) {
        StringBuilder markup = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            double fill = Math.max(0.0, Math.min(1.0, starsOutOfFive - i));
            if (fill >= 1.0) {
                markup.append("<span class=\"star filled\">&#9733;</span>");
            } else if (fill >= 0.5) {
                markup.append("<span class=\"star half\">&#9733;</span>");
            } else {
                markup.append("<span class=\"star empty\">&#9734;</span>");
            }
        }
        return markup.toString();
    }
}
