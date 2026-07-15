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
            markup.append("<span class=\"star\">");
            markup.append("<span class=\"star-empty\" aria-hidden=\"true\">&#9734;</span>");
            if (fill > 0.0) {
                int clipRightPercent = (int) Math.round((1.0 - fill) * 100.0);
                markup.append("<span class=\"star-filled\" aria-hidden=\"true\" style=\"clip-path:inset(0 ")
                        .append(clipRightPercent)
                        .append("% 0 0)\">&#9733;</span>");
            }
            markup.append("</span>");
        }
        return markup.toString();
    }
}
