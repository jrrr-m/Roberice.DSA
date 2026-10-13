package com.roberice.dsa.ui.components;

import com.roberice.dsa.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Topbar extends JPanel {

    private final JLabel titleLabel;
    private final JLabel subtitleLabel;
    private final JLabel clockLabel;
    private final JButton refreshButton;
    private Runnable onRefresh;

    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy  •  hh:mm:ss a");

    public Topbar(Runnable onRefresh) {
        this.onRefresh = onRefresh;

        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_DARK);
        setPreferredSize(new Dimension(0, 72));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER_COLOR),
                new EmptyBorder(14, 28, 14, 28)
        ));

        // LEFT: Title & Subtitle
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        titleLabel = new JLabel("Dashboard Overview");
        titleLabel.setFont(UIUtils.FONT_TITLE);
        titleLabel.setForeground(UIUtils.TEXT_PRIMARY);

        subtitleLabel = new JLabel("Welcome back. Here is your grain inventory and sales snapshot.");
        subtitleLabel.setFont(UIUtils.FONT_REGULAR);
        subtitleLabel.setForeground(UIUtils.TEXT_MUTED);

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(2));
        leftPanel.add(subtitleLabel);

        add(leftPanel, BorderLayout.WEST);

        // RIGHT: Live Clock & Action Button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightPanel.setOpaque(false);

        clockLabel = new JLabel();
        clockLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        clockLabel.setForeground(UIUtils.TEXT_SECONDARY);
        updateClock();

        Timer timer = new Timer(1000, e -> updateClock());
        timer.start();

        JLabel statusBadge = UIUtils.createBadge("ONLINE", new Color(6, 78, 59), UIUtils.ACCENT_SUCCESS);

        refreshButton = UIUtils.createSecondaryButton("🔄 Refresh", e -> {
            if (this.onRefresh != null) this.onRefresh.run();
        });

        rightPanel.add(statusBadge);
        rightPanel.add(clockLabel);
        rightPanel.add(refreshButton);

        add(rightPanel, BorderLayout.EAST);
    }

    private void updateClock() {
        clockLabel.setText(LocalDateTime.now().format(CLOCK_FORMAT));
    }

    public void setHeader(String title, String subtitle) {
        titleLabel.setText(title);
        subtitleLabel.setText(subtitle);
    }

    public void setOnRefresh(Runnable onRefresh) {
        this.onRefresh = onRefresh;
    }
}
