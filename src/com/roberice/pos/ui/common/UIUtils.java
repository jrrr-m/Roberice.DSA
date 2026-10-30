package com.roberice.pos.ui.common;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * UI Utilities, color themes, custom components, and formatting helpers.
 * Theme matching the warm ivory and deep forest green Roberice branding.
 */
public class UIUtils {
    // Curated Roberice Brand Color Palette
    public static final Color COLOR_PRIMARY = new Color(27, 77, 50);          // Deep Forest Green #1B4D32
    public static final Color COLOR_PRIMARY_HOVER = new Color(34, 95, 62);     // Hover Green
    public static final Color COLOR_PRIMARY_DARK = new Color(20, 60, 38);

    public static final Color COLOR_SIDEBAR_BG = new Color(255, 253, 246);     // Light Ivory #FFFCF6
    public static final Color COLOR_SIDEBAR_HOVER = new Color(245, 242, 232);  // Gentle warm hover
    public static final Color COLOR_SIDEBAR_ACTIVE = new Color(231, 241, 233); // Soft sage green tint #E7F1E9
    public static final Color COLOR_SIDEBAR_ACTIVE_TEXT = new Color(27, 77, 50);

    public static final Color COLOR_BG = new Color(246, 243, 233);             // Warm cream #F6F3E9
    public static final Color COLOR_CARD_BG = new Color(255, 253, 246);        // Pure Ivory Card #FFFCF6
    public static final Color COLOR_BORDER = new Color(234, 230, 218);         // Soft Card Border #EAE6DA

    public static final Color COLOR_TEXT_PRIMARY = new Color(15, 30, 24);      // Deep green-slate #0F1E18
    public static final Color COLOR_TEXT_MUTED = new Color(110, 125, 115);     // Slate muted #6E7D73

    public static final Color COLOR_SUCCESS = new Color(22, 163, 74);          // #16a34a
    public static final Color COLOR_WARNING = new Color(217, 119, 6);          // #d97706
    public static final Color COLOR_DANGER = new Color(220, 38, 38);           // #dc2626
    public static final Color COLOR_INFO = new Color(2, 132, 199);             // #0284c7

    public static final Font FONT_HEADER_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("₱ #,##0.00");
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0.00");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    public static String formatNumber(double num) {
        return NUMBER_FORMAT.format(num);
    }

    public static String formatDate(Date date) {
        return date != null ? DATE_FORMAT.format(date) : "-";
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(COLOR_PRIMARY_DARK);
                } else if (getModel().isRollover()) {
                    g2.setColor(COLOR_PRIMARY_HOVER);
                } else {
                    g2.setColor(COLOR_PRIMARY);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_REGULAR);
        btn.setForeground(COLOR_TEXT_PRIMARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(COLOR_DANGER);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JPanel createCard() {
        return new RoundedCardPanel(16);
    }

    public static class RoundedCardPanel extends JPanel {
        private final int radius;

        public RoundedCardPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
            setBorder(new EmptyBorder(16, 18, 16, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background fill
            g2.setColor(COLOR_CARD_BG);
            g2.fillRoundRect(0, 0, w - 1, h - 1, radius, radius);

            // Border outline
            g2.setColor(COLOR_BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(36);
        table.setFont(FONT_REGULAR);
        table.setSelectionBackground(new Color(231, 241, 233));
        table.setSelectionForeground(COLOR_TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(COLOR_BORDER);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(250, 248, 242));
        header.setForeground(COLOR_TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(" " + text + " ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setBorder(new EmptyBorder(2, 6, 2, 6));
        return badge;
    }
}
