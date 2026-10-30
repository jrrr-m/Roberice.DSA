package com.roberice.pos.ui.common;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom 2D Graphics charts matching the Roberice Dashboard Design:
 * 1. 7-Day Sales Trend Bar Chart (soft sage green bars, dark active bar for current date, dashed reference lines)
 * 2. Product Categories / Variety Sales Donut Chart (earthy rice palette + clean legend)
 */
public class CustomCharts {

    public static class BarItem {
        public String label;
        public double value;
        public BarItem(String label, double value) {
            this.label = label;
            this.value = value;
        }
    }

    public static class SliceItem {
        public String name;
        public double value;
        public Color color;
        public SliceItem(String name, double value, Color color) {
            this.name = name;
            this.value = value;
            this.color = color;
        }
    }

    /**
     * Polished 7-Day Bar Chart matching the user image.
     */
    public static class SalesTrendBarChart extends JComponent {
        private List<BarItem> items = new ArrayList<>();
        private static final Color COLOR_BAR_NORMAL = new Color(185, 201, 186); // Soft sage green #B9C9BA
        private static final Color COLOR_BAR_ACTIVE = new Color(27, 77, 50);    // Dark forest green #1B4D32
        private static final Color COLOR_GRID = new Color(230, 236, 230);
        private static final Color COLOR_AXIS_TEXT = new Color(130, 140, 132);

        public SalesTrendBarChart() {
            setPreferredSize(new Dimension(460, 220));
            setOpaque(false);
        }

        public void setData(List<BarItem> data) {
            this.items = data != null ? data : new ArrayList<>();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int paddingLeft = 54;
            int paddingRight = 24;
            int paddingTop = 20;
            int paddingBottom = 36;

            int chartW = w - paddingLeft - paddingRight;
            int chartH = h - paddingTop - paddingBottom;

            if (chartW <= 0 || chartH <= 0) {
                g2.dispose();
                return;
            }

            double maxVal = 1000;
            for (BarItem item : items) {
                if (item.value > maxVal) maxVal = item.value;
            }
            maxVal = Math.max(1000, Math.ceil(maxVal / 1000.0) * 1000.0);

            // Draw horizontal dashed grid lines
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4.0f, 4.0f}, 0.0f);
            g2.setStroke(dashed);

