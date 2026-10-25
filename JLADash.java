package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class JLADash extends Dashboard {
    private WatchStore watchStore;
    private OrderStore orderStore;
    private PurchasePanel purchasePanel;

    public JLADash(WatchStore watchStore, OrderStore orderStore) {
        super("JLA TIMEPIECE", "JLA TIMEPIECE");
        this.watchStore = watchStore;
        this.orderStore = orderStore;

        purchasePanel = new PurchasePanel(watchStore, orderStore, this);
        addPage("home", makeHomePanel());
        addPage("browse", makeBrowsePanel());
        addPage("purchase", purchasePanel);

        addMenuButton("Home", "home");
        addMenuButton("Browse Watches", "browse");
        addMenuButton("Purchase Watch", "purchase");
        addBottomButton("Back to Welcome", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WelcomePage.open(JLADash.this.watchStore, JLADash.this.orderStore);
                dispose();
            }
        });

        finishLayout();
    }

    @Override
    protected void beforeShowingPage(String pageName) {
    }

    public void showDetails(Watch watch) {
        WatchDetailsDialog dialog = new WatchDetailsDialog(this, watch);
        dialog.setVisible(true);
    }

    public void goToPurchase(Watch watch) {
        purchasePanel.setSelectedWatch(watch);
        showPage("purchase");
    }

    private JPanel makeHomePanel() {
        JLabel heading = Theme.makeLabel("Welcome to JLA TIMEPIECE", Theme.georgiaFont(28), Theme.DARK_GREEN);
        JLabel sub = Theme.makeLabel("A collection of timeless pieces.", Theme.normalFont(16), Theme.DARK_GREEN);
        JLabel featuredTitle = Theme.makeHeading("FEATURED WATCHES");
        Theme.alignCenter(heading, sub, featuredTitle);

        JPanel top = Theme.makeColumn();
        top.add(heading);
        top.add(Theme.gap(4));
        top.add(sub);
        top.add(Theme.gap(16));
        top.add(featuredTitle);

        JPanel featuredPanel = new JPanel(new GridLayout(0, 3, 12, 12));
        featuredPanel.setOpaque(false);
        for (Watch watch : watchStore.getFeaturedWatches()) {
            WatchCard card = new WatchCard(watch, "View Details");
            card.getButton().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showDetails(card.getWatch());
                }
            });
            featuredPanel.add(card);
        }

        JButton browseButton = Theme.greenButton("Browse All Watches");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPage("browse");
            }
        });

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(Theme.CREAM);
        Theme.addPadding(content, 24);
        content.add(top, BorderLayout.NORTH);
        content.add(featuredPanel, BorderLayout.CENTER);
        content.add(Theme.makeRow("center", browseButton), BorderLayout.SOUTH);
        content.setPreferredSize(new Dimension(1030, content.getPreferredSize().height));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.CREAM);
        panel.add(Theme.makeScroll(content), BorderLayout.CENTER);
        return panel;
    }

    private JPanel makeBrowsePanel() {
        JPanel page = new JPanel();
        Theme.setupPage(page, 12, 18);
        page.add(Theme.makeHeading("BROWSE WATCHES"), BorderLayout.NORTH);

        WatchGallery gallery = new WatchGallery(watchStore, "View Details", 1010);
        for (WatchCard card : gallery.getCards()) {
            card.getButton().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showDetails(card.getWatch());
                }
            });
        }
        page.add(Theme.makeScroll(gallery), BorderLayout.CENTER);
        return page;
    }

    public static void main(String[] args) {
        WelcomePage.open();
    }
}
