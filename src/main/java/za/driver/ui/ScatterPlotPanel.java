package za.driver.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import za.driver.chart.ScatterPlotAxis;
import za.driver.chart.ScatterPlotData;
import za.driver.chart.ScatterPlotPoint;
import za.driver.chart.ScatterPlotStatistics;
import za.driver.model.Vehicle;

public class ScatterPlotPanel extends JPanel {

    private static final int MARGIN_LEFT = 72;
    private static final int MARGIN_BOTTOM = 56;
    private static final int MARGIN_TOP = 16;
    private static final int MARGIN_RIGHT = 140;
    private static final int POINT_RADIUS = 6;
    private static final int HIT_RADIUS = 12;
    private static final int TICK_COUNT = 5;
    private static final int LABEL_PADDING = 2;
    private static final Color REFERENCE_LINE_COLOR = new Color(190, 190, 190);
    private static final float[] DASH_PATTERN = {4f, 4f};

    private record LabelPlacement(ScatterPlotPoint point, int x, int baselineY) {
    }

    private ScatterPlotData plotData = new ScatterPlotData(List.of(), Map.of(), Map.of(), 0);
    private ScatterPlotAxis xAxis = ScatterPlotAxis.PRICE;
    private ScatterPlotAxis yAxis = ScatterPlotAxis.OVERALL_SCORE;
    private List<ScatterPlotPoint> renderedPoints = List.of();
    private Consumer<Vehicle> pointSelectedListener;

