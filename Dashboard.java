package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.awt.event.ActionEvent;

public abstract class Dashboard extends JFrame {

    public static final int WINDOW_WIDTH = 1300;
    public static final int WINDOW_HEIGHT = 700;

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel sidebar;
    private JPanel contentPanel;
    private ArrayList<JButton> bottomButtons = new ArrayList<JButton>();

    public Dashboard(String windowTitle, String sidebarTitle) {
        this(windowTitle, sidebarTitle, "");
    }

    public Dashboard(String windowTitle, String sidebarTitle, String subtitle) {
        setTitle(windowTitle);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.CREAM);
        mainPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        sidebar = Theme.makeColumn(Theme.DARK_GREEN);
        sidebar.setPreferredSize(new Dimension(230, WINDOW_HEIGHT));
        Theme.addPadding(sidebar, 25, 12, 15, 12);

        JLabel title = Theme.makeLabel(sidebarTitle, Theme.georgiaFont(22), Theme.GOLD);
        sidebar.add(title);
        if (subtitle.isEmpty()) {
            sidebar.add(Theme.gap(35));
        } else {
            JLabel sub = Theme.makeLabel(subtitle, Theme.normalFont(13), Theme.CREAM);
            sidebar.add(Theme.gap(6));
            sidebar.add(sub);
            sidebar.add(Theme.gap(29));
            Theme.alignCenter(sub);
        }
        Theme.alignCenter(title);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Theme.CREAM);
    }

    protected void addPage(String pageName, JPanel page) {
        contentPanel.add(page, pageName);
    }

    protected void addMenuButton(String text, String pageName) {
        JButton button = makeSidebarButton(Theme.goldButton(text));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPage(pageName);
            }
        });
        sidebar.add(button);
        sidebar.add(Theme.gap(12));
    }

    protected void addBottomButton(String text, ActionListener whenClicked) {
        JButton button = makeSidebarButton(Theme.greenButton(text));
        button.addActionListener(whenClicked);
        bottomButtons.add(button);
    }

    protected void finishLayout() {
        sidebar.add(Theme.pushDown());

        for (int i = 0; i < bottomButtons.size(); i++) {
            sidebar.add(bottomButtons.get(i));
            sidebar.add(Theme.gap(12));
        }

        JButton exitButton = makeSidebarButton(Theme.greenButton("Exit"));
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Theme.askYesNo(Dashboard.this, "Exit JLA TIMEPIECE?", "Confirm Exit")) {
                    System.exit(0);
                }
            }
        });
        sidebar.add(exitButton);

        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    public void showPage(String pageName) {
        beforeShowingPage(pageName);
        cardLayout.show(contentPanel, pageName);
    }

    protected abstract void beforeShowingPage(String pageName);

    private JButton makeSidebarButton(JButton button) {
        button.setMaximumSize(new Dimension(200, 42));
        Theme.alignCenter(button);
        return button;
    }
}
