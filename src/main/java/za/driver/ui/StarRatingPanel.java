package za.driver.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class StarRatingPanel extends JPanel {

    private static final Color FILLED_COLOR = new Color(218, 165, 32);
    private static final Color EMPTY_COLOR = new Color(200, 200, 200);
    private static final int STAR_COUNT = 5;
    private static final int STAR_OUTER_RADIUS = 7;
    private static final int STAR_SPACING = 30;
    private static final int ROW_HEIGHT = 32;
    private static final int LABEL_WIDTH = 120;
    private static final int SCORE_WIDTH = 48;
    private static final int STARS_WIDTH = STAR_OUTER_RADIUS * 2 + (STAR_COUNT - 1) * STAR_SPACING + 8;

    private final JLabel label = new JLabel();
    private final JLabel scoreLabel = new JLabel();
    private final JPanel starsPanel;
    private Double score;

    public StarRatingPanel(String text) {
        super(new GridBagLayout());
        setPreferredSize(new Dimension(LABEL_WIDTH + STARS_WIDTH + SCORE_WIDTH + 24, ROW_HEIGHT));
        setMinimumSize(getPreferredSize());
        setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));

        label.setText(text);
        label.setPreferredSize(new Dimension(LABEL_WIDTH, ROW_HEIGHT));
        scoreLabel.setPreferredSize(new Dimension(SCORE_WIDTH, ROW_HEIGHT));

        starsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintStars((Graphics2D) g, getWidth(), getHeight());
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(STARS_WIDTH, ROW_HEIGHT);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        starsPanel.setOpaque(false);

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(0, 0, 0, 16);
        add(label, labelConstraints);

        GridBagConstraints starsConstraints = new GridBagConstraints();
        starsConstraints.gridx = 1;
        starsConstraints.anchor = GridBagConstraints.WEST;
        starsConstraints.insets = new Insets(0, 0, 0, 16);
        add(starsPanel, starsConstraints);

        GridBagConstraints scoreConstraints = new GridBagConstraints();
        scoreConstraints.gridx = 2;
        scoreConstraints.anchor = GridBagConstraints.WEST;
        add(scoreLabel, scoreConstraints);

        setOpaque(false);
    }

    public void setScore(Double score) {
        this.score = score;
        if (score == null) {
            scoreLabel.setText("-");
        } else {
            scoreLabel.setText(String.format("%.1f", score));
        }
        starsPanel.repaint();
    }

    public void setLabel(String text) {
        label.setText(text);
    }

    private void paintStars(Graphics2D g, int width, int height) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int centerY = height / 2;
        if (score == null) {
            g.setColor(EMPTY_COLOR);
            for (int i = 0; i < STAR_COUNT; i++) {
                drawStar(g, starCenterX(i), centerY, STAR_OUTER_RADIUS, false);
            }
            return;
        }

        double starValue = score / 20.0;
        for (int i = 0; i < STAR_COUNT; i++) {
            double fill = Math.max(0.0, Math.min(1.0, starValue - i));
            int centerX = starCenterX(i);
            if (fill <= 0.0) {
                g.setColor(EMPTY_COLOR);
                drawStar(g, centerX, centerY, STAR_OUTER_RADIUS, false);
            } else if (fill >= 1.0) {
                g.setColor(FILLED_COLOR);
                drawStar(g, centerX, centerY, STAR_OUTER_RADIUS, true);
            } else {
                g.setColor(EMPTY_COLOR);
                drawStar(g, centerX, centerY, STAR_OUTER_RADIUS, false);
                g.setClip(centerX - STAR_OUTER_RADIUS, 0, (int) Math.round(STAR_OUTER_RADIUS * 2 * fill), height);
                g.setColor(FILLED_COLOR);
                drawStar(g, centerX, centerY, STAR_OUTER_RADIUS, true);
                g.setClip(null);
            }
        }
    }

    private static int starCenterX(int index) {
        return STAR_OUTER_RADIUS + 4 + index * STAR_SPACING;
    }

    private static void drawStar(Graphics2D g, int centerX, int centerY, int outerRadius, boolean filled) {
        Path2D star = new Path2D.Double();
        double innerRadius = outerRadius * 0.45;
        for (int point = 0; point < 10; point++) {
            double angle = Math.PI / 2 + point * Math.PI / 5;
            double radius = point % 2 == 0 ? outerRadius : innerRadius;
            double x = centerX + Math.cos(angle) * radius;
            double y = centerY - Math.sin(angle) * radius;
            if (point == 0) {
                star.moveTo(x, y);
            } else {
                star.lineTo(x, y);
            }
        }
        star.closePath();
        if (filled) {
            g.fill(star);
        } else {
            g.draw(star);
        }
    }
}