            int gridSteps = 4;
            for (int i = 0; i <= gridSteps; i++) {
                int y = paddingTop + (int) (chartH * (1.0 - (double) i / gridSteps));
                double val = (maxVal / gridSteps) * i;
                g2.setColor(COLOR_GRID);
                g2.drawLine(paddingLeft, y, paddingLeft + chartW, y);

                g2.setColor(COLOR_AXIS_TEXT);
                String label;
                if (val >= 1000) {
                    label = String.format("%.1fk", val / 1000.0);
                } else {
                    label = String.valueOf((int) val);
                }
                int strW = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, paddingLeft - strW - 10, y + 4);
            }

            if (items.isEmpty()) {
                g2.setColor(COLOR_AXIS_TEXT);
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                String emptyMsg = "No sales recorded for this period.";
                int emW = g2.getFontMetrics().stringWidth(emptyMsg);
                g2.drawString(emptyMsg, paddingLeft + (chartW - emW) / 2, paddingTop + chartH / 2);
                g2.dispose();
                return;
            }

            int n = items.size();
            int slotW = chartW / n;
            int barW = Math.max(18, Math.min(30, slotW - 18));

            for (int i = 0; i < n; i++) {
                BarItem it = items.get(i);
                int x = paddingLeft + (i * slotW) + (slotW - barW) / 2;
                int barH = (int) Math.round((it.value / maxVal) * chartH);
                if (barH < 3 && it.value > 0) barH = 3;
                int y = paddingTop + chartH - barH;

                // Active bar is the last item (today)
                boolean isToday = (i == n - 1);
                g2.setColor(isToday ? COLOR_BAR_ACTIVE : COLOR_BAR_NORMAL);
                g2.fillRoundRect(x, y, barW, barH, 4, 4);

                // Date label below
                g2.setColor(COLOR_AXIS_TEXT);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                int lblW = g2.getFontMetrics().stringWidth(it.label);
                g2.drawString(it.label, x + (barW - lblW) / 2, paddingTop + chartH + 18);
            }

            g2.dispose();
        }
    }

    /**
     * Centered Variety Sales Donut Chart with elegant legend list.
     */
    public static class VarietyDonutChart extends JPanel {
        private List<SliceItem> slices = new ArrayList<>();
        private final JPanel chartCanvas;
        private final JPanel legendPanel;

        // Earthy Rice Merchandising Palette from design
        public static final Color[] PALETTE = {
            new Color(27, 77, 50),   // Deep Rice Green
            new Color(180, 135, 45), // Golden Amber
            new Color(88, 138, 105), // Sage Green
            new Color(205, 168, 85), // Wheat Gold
            new Color(110, 150, 120),// Soft Forest
            new Color(195, 140, 60), // Warm Khaki
            new Color(70, 95, 65),   // Olive Green
            new Color(215, 180, 110),// Pale Husk
            new Color(55, 90, 70),   // Spruce Green
            new Color(165, 120, 50)  // Toasted Rice
        };

        public VarietyDonutChart() {
            setLayout(new BorderLayout(16, 0));
            setOpaque(false);

            chartCanvas = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();
                    int size = Math.min(w, h) - 16;
                    if (size <= 0) { g2.dispose(); return; }

                    int cx = (w - size) / 2;
                    int cy = (h - size) / 2;

                    double total = 0;
                    for (SliceItem s : slices) total += s.value;

                    if (total <= 0) {
                        g2.setColor(new Color(234, 230, 218));
                        g2.setStroke(new BasicStroke(24f));
                        g2.drawOval(cx + 12, cy + 12, size - 24, size - 24);
                        g2.setColor(new Color(140, 150, 140));
                        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                        String msg = "No Sales Yet";
                        int mw = g2.getFontMetrics().stringWidth(msg);
                        g2.drawString(msg, (w - mw) / 2, h / 2 + 4);
                        g2.dispose();
                        return;
                    }

                    double curAngle = 90.0;
                    for (SliceItem slice : slices) {
                        double sweep = (slice.value / total) * 360.0;
                        g2.setColor(slice.color);
                        g2.fill(new Arc2D.Double(cx, cy, size, size, curAngle, -sweep, Arc2D.PIE));

                        // Divider lines
                        g2.setColor(new Color(255, 253, 246));
                        g2.setStroke(new BasicStroke(2.0f));
                        g2.draw(new Arc2D.Double(cx, cy, size, size, curAngle, -sweep, Arc2D.PIE));

                        curAngle -= sweep;
                    }

                    // Inner circle for donut hole (matching card bg #FFFCF6)
                    int innerSize = (int) (size * 0.54);
                    int inX = cx + (size - innerSize) / 2;
                    int inY = cy + (size - innerSize) / 2;
                    g2.setColor(new Color(255, 253, 246));
                    g2.fillOval(inX, inY, innerSize, innerSize);

                    g2.dispose();
                }
            };
            chartCanvas.setOpaque(false);
            chartCanvas.setPreferredSize(new Dimension(170, 170));

            legendPanel = new JPanel();
            legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
            legendPanel.setOpaque(false);

            JScrollPane scrollLegend = new JScrollPane(legendPanel);
            scrollLegend.setBorder(null);
            scrollLegend.setOpaque(false);
            scrollLegend.getViewport().setOpaque(false);
            scrollLegend.setPreferredSize(new Dimension(150, 170));

            add(chartCanvas, BorderLayout.CENTER);
            add(scrollLegend, BorderLayout.EAST);
        }

        public void setData(List<SliceItem> data) {
            this.slices = data != null ? data : new ArrayList<>();
            rebuildLegend();
            chartCanvas.repaint();
        }

        private void rebuildLegend() {
            legendPanel.removeAll();
            double total = 0;
            for (SliceItem s : slices) total += s.value;

            for (SliceItem item : slices) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
                row.setOpaque(false);

                // Color box
                JPanel box = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        g.setColor(item.color);
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
                box.setPreferredSize(new Dimension(9, 9));

                int pct = (total > 0) ? (int) Math.round((item.value / total) * 100.0) : 0;
                JLabel lbl = new JLabel(item.name + ": " + pct + "%");
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                lbl.setForeground(new Color(60, 70, 65));

                row.add(box);
                row.add(lbl);
                legendPanel.add(row);
            }
            legendPanel.revalidate();
            legendPanel.repaint();
        }
    }
}
