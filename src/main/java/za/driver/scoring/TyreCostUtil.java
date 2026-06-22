package za.driver.scoring;

import static za.driver.scoring.ScoreUtil.inverseScale;
import static za.driver.scoring.ScoringConstants.TYRE_RIM_MAX;
import static za.driver.scoring.ScoringConstants.TYRE_RIM_MIN;
import static za.driver.scoring.ScoringConstants.TYRE_WIDTH_MAX;
import static za.driver.scoring.ScoringConstants.TYRE_WIDTH_MIN;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class TyreCostUtil {

    private static final Pattern TYRE_SIZE_PATTERN =
            Pattern.compile("(\\d{3})/\\d{2}\\s*R(\\d{2})", Pattern.CASE_INSENSITIVE);

    private TyreCostUtil() {
    }

    static Double tyreCostScore(String tyreSize) {
        if (tyreSize == null || tyreSize.isBlank()) {
            return null;
        }

        Matcher matcher = TYRE_SIZE_PATTERN.matcher(tyreSize.trim());
        if (!matcher.find()) {
            return null;
        }

        int sectionWidth = Integer.parseInt(matcher.group(1));
        int rimInches = Integer.parseInt(matcher.group(2));

        Double rimScore = inverseScale(rimInches, TYRE_RIM_MIN, TYRE_RIM_MAX);
        if (rimScore == null) {
            return null;
        }

        Double widthScore = inverseScale(sectionWidth, TYRE_WIDTH_MIN, TYRE_WIDTH_MAX);
        if (widthScore == null) {
            return rimScore;
        }

        return 0.75 * rimScore + 0.25 * widthScore;
    }
}
