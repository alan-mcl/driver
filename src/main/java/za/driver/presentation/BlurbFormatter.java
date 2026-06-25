package za.driver.presentation;

public final class BlurbFormatter {

    static final int MAX_WORDS = 50;

    private BlurbFormatter() {
    }

    public static String truncateToWords(String text, int maxWords) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String trimmed = text.trim().replaceAll("\\s+", " ");
        String[] words = trimmed.split(" ");
        if (words.length <= maxWords) {
            return trimmed;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < maxWords; i++) {
            if (i > 0) {
                result.append(' ');
            }
            result.append(words[i]);
        }
        result.append('\u2026');
        return result.toString();
    }
}
