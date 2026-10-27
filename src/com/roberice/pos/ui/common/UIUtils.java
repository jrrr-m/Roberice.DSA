package com.roberice.pos.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * UI Utilities, color themes, custom components, and formatting helpers.
 */
public class UIUtils {
    // Curated Roberice Brand Color Palette
    public static final Color COLOR_PRIMARY = new Color(22, 101, 52); // #166534 Forest Green
    public static final Color COLOR_PRIMARY_HOVER = new Color(21, 128, 61); // #15803d
    public static final Color COLOR_PRIMARY_DARK = new Color(20, 83, 45); // #14532d

    public static final Color COLOR_SIDEBAR_BG = new Color(15, 23, 42); // #0f172a Slate dark
    public static final Color COLOR_SIDEBAR_HOVER = new Color(30, 41, 59); // #1e293b
    public static final Color COLOR_SIDEBAR_ACTIVE = new Color(22, 101, 52); // Active green

    public static final Color COLOR_BG = new Color(248, 250, 252); // #f8fafc Light grey-blue
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_BORDER = new Color(226, 232, 240); // #e2e8f0

    public static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42); // #0f172a
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139); // #64748b

    public static final Color COLOR_SUCCESS = new Color(22, 163, 74); // #16a34a
    public static final Color COLOR_WARNING = new Color(217, 119, 6); // #d97706
    public static final Color COLOR_DANGER = new Color(220, 38, 38); // #dc2626
    public static final Color COLOR_INFO = new Color(2, 132, 199); // #0284c7

    public static final Font FONT_HEADER_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
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
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(COLOR_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_REGULAR);
        btn.setForeground(COLOR_TEXT_PRIMARY);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(7, 14, 7, 14)
        ));
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
        JPanel card = new JPanel();
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(16, 18, 16, 18)
        ));
        return card;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(36);
        table.setFont(FONT_REGULAR);
        table.setSelectionBackground(new Color(220, 252, 231)); // Soft green tint
        table.setSelectionForeground(COLOR_TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(COLOR_BORDER);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(COLOR_TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        // Center align by default
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        // Right align for numbers
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
