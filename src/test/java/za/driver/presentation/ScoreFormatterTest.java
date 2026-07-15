package za.driver.presentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ScoreFormatterTest {

    @Test
    void starMarkup_rendersFullStarsWithSliceMarkup() {
        String markup = ScoreFormatter.starMarkup(3.0);

        assertTrue(markup.contains("class=\"star-filled\""));
        assertFalse(markup.contains("class=\"star filled\""));
        assertFalse(markup.contains("class=\"star half\""));
        assertFalse(markup.contains("opacity"));
    }

    @Test
    void starMarkup_rendersFractionalStarWithClipPath() {
        String markup = ScoreFormatter.starMarkup(3.7);

        assertTrue(markup.contains("clip-path:inset(0 30% 0 0)"));
        assertTrue(markup.contains("class=\"star-empty\""));
    }

    @Test
    void starMarkup_rendersEmptyStarsWithoutFilledOverlay() {
        String markup = ScoreFormatter.starMarkup(0.0);

        assertTrue(markup.contains("class=\"star-empty\""));
        assertFalse(markup.contains("class=\"star-filled\""));
    }
}