    public ScatterPlotPanel() {
        setPreferredSize(new Dimension(720, 480));
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
            ScatterPlotPoint point = findPointAt(event.getX(), event.getY());
            if (point != null && point.tooltipText() != null && !point.tooltipText().isBlank()) {
                return point.tooltipText();
            }
            return null;
        }
        return super.getToolTipText(event);
    }

    public void setPointSelectedListener(Consumer<Vehicle> listener) {
        this.pointSelectedListener = listener;
    }

    public void setPlot(ScatterPlotData data, ScatterPlotAxis xAxis, ScatterPlotAxis yAxis) {
        this.plotData = data != null ? data : new ScatterPlotData(List.of(), Map.of(), Map.of(), 0);
        this.xAxis = xAxis;
        this.yAxis = yAxis;
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

            List<ScatterPlotPoint> points = plotData.points();
            if (points.isEmpty()) {
                drawCenteredMessage(g, "No plottable vehicles for the selected axes.");
                drawLegend(g, plotRight, plotTop, plotData.legend(), plotData.legendLabels());
                return;
            }

            double xMin = points.stream().mapToDouble(ScatterPlotPoint::x).min().orElse(0);
            double xMax = points.stream().mapToDouble(ScatterPlotPoint::x).max().orElse(1);
            double yMin = points.stream().mapToDouble(ScatterPlotPoint::y).min().orElse(0);
            double yMax = points.stream().mapToDouble(ScatterPlotPoint::y).max().orElse(1);

            double[] xRange = paddedRange(xMin, xMax);
            double[] yRange = paddedRange(yMin, yMax);

            drawTicks(g, plotLeft, plotBottom, plotWidth, true, xRange[0], xRange[1], xAxis);
            drawTicks(g, plotLeft, plotTop, plotHeight, false, yRange[0], yRange[1], yAxis);

            Font labelFont = g.getFont().deriveFont(Font.BOLD, 11f);
            g.setFont(labelFont);
            drawAxisTitle(g, xAxis.label(), plotLeft + plotWidth / 2, getHeight() - 12, true);
            drawAxisTitle(g, yAxis.label(), 14, plotTop + plotHeight / 2, false);

            drawReferenceLines(g, points, xRange, yRange, plotLeft, plotTop, plotWidth, plotHeight);

            Font pointLabelFont = g.getFont().deriveFont(Font.PLAIN, 10f);
            g.setFont(pointLabelFont);
            FontMetrics labelMetrics = g.getFontMetrics();

            List<ScatterPlotPoint> positioned = new ArrayList<>();
            for (ScatterPlotPoint point : points) {
                int px = toPixelX(point.x(), xRange[0], xRange[1], plotLeft, plotWidth);
                int py = toPixelY(point.y(), yRange[0], yRange[1], plotTop, plotHeight);
                positioned.add(point.withPixelPosition(px, py));

                g.setColor(point.color());
                g.fill(new Ellipse2D.Double(px - POINT_RADIUS, py - POINT_RADIUS, POINT_RADIUS * 2.0, POINT_RADIUS * 2.0));
            }
            renderedPoints = positioned;

            List<LabelPlacement> labels = layoutLabels(positioned, labelMetrics, plotLeft, plotTop, plotRight, plotBottom);
            for (LabelPlacement placement : labels) {
                g.setColor(placement.point().color());
                g.drawString(placement.point().label(), placement.x(), placement.baselineY());
            }

            drawLegend(g, plotRight, plotTop, plotData.legend(), plotData.legendLabels());
        } finally {
            g.dispose();
        }
    }

    private void handleClick(int x, int y) {
        if (pointSelectedListener == null || renderedPoints.isEmpty()) {
            return;
        }
        ScatterPlotPoint nearest = findPointAt(x, y);
        if (nearest != null) {
            pointSelectedListener.accept(nearest.vehicle());
        }
    }

    private ScatterPlotPoint findPointAt(int x, int y) {
        ScatterPlotPoint nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (ScatterPlotPoint point : renderedPoints) {
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

    private static List<LabelPlacement> layoutLabels(
            List<ScatterPlotPoint> points,
            FontMetrics metrics,
            int plotLeft,
            int plotTop,
            int plotRight,
            int plotBottom) {
        List<ScatterPlotPoint> sorted = new ArrayList<>(points);
        sorted.sort(Comparator.comparingInt(ScatterPlotPoint::pixelY).thenComparingInt(ScatterPlotPoint::pixelX));

        List<Rectangle> occupied = new ArrayList<>();
        List<LabelPlacement> placements = new ArrayList<>();

        for (ScatterPlotPoint point : sorted) {
            occupied.add(new Rectangle(
                    point.pixelX() - POINT_RADIUS,
                    point.pixelY() - POINT_RADIUS,
                    POINT_RADIUS * 2,
                    POINT_RADIUS * 2));

            String text = point.label();
            if (text == null || text.isBlank()) {
                continue;
            }

            int textWidth = metrics.stringWidth(text);
            int textHeight = metrics.getHeight();
            int ascent = metrics.getAscent();
            int px = point.pixelX();
            int py = point.pixelY();

            int[][] candidates = {
                    {px + POINT_RADIUS + 2, py - POINT_RADIUS},
                    {px + POINT_RADIUS + 2, py + POINT_RADIUS + ascent},
                    {px - POINT_RADIUS - 2 - textWidth, py - POINT_RADIUS},
                    {px - POINT_RADIUS - 2 - textWidth, py + POINT_RADIUS + ascent},
                    {px - textWidth / 2, py - POINT_RADIUS - 4},
                    {px - textWidth / 2, py + POINT_RADIUS + ascent + 4},
                    {px - textWidth - POINT_RADIUS - 2, py - textHeight / 2 + ascent / 2},
                    {px + POINT_RADIUS + 2, py - textHeight / 2 + ascent / 2},
            };

            for (int[] candidate : candidates) {
                Rectangle bounds = new Rectangle(candidate[0], candidate[1] - ascent, textWidth, textHeight);
                bounds.grow(LABEL_PADDING, LABEL_PADDING);
                if (fitsInPlot(bounds, plotLeft, plotTop, plotRight, plotBottom)
                        && !intersectsAny(bounds, occupied)) {
                    placements.add(new LabelPlacement(point, candidate[0], candidate[1]));
                    occupied.add(bounds);
                    break;
                }
            }
        }

        return placements;
    }

    private static boolean fitsInPlot(Rectangle bounds, int plotLeft, int plotTop, int plotRight, int plotBottom) {
        return bounds.x >= plotLeft
                && bounds.y >= plotTop
                && bounds.x + bounds.width <= plotRight
                && bounds.y + bounds.height <= plotBottom;
    }

    private static boolean intersectsAny(Rectangle bounds, List<Rectangle> occupied) {
        for (Rectangle area : occupied) {
            if (bounds.intersects(area)) {
                return true;
            }
        }
        return false;
    }

    private static void drawReferenceLines(
            Graphics2D g,
            List<ScatterPlotPoint> points,
            double[] xRange,
            double[] yRange,
            int plotLeft,
            int plotTop,
            int plotWidth,
            int plotHeight) {
        g.setColor(REFERENCE_LINE_COLOR);
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, DASH_PATTERN, 0f));

        List<Double> xValues = points.stream().mapToDouble(ScatterPlotPoint::x).boxed().toList();
        List<Double> yValues = points.stream().mapToDouble(ScatterPlotPoint::y).boxed().toList();
        double medianX = ScatterPlotStatistics.median(xValues);
        double medianY = ScatterPlotStatistics.median(yValues);

        if (!Double.isNaN(medianX)) {
            int x = toPixelX(medianX, xRange[0], xRange[1], plotLeft, plotWidth);
            g.drawLine(x, plotTop, x, plotTop + plotHeight);
        }
        if (!Double.isNaN(medianY)) {
            int y = toPixelY(medianY, yRange[0], yRange[1], plotTop, plotHeight);
            g.drawLine(plotLeft, y, plotLeft + plotWidth, y);
        }

        ScatterPlotStatistics.linearFit(points).ifPresent(fit -> {
            double yStart = fit.yAt(xRange[0]);
            double yEnd = fit.yAt(xRange[1]);
            int x1 = toPixelX(xRange[0], xRange[0], xRange[1], plotLeft, plotWidth);
            int y1 = toPixelY(yStart, yRange[0], yRange[1], plotTop, plotHeight);
            int x2 = toPixelX(xRange[1], xRange[0], xRange[1], plotLeft, plotWidth);
            int y2 = toPixelY(yEnd, yRange[0], yRange[1], plotTop, plotHeight);
            g.setStroke(new BasicStroke(1f));
            g.draw(new Line2D.Double(x1, y1, x2, y2));
        });
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
            double max,
            ScatterPlotAxis axis) {
        Font tickFont = g.getFont().deriveFont(Font.PLAIN, 10f);
        g.setFont(tickFont);
        g.setColor(Color.DARK_GRAY);
        g.setStroke(new BasicStroke(1f));

        for (int i = 0; i <= TICK_COUNT; i++) {
            double fraction = i / (double) TICK_COUNT;
            double value = min + (max - min) * fraction;
            String label = formatTick(value, axis);
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

    private static double[] paddedRange(double min, double max) {
        if (min == max) {
            double delta = min == 0 ? 1.0 : Math.abs(min) * 0.05;
            return new double[] {min - delta, max + delta};
        }
        double padding = (max - min) * 0.05;
        return new double[] {min - padding, max + padding};
    }

    private static String formatTick(double value, ScatterPlotAxis axis) {
        if (axis == ScatterPlotAxis.PRICE) {
            NumberFormat format = NumberFormat.getIntegerInstance(new Locale("en", "ZA"));
            return format.format(Math.round(value));
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
