package za.driver.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import za.driver.chart.PriceDiscoveryCrossover;
import za.driver.chart.PriceDiscoveryData;
import za.driver.chart.PriceDiscoveryPoint;
import za.driver.chart.PriceDiscoveryVehicle;
import za.driver.model.Vehicle;

public class PriceDiscoveryPanel extends JPanel {

    private static final int MARGIN_LEFT = 72;
    private static final int MARGIN_BOTTOM = 56;
    private static final int MARGIN_TOP = 16;
    private static final int MARGIN_RIGHT = 140;
    private static final int POINT_RADIUS = 6;
    private static final int HIT_RADIUS = 12;
    private static final int TICK_COUNT = 5;
    private static final Color REFERENCE_LINE_COLOR = new Color(60, 60, 60);
    private static final float[] DASH_PATTERN = {6f, 4f};
    private static final float CURVE_ALPHA = 0.55f;

    private record RenderedPoint(Vehicle vehicle, String tooltip, Color color, int pixelX, int pixelY) {
    }

    private PriceDiscoveryData plotData = PriceDiscoveryData.empty(0);
    private List<RenderedPoint> renderedPoints = List.of();
    private Consumer<Vehicle> pointSelectedListener;

    public PriceDiscoveryPanel() {
        setPreferredSize(new Dimension(720, 380));
        setBackground(Color.WHITE);
        ToolTipManager.sharedInstance().registerComponent(this);
        setToolTipText("");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                handleClick(event.getX(), event.getY());
            }
        });
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (event != null) {
            RenderedPoint point = findPointAt(event.getX(), event.getY());
            if (point != null && point.tooltip() != null && !point.tooltip().isBlank()) {
                return point.tooltip();
            }
            return null;
        }
        return super.getToolTipText(event);
    }

    public void setPointSelectedListener(Consumer<Vehicle> listener) {
        this.pointSelectedListener = listener;
    }

    public void setPlot(PriceDiscoveryData data) {
        this.plotData = data != null ? data : PriceDiscoveryData.empty(0);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int plotLeft = MARGIN_LEFT;
            int plotTop = MARGIN_TOP;
            int plotRight = getWidth() - MARGIN_RIGHT;
            int plotBottom = getHeight() - MARGIN_BOTTOM;
            int plotWidth = plotRight - plotLeft;
            int plotHeight = plotBottom - plotTop;

            if (plotWidth <= 0 || plotHeight <= 0) {
                return;
            }

            g.setColor(Color.BLACK);
            g.drawRect(plotLeft, plotTop, plotWidth, plotHeight);

            PriceDiscoveryVehicle benchmark = plotData.benchmark();
            if (benchmark == null) {
                drawCenteredMessage(g, "No plottable vehicles for price discovery.");
                drawLegend(g, plotRight, plotTop, plotData.legend(), plotData.legendLabels());
                renderedPoints = List.of();
                return;
            }

            double xMin = plotData.xMin();
            double xMax = plotData.xMax();
            double yMin = plotData.yMin();
            double yMax = plotData.yMax();

            drawTicks(g, plotLeft, plotBottom, plotWidth, true, xMin, xMax);
            drawTicks(g, plotLeft, plotTop, plotHeight, false, yMin, yMax);

            Font labelFont = g.getFont().deriveFont(Font.BOLD, 11f);
            g.setFont(labelFont);
            drawAxisTitle(g, "Price (ZAR)", plotLeft + plotWidth / 2, getHeight() - 12, true);
            drawAxisTitle(g, "Score/R100k", 14, plotTop + plotHeight / 2, false);

            drawBenchmarkLine(g, benchmark, xMin, xMax, yMin, yMax, plotLeft, plotTop, plotWidth, plotHeight, plotRight);

            for (PriceDiscoveryVehicle subject : plotData.subjects()) {
                drawCurve(g, subject, xMin, xMax, yMin, yMax, plotLeft, plotTop, plotWidth, plotHeight);
            }

            List<RenderedPoint> points = new ArrayList<>();
            for (PriceDiscoveryVehicle subject : plotData.subjects()) {
                drawCrossover(g, subject, plotLeft, plotTop, plotWidth, plotHeight, xMin, xMax, yMin, yMax);
                points.add(renderListPoint(subject, plotLeft, plotTop, plotWidth, plotHeight, xMin, xMax, yMin, yMax));
            }
            points.add(renderListPoint(benchmark, plotLeft, plotTop, plotWidth, plotHeight, xMin, xMax, yMin, yMax));

            for (RenderedPoint point : points) {
                g.setColor(point.color());
                g.fill(new Ellipse2D.Double(
                        point.pixelX() - POINT_RADIUS,
                        point.pixelY() - POINT_RADIUS,
                        POINT_RADIUS * 2.0,
                        POINT_RADIUS * 2.0));
                g.setColor(Color.BLACK);
                g.draw(new Ellipse2D.Double(
                        point.pixelX() - POINT_RADIUS,
                        point.pixelY() - POINT_RADIUS,
                        POINT_RADIUS * 2.0,
                        POINT_RADIUS * 2.0));
            }
            renderedPoints = points;

            drawLegend(g, plotRight, plotTop, plotData.legend(), plotData.legendLabels());
        } finally {
            g.dispose();
        }
    }

    private void drawBenchmarkLine(
            Graphics2D g,
            PriceDiscoveryVehicle benchmark,
            double xMin,
            double xMax,
            double yMin,
            double yMax,
            int plotLeft,
            int plotTop,
            int plotWidth,
            int plotHeight,
            int plotRight) {
        int y = toPixelY(benchmark.listScorePer100k(), yMin, yMax, plotTop, plotHeight);
        g.setColor(REFERENCE_LINE_COLOR);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, DASH_PATTERN, 0f));
        g.drawLine(plotLeft, y, plotLeft + plotWidth, y);

        Font labelFont = g.getFont().deriveFont(Font.PLAIN, 10f);
        g.setFont(labelFont);
        g.setColor(Color.DARK_GRAY);
        String label = String.format(Locale.ROOT, "Benchmark %.1f", benchmark.listScorePer100k());
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(label, plotRight + 6, y + metrics.getAscent() / 2);
    }

    private void drawCurve(
            Graphics2D g,
            PriceDiscoveryVehicle subject,
            double xMin,
            double xMax,
            double yMin,
            double yMax,
            int plotLeft,
            int plotTop,
            int plotWidth,
            int plotHeight) {
        List<PriceDiscoveryPoint> points = subject.curvePoints();
        if (points.size() < 2) {
            return;
        }

        Path2D path = new Path2D.Double();
        boolean started = false;
        for (PriceDiscoveryPoint point : points) {
            int px = toPixelX(point.price(), xMin, xMax, plotLeft, plotWidth);
            int py = toPixelY(point.scorePer100k(), yMin, yMax, plotTop, plotHeight);
            if (!started) {
                path.moveTo(px, py);
                started = true;
            } else {
                path.lineTo(px, py);
            }
        }

        Composite previous = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, CURVE_ALPHA));
        g.setColor(subject.color());
        g.setStroke(new BasicStroke(2f));
        g.draw(path);
        g.setComposite(previous);
    }

    private void drawCrossover(
            Graphics2D g,
            PriceDiscoveryVehicle subject,
            int plotLeft,
            int plotTop,
            int plotWidth,
            int plotHeight,
            double xMin,
            double xMax,
            double yMin,
            double yMax) {
        PriceDiscoveryCrossover crossover = findCrossover(subject.vehicle());
        if (crossover == null || crossover.beatsAtList()) {
            return;
        }
        if (crossover.crossoverPrice() < xMin || crossover.crossoverPrice() > xMax) {
            return;
        }

        int px = toPixelX(crossover.crossoverPrice(), xMin, xMax, plotLeft, plotWidth);
        int py = toPixelY(crossover.crossoverScorePer100k(), yMin, yMax, plotTop, plotHeight);
        int plotBottom = plotTop + plotHeight;

        g.setColor(subject.color());
        g.fill(new Ellipse2D.Double(px - 4, py - 4, 8, 8));

        g.setColor(Color.DARK_GRAY);
        g.setStroke(new BasicStroke(1f));
        g.drawLine(px, plotBottom, px, plotBottom + 6);

        Font tickFont = g.getFont().deriveFont(Font.PLAIN, 9f);
        g.setFont(tickFont);
        String label = formatPrice(crossover.crossoverPrice());
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(label, px - metrics.stringWidth(label) / 2, plotBottom + 18);
    }

    private PriceDiscoveryCrossover findCrossover(Vehicle vehicle) {
        for (PriceDiscoveryCrossover crossover : plotData.crossovers()) {
            if (crossover.subject().getId().equals(vehicle.getId())) {
                return crossover;
            }
        }
        return null;
    }

    private RenderedPoint renderListPoint(
            PriceDiscoveryVehicle entry,
            int plotLeft,
            int plotTop,
            int plotWidth,
            int plotHeight,
            double xMin,
            double xMax,
            double yMin,
            double yMax) {
        int px = toPixelX(entry.listPrice(), xMin, xMax, plotLeft, plotWidth);
        int py = toPixelY(entry.listScorePer100k(), yMin, yMax, plotTop, plotHeight);
        return new RenderedPoint(entry.vehicle(), entry.tooltipText(), entry.color(), px, py);
    }

    private void handleClick(int x, int y) {
        if (pointSelectedListener == null || renderedPoints.isEmpty()) {
            return;
        }
        RenderedPoint nearest = findPointAt(x, y);
        if (nearest != null) {
            pointSelectedListener.accept(nearest.vehicle());
        }
    }

    private RenderedPoint findPointAt(int x, int y) {
        RenderedPoint nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (RenderedPoint point : renderedPoints) {
            double dx = point.pixelX() - x;
            double dy = point.pixelY() - y;
            double distance = Math.hypot(dx, dy);
            if (distance <= HIT_RADIUS && distance < nearestDistance) {
                nearest = point;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static void drawCenteredMessage(Graphics2D g, String message) {
        FontMetrics metrics = g.getFontMetrics();
        int x = (g.getClipBounds().width - metrics.stringWidth(message)) / 2;
        int y = g.getClipBounds().height / 2;
        g.setColor(Color.GRAY);
        g.drawString(message, Math.max(x, MARGIN_LEFT), y);
    }

    private static void drawAxisTitle(Graphics2D g, String title, int centerX, int centerY, boolean horizontal) {
        FontMetrics metrics = g.getFontMetrics();
        if (horizontal) {
            g.drawString(title, centerX - metrics.stringWidth(title) / 2, centerY);
        } else {
            g.rotate(-Math.PI / 2, centerX, centerY);
            g.drawString(title, centerX - metrics.stringWidth(title) / 2, centerY);
            g.rotate(Math.PI / 2, centerX, centerY);
        }
    }

    private void drawTicks(
            Graphics2D g,
            int origin,
            int edge,
            int length,
            boolean horizontal,
            double min,
            double max) {
        Font tickFont = g.getFont().deriveFont(Font.PLAIN, 10f);
        g.setFont(tickFont);
        g.setColor(Color.DARK_GRAY);
        g.setStroke(new BasicStroke(1f));

        for (int i = 0; i <= TICK_COUNT; i++) {
            double fraction = i / (double) TICK_COUNT;
            double value = min + (max - min) * fraction;
            String label = horizontal ? formatPrice(value) : String.format(Locale.ROOT, "%.1f", value);
            FontMetrics metrics = g.getFontMetrics();

            if (horizontal) {
                int x = origin + (int) Math.round(length * fraction);
                g.drawLine(x, edge, x, edge + 4);
                int labelX = x - metrics.stringWidth(label) / 2;
                g.drawString(label, labelX, edge + 16);
            } else {
                int y = edge + (int) Math.round(length * (1.0 - fraction));
                g.drawLine(origin - 4, y, origin, y);
                int labelX = origin - 8 - metrics.stringWidth(label);
                g.drawString(label, Math.max(4, labelX), y + metrics.getAscent() / 2 - 1);
            }
        }
    }

    private static void drawLegend(
            Graphics2D g,
            int legendX,
            int legendY,
            Map<String, Color> legend,
            Map<String, String> legendLabels) {
        if (legend.isEmpty()) {
            return;
        }
        Font legendFont = g.getFont().deriveFont(Font.PLAIN, 10f);
        g.setFont(legendFont);
        FontMetrics metrics = g.getFontMetrics();
        int y = legendY + metrics.getAscent();
        g.setColor(Color.BLACK);
        g.drawString("Brand", legendX, y);
        y += metrics.getHeight() + 4;

        for (Map.Entry<String, Color> entry : legend.entrySet()) {
            g.setColor(entry.getValue());
            g.fillRect(legendX, y - metrics.getAscent() + 2, 10, 10);
            g.setColor(Color.DARK_GRAY);
            String make = legendLabels.getOrDefault(entry.getKey(), entry.getKey());
            g.drawString(make, legendX + 14, y);
            y += metrics.getHeight() + 2;
        }
    }

    private static int toPixelX(double value, double min, double max, int plotLeft, int plotWidth) {
        double fraction = (value - min) / (max - min);
        return plotLeft + (int) Math.round(fraction * plotWidth);
    }

    private static int toPixelY(double value, double min, double max, int plotTop, int plotHeight) {
        double fraction = (value - min) / (max - min);
        return plotTop + plotHeight - (int) Math.round(fraction * plotHeight);
    }

    private static String formatPrice(double value) {
        NumberFormat format = NumberFormat.getIntegerInstance(new Locale("en", "ZA"));
        return format.format(Math.round(value));
    }
}
