package com.roberice.dsa.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

public class UIUtils {

    // Harmonious Dark Theme Colors
    public static final Color BG_DARK = new Color(15, 23, 42);          // #0f172a
    public static final Color BG_SIDEBAR = new Color(11, 17, 32);       // #0b1120
    public static final Color BG_CARD = new Color(30, 41, 59);          // #1e293b
    public static final Color BG_CARD_ALT = new Color(24, 34, 53);      // #182235
    public static final Color BG_HOVER = new Color(51, 65, 85);         // #334155
    public static final Color BORDER_COLOR = new Color(51, 65, 85);     // #334155
    public static final Color BORDER_LIGHT = new Color(71, 85, 105);    // #475569

    public static final Color ACCENT_PRIMARY = new Color(59, 130, 246); // #3b82f6 (Blue)
    public static final Color ACCENT_SUCCESS = new Color(16, 185, 129); // #10b981 (Emerald)
    public static final Color ACCENT_WARNING = new Color(245, 158, 11); // #f59e0b (Amber)
    public static final Color ACCENT_DANGER = new Color(239, 68, 68);   // #ef4444 (Rose)
    public static final Color ACCENT_INFO = new Color(14, 165, 233);    // #0ea5e9 (Sky)

    public static final Color TEXT_PRIMARY = new Color(248, 250, 252);  // #f8fafc
    public static final Color TEXT_SECONDARY = new Color(203, 213, 225);// #cbd5e1
    public static final Color TEXT_MUTED = new Color(148, 163, 184);    // #94a3b8

    // Fonts
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("₱#,##0.00");
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0.00");

    public static String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    public static String formatNumber(double number) {
        return NUMBER_FORMAT.format(number);
    }

    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        return card;
    }

    public static JButton createPrimaryButton(String text, ActionListener listener) {
        return createButton(text, ACCENT_PRIMARY, Color.WHITE, listener);
    }

    public static JButton createSuccessButton(String text, ActionListener listener) {
        return createButton(text, ACCENT_SUCCESS, Color.WHITE, listener);
    }

    public static JButton createDangerButton(String text, ActionListener listener) {
        return createButton(text, ACCENT_DANGER, Color.WHITE, listener);
    }

    public static JButton createSecondaryButton(String text, ActionListener listener) {
        return createButton(text, BG_HOVER, TEXT_PRIMARY, listener);
    }

    public static JButton createButton(String text, Color bg, Color fg, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));

        Color hoverBg = bg.brighter();
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(hoverBg);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(bg);
            }
        });

        if (listener != null) {
            btn.addActionListener(listener);
        }
        return btn;
    }

    public static JTable createStyledTable(TableModel model) {
        JTable table = new JTable(model);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setRowHeight(36);
        table.setFont(FONT_REGULAR);
        table.setSelectionBackground(new Color(37, 99, 235)); // Blue 600
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(15, 23, 42));
        header.setForeground(TEXT_MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                if (!isSel) {
                    c.setBackground(row % 2 == 0 ? BG_CARD : BG_CARD_ALT);
                    c.setForeground(TEXT_PRIMARY);
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);

        return table;
    }

    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel("  " + text + "  ", SwingConstants.CENTER);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setBorder(new LineBorder(bg.darker(), 1, true));
        return badge;
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message, String title) {
        int res = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return res == JOptionPane.YES_OPTION;
    }
}
