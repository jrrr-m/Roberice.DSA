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

    // Signature Roberice Theme Colors (from css/styles.css)
    public static final Color BG_MAIN = new Color(245, 240, 223);       // #f5f0df (Warm Rice Cream)
    public static final Color BG_CARD = new Color(255, 253, 246);       // #fffdf6 (Ivory Card)
    public static final Color BG_CARD_ALT = new Color(251, 247, 238);   // #fbf7ee (Soft Cream Alt)
    public static final Color BG_SIDEBAR = new Color(255, 253, 246);    // #fffdf6 (Sidebar Ivory)
    public static final Color BG_HEADER = new Color(255, 253, 246);     // #fffdf6
    public static final Color BG_HOVER = new Color(241, 235, 216);      // #f1ebd8 (Hover Cream)
    public static final Color BG_INPUT = new Color(251, 247, 238);      // #fbf7ee (Input Field)

    public static final Color BORDER_COLOR = new Color(222, 216, 200);  // #ded8c8 (Warm Border)
    public static final Color BORDER_LIGHT = new Color(235, 230, 218);  // #ebe6da

    // Signature Brand Accents
    public static final Color PRIMARY = new Color(31, 77, 50);          // #1f4d32 (Forest Green)
    public static final Color PRIMARY_HOVER = new Color(22, 58, 37);    // #163a25
    public static final Color PRIMARY_LIGHT = new Color(237, 243, 236); // #edf3ec (Soft Green Tint)
    public static final Color PRIMARY_BORDER = new Color(199, 216, 201);// #c7d8c9

    public static final Color BRAND_GOLD = new Color(245, 158, 11);     // #f59e0b (Harvest Gold)
    public static final Color BRAND_GOLD_HOVER = new Color(217, 119, 6);// #d97706
    public static final Color BRAND_GOLD_LIGHT = new Color(254, 243, 199);// #fef3c7
    public static final Color BRAND_GOLD_BORDER = new Color(252, 211, 77);// #fcd34d

    // Status Colors
    public static final Color ACCENT_SUCCESS = new Color(16, 185, 129); // #10b981
    public static final Color SUCCESS_BG = new Color(209, 250, 229);    // #d1fae5
    public static final Color SUCCESS_TEXT = new Color(6, 95, 70);      // #065f46
    public static final Color SUCCESS_BORDER = new Color(167, 243, 208);// #a7f3d0

    public static final Color ACCENT_WARNING = new Color(217, 119, 6);  // #d97706
    public static final Color WARNING_BG = new Color(254, 243, 199);    // #fef3c7
    public static final Color WARNING_TEXT = new Color(146, 64, 14);    // #92400e
    public static final Color WARNING_BORDER = new Color(253, 230, 138);// #fde68a

    public static final Color ACCENT_DANGER = new Color(239, 68, 68);   // #ef4444
    public static final Color DANGER_BG = new Color(254, 226, 226);     // #fee2e2
    public static final Color DANGER_TEXT = new Color(153, 27, 27);     // #991b1b
    public static final Color DANGER_BORDER = new Color(252, 165, 165); // #fca5a5

    public static final Color ACCENT_INFO = new Color(6, 182, 212);     // #06b6d4
    public static final Color INFO_BG = new Color(207, 250, 255);       // #cffaff
    public static final Color INFO_TEXT = new Color(14, 116, 144);      // #0e7490

    // Typography Colors
    public static final Color TEXT_MAIN = new Color(20, 46, 32);        // #142e20 (Forest Charcoal)
    public static final Color TEXT_MUTED = new Color(94, 108, 100);     // #5e6c64 (Muted Forest Slate)
    public static final Color TEXT_LIGHT = new Color(141, 151, 144);    // #8d9790

    // Compatibility aliases
    public static final Color BG_DARK = BG_MAIN;
    public static final Color TEXT_PRIMARY = TEXT_MAIN;
    public static final Color TEXT_SECONDARY = TEXT_MUTED;
    public static final Color ACCENT_PRIMARY = PRIMARY;

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
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(PRIMARY_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(PRIMARY);
            }
        });

        if (listener != null) btn.addActionListener(listener);
        return btn;
    }

    public static JButton createSecondaryButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(BG_INPUT);
        btn.setForeground(TEXT_MAIN);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(new Color(241, 235, 216));
                    btn.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(209, 198, 171), 1, true),
                            new EmptyBorder(8, 14, 8, 14)
                    ));
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(BG_INPUT);
                    btn.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(BORDER_COLOR, 1, true),
                            new EmptyBorder(8, 14, 8, 14)
                    ));
                }
            }
        });

        if (listener != null) btn.addActionListener(listener);
        return btn;
    }

    public static JButton createSuccessButton(String text, ActionListener listener) {
        return createButton(text, ACCENT_SUCCESS, Color.WHITE, listener);
    }

    public static JButton createDangerButton(String text, ActionListener listener) {
        return createButton(text, ACCENT_DANGER, Color.WHITE, listener);
    }

    public static JButton createWarningButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(new Color(180, 83, 9)); // #b45309 Harvest Gold/Amber
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(new Color(146, 64, 14));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(new Color(180, 83, 9));
            }
        });

        if (listener != null) btn.addActionListener(listener);
        return btn;
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

        Color hoverBg = bg.darker();
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
        table.setForeground(TEXT_MAIN);
        table.setGridColor(BORDER_COLOR);
        table.setRowHeight(38);
        table.setFont(FONT_REGULAR);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(PRIMARY);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(243, 237, 216)); // #f3edd8
        header.setForeground(new Color(59, 78, 66));     // #3b4e42
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                if (!isSel) {
                    c.setBackground(row % 2 == 0 ? BG_CARD : BG_CARD_ALT);
                    c.setForeground(TEXT_MAIN);
                } else {
                    c.setBackground(PRIMARY_LIGHT);
                    c.setForeground(PRIMARY);
                }
                setBorder(new EmptyBorder(0, 12, 0, 12));
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
