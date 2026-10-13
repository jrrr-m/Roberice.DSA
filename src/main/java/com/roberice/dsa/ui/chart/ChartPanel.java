package com.roberice.dsa.ui.chart;

import com.roberice.dsa.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class ChartPanel extends JPanel {

    public enum ChartType {
        BAR, DONUT
    }

    private ChartType chartType = ChartType.BAR;
    private String title = "";
    private List<Map<String, Object>> data;
    private String labelKey = "label";
    private String valueKey = "value";

    private static final Color[] PALETTE = {
            new Color(59, 130, 246),  // Blue
            new Color(16, 185, 129),  // Emerald
            new Color(245, 158, 11),  // Amber
            new Color(168, 85, 247),  // Purple
            new Color(236, 72, 153),  // Pink
            new Color(14, 165, 233),  // Sky
            new Color(249, 115, 22),  // Orange
            new Color(20, 184, 166)   // Teal
    };

    public ChartPanel(ChartType chartType, String title) {
        this.chartType = chartType;
        this.title = title;
        setBackground(UIUtils.BG_CARD);
        setPreferredSize(new Dimension(350, 240));
    }

    public void setData(List<Map<String, Object>> data, String labelKey, String valueKey) {
        this.data = data;
        this.labelKey = labelKey;
        this.valueKey = valueKey;
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

        // Draw Title
        g2.setFont(UIUtils.FONT_SUBHEADER);
        g2.setColor(UIUtils.TEXT_PRIMARY);
        g2.drawString(title, 16, 26);

        if (data == null || data.isEmpty()) {
            g2.setFont(UIUtils.FONT_REGULAR);
            g2.setColor(UIUtils.TEXT_MUTED);
            g2.drawString("No data available for display", 16, h / 2);
            g2.dispose();
            return;
        }

        if (chartType == ChartType.BAR) {
            drawBarChart(g2, 16, 42, w - 32, h - 54);
        } else {
            drawDonutChart(g2, 16, 42, w - 32, h - 54);
        }

        g2.dispose();
    }

    private void drawBarChart(Graphics2D g2, int x, int y, int w, int h) {
        double maxVal = 0.0;
        for (Map<String, Object> item : data) {
            Object v = item.get(valueKey);
            if (v instanceof Number) {
                maxVal = Math.max(maxVal, ((Number) v).doubleValue());
            }
        }
        if (maxVal <= 0) maxVal = 1.0;

        int n = data.size();
        int bottomY = y + h - 24;
        int chartH = h - 45;

        // Draw baseline
        g2.setColor(UIUtils.BORDER_COLOR);
        g2.drawLine(x, bottomY, x + w, bottomY);

        int barW = Math.max(16, (w - (n * 12)) / Math.max(1, n));
        int totalBarsW = n * barW + (n - 1) * 10;
        int startX = x + Math.max(0, (w - totalBarsW) / 2);

        DecimalFormat df = new DecimalFormat("#,##0");

        for (int i = 0; i < n; i++) {
            Map<String, Object> item = data.get(i);
            double val = item.get(valueKey) instanceof Number ? ((Number) item.get(valueKey)).doubleValue() : 0.0;
            String lbl = String.valueOf(item.get(labelKey));
            if (lbl.length() > 6) {
                // Shorten date like 2026-10-12 to 10-12
                if (lbl.contains("-")) {
                    String[] parts = lbl.split("-");
                    if (parts.length >= 3) lbl = parts[1] + "/" + parts[2];
                }
            }

            int barH = (int) Math.round((val / maxVal) * chartH);
            int barX = startX + i * (barW + 10);
            int barY = bottomY - barH;

            // Bar fill with gradient
            GradientPaint gp = new GradientPaint(
                    barX, barY, UIUtils.ACCENT_PRIMARY.brighter(),
                    barX, bottomY, UIUtils.ACCENT_PRIMARY
            );
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Double(barX, barY, barW, barH, 6, 6));

            // Bar value on top
            if (barW >= 24) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.setColor(UIUtils.TEXT_SECONDARY);
                String valStr = val >= 1000 ? String.format("%.0fk", val / 1000.0) : df.format(val);
                FontMetrics fm = g2.getFontMetrics();
                int vStrW = fm.stringWidth(valStr);
                g2.drawString(valStr, barX + (barW - vStrW) / 2, barY - 4);
            }

            // Label below
            g2.setFont(UIUtils.FONT_SMALL);
            g2.setColor(UIUtils.TEXT_MUTED);
            FontMetrics lfm = g2.getFontMetrics();
            int lStrW = lfm.stringWidth(lbl);
            g2.drawString(lbl, barX + (barW - lStrW) / 2, bottomY + 16);
        }
    }

    private void drawDonutChart(Graphics2D g2, int x, int y, int w, int h) {
        double total = 0.0;
        for (Map<String, Object> item : data) {
            Object v = item.get(valueKey);
            if (v instanceof Number) {
                total += ((Number) v).doubleValue();
            }
        }
        if (total <= 0) total = 1.0;

        int size = Math.min(w / 2, h - 10);
        int cx = x + size / 2;
        int cy = y + h / 2;
        int rOuter = size / 2;
        int rInner = rOuter - 22;

        double currentAngle = 90.0;
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> item = data.get(i);
            double val = item.get(valueKey) instanceof Number ? ((Number) item.get(valueKey)).doubleValue() : 0.0;
            double angle = (val / total) * 360.0;

            Color c = PALETTE[i % PALETTE.length];
            g2.setColor(c);
            Arc2D.Double arc = new Arc2D.Double(cx - rOuter, cy - rOuter, rOuter * 2, rOuter * 2, currentAngle, -angle, Arc2D.PIE);
            g2.fill(arc);

            currentAngle -= angle;
        }

        // Inner circle for donut hole
        g2.setColor(UIUtils.BG_CARD);
        g2.fillOval(cx - rInner, cy - rInner, rInner * 2, rInner * 2);

        // Center total text
        g2.setColor(UIUtils.TEXT_PRIMARY);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        String totalStr = "100%";
        FontMetrics tfm = g2.getFontMetrics();
        g2.drawString(totalStr, cx - tfm.stringWidth(totalStr) / 2, cy + 4);

        // Draw Legend on the right
        int legendX = cx + rOuter + 20;
        int legendY = y + 16;
        g2.setFont(UIUtils.FONT_SMALL);

        for (int i = 0; i < Math.min(data.size(), 6); i++) {
            Map<String, Object> item = data.get(i);
            String lbl = String.valueOf(item.get(labelKey));
            double val = item.get(valueKey) instanceof Number ? ((Number) item.get(valueKey)).doubleValue() : 0.0;
            double pct = (val / total) * 100.0;

            Color c = PALETTE[i % PALETTE.length];
            g2.setColor(c);
            g2.fillRoundRect(legendX, legendY - 8, 10, 10, 3, 3);

            g2.setColor(UIUtils.TEXT_SECONDARY);
            String text = lbl + " (" + String.format("%.0f%%", pct) + ")";
            g2.drawString(text, legendX + 16, legendY);

            legendY += 20;
        }
    }
}
