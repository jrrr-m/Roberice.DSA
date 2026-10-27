package com.roberice.pos.ui.common;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom 2D Graphics charts for the Executive Store Dashboard:
 * 1. 7-Day Sales Trend Bar Chart
 * 2. Centered Variety Sales Donut Chart with Legend Table
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
     * Polished 7-Day Bar Chart.
     */
    public static class SalesTrendBarChart extends JComponent {
        private List<BarItem> items = new ArrayList<>();

        public SalesTrendBarChart() {
            setPreferredSize(new Dimension(500, 240));
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
            int paddingLeft = 60;
            int paddingRight = 20;
            int paddingTop = 30;
            int paddingBottom = 40;

            int chartW = w - paddingLeft - paddingRight;
            int chartH = h - paddingTop - paddingBottom;

            // Background & Grid
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);

            double maxVal = 1000;
            for (BarItem item : items) {
                if (item.value > maxVal) maxVal = item.value;
            }
            maxVal = Math.ceil(maxVal / 1000.0) * 1000.0; // round up to thousands

            // Draw horizontal grid lines
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            int gridSteps = 4;
            for (int i = 0; i <= gridSteps; i++) {
                int y = paddingTop + (int) (chartH * (1.0 - (double) i / gridSteps));
                double val = (maxVal / gridSteps) * i;
                g2.setColor(new Color(241, 245, 249));
                g2.drawLine(paddingLeft, y, paddingLeft + chartW, y);

                g2.setColor(UIUtils.COLOR_TEXT_MUTED);
                String label = "P " + (int) val;
                g2.drawString(label, 8, y + 4);
            }

            if (items.isEmpty()) {
                g2.setColor(UIUtils.COLOR_TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                g2.drawString("No sales data recorded for this period.", chartW / 2, chartH / 2 + paddingTop);
                g2.dispose();
                return;
            }

            int n = items.size();
            int slotW = chartW / n;
            int barW = Math.max(16, slotW - 20);

            for (int i = 0; i < n; i++) {
                BarItem it = items.get(i);
                int x = paddingLeft + (i * slotW) + (slotW - barW) / 2;
                int barH = (int) ((it.value / maxVal) * chartH);
                int y = paddingTop + chartH - barH;

                // Gradient bar
                GradientPaint gp = new GradientPaint(x, y, new Color(34, 197, 94), x, y + barH, new Color(22, 101, 52));
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, barW, barH, 6, 6);

                // Bar top value
                g2.setColor(UIUtils.COLOR_TEXT_PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String valStr = "P " + (int) it.value;
                int valW = g2.getFontMetrics().stringWidth(valStr);
                g2.drawString(valStr, x + (barW - valW) / 2, Math.max(paddingTop - 6, y - 6));

                // Bottom date label
                g2.setColor(UIUtils.COLOR_TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                int lblW = g2.getFontMetrics().stringWidth(it.label);
                g2.drawString(it.label, x + (barW - lblW) / 2, paddingTop + chartH + 18);
            }

            g2.dispose();
        }
    }

    /**
     * Sizable, Centered Variety Sales Donut Chart with adjacent percentage legend.
     */
    public static class VarietyDonutChart extends JPanel {
        private List<SliceItem> slices = new ArrayList<>();
        private final JPanel chartCanvas;
        private final JPanel legendPanel;

        public static final Color[] PALETTE = {
            new Color(22, 101, 52),
            new Color(34, 197, 94),
            new Color(217, 119, 6),
            new Color(2, 132, 199),
            new Color(147, 51, 234),
            new Color(236, 72, 153),
            new Color(20, 184, 166),
            new Color(245, 158, 11),
            new Color(79, 70, 229),
            new Color(13, 148, 136),
            new Color(190, 24, 93),
            new Color(100, 116, 139)
        };

        public VarietyDonutChart() {
            setLayout(new BorderLayout(24, 0));
            setBackground(Color.WHITE);

            chartCanvas = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();
                    int size = Math.min(w, h) - 20;
                    if (size <= 0) { g2.dispose(); return; }

                    int cx = (w - size) / 2;
                    int cy = (h - size) / 2;

                    double total = 0;
                    for (SliceItem s : slices) total += s.value;

                    if (total <= 0) {
                        g2.setColor(new Color(226, 232, 240));
                        g2.drawOval(cx, cy, size, size);
                        g2.setColor(UIUtils.COLOR_TEXT_MUTED);
                        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
                        g2.setColor(Color.WHITE);
                        g2.setStroke(new BasicStroke(2.0f));
                        g2.draw(new Arc2D.Double(cx, cy, size, size, curAngle, -sweep, Arc2D.PIE));

                        curAngle -= sweep;
                    }

                    // Cut center hole for Donut effect
                    int holeSize = (int) (size * 0.58);
                    int hx = (w - holeSize) / 2;
                    int hy = (h - holeSize) / 2;
                    g2.setColor(Color.WHITE);
                    g2.fillOval(hx, hy, holeSize, holeSize);

                    // Donut center label
                    g2.setColor(UIUtils.COLOR_TEXT_PRIMARY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    String centerHdr = "Varieties";
                    int cw = g2.getFontMetrics().stringWidth(centerHdr);
                    g2.drawString(centerHdr, (w - cw) / 2, h / 2 - 2);

                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    g2.setColor(UIUtils.COLOR_TEXT_MUTED);
                    String sub = slices.size() + " Active";
                    int sw = g2.getFontMetrics().stringWidth(sub);
                    g2.drawString(sub, (w - sw) / 2, h / 2 + 14);

                    g2.dispose();
                }
            };
            chartCanvas.setPreferredSize(new Dimension(210, 210));
            chartCanvas.setBackground(Color.WHITE);

            legendPanel = new JPanel();
            legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
            legendPanel.setBackground(Color.WHITE);

            JScrollPane scroll = new JScrollPane(legendPanel);
            scroll.setBorder(null);
            scroll.setBackground(Color.WHITE);
            scroll.getViewport().setBackground(Color.WHITE);
            scroll.setPreferredSize(new Dimension(220, 210));

            add(chartCanvas, BorderLayout.WEST);
            add(scroll, BorderLayout.CENTER);
        }

        public void setData(List<SliceItem> data) {
            this.slices = data != null ? data : new ArrayList<>();
            legendPanel.removeAll();

            double total = 0;
            for (SliceItem s : slices) total += s.value;

            for (SliceItem item : slices) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
                row.setBackground(Color.WHITE);

                JPanel dot = new JPanel();
                dot.setPreferredSize(new Dimension(10, 10));
                dot.setBackground(item.color);

                double pct = total > 0 ? (item.value / total) * 100.0 : 0;
                String text = String.format("%s (%.1f%%)", item.name, pct);
                JLabel lbl = new JLabel(text);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lbl.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

                row.add(dot);
                row.add(lbl);
                legendPanel.add(row);
            }

            legendPanel.revalidate();
            legendPanel.repaint();
            chartCanvas.repaint();
        }
    }
}
