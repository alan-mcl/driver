package za.driver.presentation;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import za.driver.model.ScoringProfile;

public final class RevealPresentationBuilder {

    private static final DateTimeFormatter EXPORT_TIMESTAMP =
            DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm");

    private RevealPresentationBuilder() {
    }

    public static String build(
            List<BodyTypeSection> sections,
            ScoringProfile profile,
            LocalDateTime exportedAt) {
        int modelCount = sections.stream().mapToInt(section -> section.models().size()).sum();
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("  <meta charset=\"utf-8\">\n");
        html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("  <title>Driver Vehicle Presentation</title>\n");
        html.append("  <link rel=\"stylesheet\" href=\"reveal/dist/reveal.css\">\n");
        html.append("  <link rel=\"stylesheet\" href=\"reveal/dist/theme/black.css\">\n");
        html.append("  <link rel=\"stylesheet\" href=\"css/driver-presentation.css\">\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("  <div class=\"reveal\">\n");
        html.append("    <div class=\"slides\">\n");

        appendTitleSlide(html, modelCount, profile, exportedAt);
        for (BodyTypeSection section : sections) {
            appendBodyTypeSection(html, section);
        }
        appendClosingSlide(html, exportedAt);

        html.append("    </div>\n");
        html.append("  </div>\n");
        html.append("  <script src=\"reveal/dist/reveal.js\"></script>\n");
        html.append("  <script>\n");
        html.append("    Reveal.initialize({ hash: true, slideNumber: true, transition: 'slide' });\n");
        html.append("  </script>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        return html.toString();
    }

    private static void appendTitleSlide(
            StringBuilder html,
            int modelCount,
            ScoringProfile profile,
            LocalDateTime exportedAt) {
        String profileName = profile != null && profile.getName() != null ? profile.getName() : "Default";
        html.append("      <section class=\"title-slide\">\n");
        html.append("        <img class=\"brand-logo\" src=\"assets/logo.png\" alt=\"Driver\">\n");
        html.append("        <h1>Vehicle Shortlist</h1>\n");
        html.append("        <p class=\"subtitle\">").append(HtmlEscaper.escape(profileName)).append("</p>\n");
        html.append("        <p class=\"meta\">")
                .append(HtmlEscaper.escape(exportedAt.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))))
                .append(" &middot; ")
                .append(modelCount)
                .append(modelCount == 1 ? " model" : " models")
                .append("</p>\n");
        html.append("      </section>\n");
    }

    private static void appendBodyTypeSection(StringBuilder html, BodyTypeSection section) {
        html.append("      <section class=\"body-type-slide\">\n");
        html.append("        <h2>").append(HtmlEscaper.escape(section.label())).append("</h2>\n");
        html.append("      </section>\n");
        for (ModelGroup group : section.models()) {
            appendModelSlide(html, group);
        }
    }

    private static void appendModelSlide(StringBuilder html, ModelGroup group) {
        String displayName = group.displayName();
        String imagePath = "images/" + group.imageFilename();
        html.append("      <section class=\"model-slide\" data-model=\"")
                .append(HtmlEscaper.escape(group.imageSlug()))
                .append("\">\n");
        html.append("        <div class=\"hero-frame\">\n");
        html.append("          <img class=\"hero\" src=\"")
                .append(HtmlEscaper.escape(imagePath))
                .append("\" alt=\"")
                .append(HtmlEscaper.escape(displayName))
                .append("\" onerror=\"this.classList.add('missing')\">\n");
        html.append("          <div class=\"hero-placeholder\">\n");
        html.append("            <p>Drop image here</p>\n");
        html.append("            <code>images/")
                .append(HtmlEscaper.escape(group.imageFilename()))
                .append("</code>\n");
        html.append("          </div>\n");
        html.append("          <div class=\"hero-overlay\">\n");
        html.append("            <h2>").append(HtmlEscaper.escape(displayName)).append("</h2>\n");
        if (!group.yearRange().isBlank()) {
            html.append("            <p class=\"year\">")
                    .append(HtmlEscaper.escape(group.yearRange()))
                    .append("</p>\n");
        }
        appendTrimPriceOverlay(html, group);
        html.append("          </div>\n");
        html.append("        </div>\n");
        html.append("        <div class=\"model-details\">\n");
        appendTrimRatingsPane(html, group);
        if (!group.highlightsBlurb().isBlank()) {
            html.append("          <div class=\"highlights\">\n");
            html.append("            <h3>Highlights</h3>\n");
            html.append("            <p class=\"highlights-blurb\">")
                    .append(HtmlEscaper.escape(group.highlightsBlurb()))
                    .append("</p>\n");
            html.append("          </div>\n");
        }
        html.append("        </div>\n");
        html.append("      </section>\n");
    }

    private static void appendTrimPriceOverlay(StringBuilder html, ModelGroup group) {
        html.append("            <ul class=\"trim-prices-overlay\">\n");
        for (TrimEntry trim : group.trims()) {
            html.append("              <li><span class=\"trim\">")
                    .append(HtmlEscaper.escape(trim.label()))
                    .append("</span><span class=\"price\">")
                    .append(HtmlEscaper.escape(formatPrice(trim.priceZar())))
                    .append("</span></li>\n");
        }
        if (group.hiddenTrimCount() > 0) {
            html.append("              <li class=\"trim-more\">+ ")
                    .append(group.hiddenTrimCount())
                    .append(group.hiddenTrimCount() == 1 ? " more trim" : " more trims")
                    .append("</li>\n");
        }
        html.append("            </ul>\n");
    }

    private static void appendTrimRatingsPane(StringBuilder html, ModelGroup group) {
        if (group.trims().isEmpty()) {
            return;
        }
        html.append("          <div class=\"trim-ratings\">\n");
        html.append("            <h3>Ratings</h3>\n");
        for (TrimEntry trim : group.trims()) {
            html.append("            <div class=\"trim-rating-block\">\n");
            html.append("              <h4>").append(HtmlEscaper.escape(trim.label())).append("</h4>\n");
            if (trim.ratings().isEmpty()) {
                html.append("              <p class=\"no-ratings\">Not yet scored</p>\n");
            } else {
                html.append("              <ul>\n");
                for (RatingEntry rating : trim.ratings()) {
                    html.append("                <li");
                    if ("Overall".equals(rating.label())) {
                        html.append(" class=\"overall-rating\"");
                    }
                    html.append("><span class=\"rating-label\">")
                            .append(HtmlEscaper.escape(rating.label()))
                            .append("</span><span class=\"rating-value\">")
                            .append(String.format(Locale.US, "%.1f/5 ", rating.starsOutOfFive()))
                            .append(ScoreFormatter.starMarkup(rating.starsOutOfFive()))
                            .append("</span></li>\n");
                }
                html.append("              </ul>\n");
            }
            html.append("            </div>\n");
        }
        html.append("          </div>\n");
    }

    private static void appendClosingSlide(StringBuilder html, LocalDateTime exportedAt) {
        html.append("      <section class=\"closing-slide\">\n");
        html.append("        <h2>Generated by Driver</h2>\n");
        html.append("        <p>").append(HtmlEscaper.escape(exportedAt.format(EXPORT_TIMESTAMP))).append("</p>\n");
        html.append("      </section>\n");
    }

    static String formatPrice(BigDecimal priceZar) {
        if (priceZar == null) {
            return "Price TBC";
        }
        NumberFormat currency = NumberFormat.getIntegerInstance(new Locale("en", "ZA"));
        return "R " + currency.format(priceZar);
    }
}
