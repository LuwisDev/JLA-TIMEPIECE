package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// JLADash
// The Customer dashboard. It only has to say WHAT it contains (Home, Browse, Purchase);
// the window, the sidebar and the page switching come from the parent class Dashboard.
//
// The Browse Watches page used to be its own class (BrowseWatches). It was only a few lines,
// so it is now the method makeBrowsePanel() in this class.
//
// OOP: INHERITANCE ("extends Dashboard") + POLYMORPHISM (it overrides beforeShowingPage).
public class JLADash extends Dashboard {
    private WatchStore watchStore;
    private OrderStore orderStore;
    private PurchasePanel purchasePanel;

    public JLADash(WatchStore watchStore, OrderStore orderStore) {
        super("JLA TIMEPIECE", "JLA TIMEPIECE");    // build the empty window first (parent constructor)
        this.watchStore = watchStore;
        this.orderStore = orderStore;

        // ---- the pages ----
        purchasePanel = new PurchasePanel(watchStore, orderStore, this);
        addPage("home", makeHomePanel());
        addPage("browse", makeBrowsePanel());
        addPage("purchase", purchasePanel);

        // ---- the buttons of the sidebar ----
        addMenuButton("Home", "home");
        addMenuButton("Browse Watches", "browse");
        addMenuButton("Purchase Watch", "purchase");
        addBottomButton("Back to Welcome", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WelcomePage.open(JLADash.this.watchStore, JLADash.this.orderStore);   // same stores, so nothing is lost
                dispose();
            }
        });

        finishLayout();     // must be the LAST step
    }

    //  The customer pages never need to reload anything, so this does nothing.
    @Override
    protected void beforeShowingPage(String pageName) {
    }

    //  Opens the details window of a watch.
    public void showDetails(Watch watch) {
        WatchDetailsDialog dialog = new WatchDetailsDialog(this, watch);
        dialog.setVisible(true);
    }

    //  Goes to Purchase Watch with this watch already selected.
    public void goToPurchase(Watch watch) {
        purchasePanel.setSelectedWatch(watch);
        showPage("purchase");
    }

    //  Builds the Home page: welcome text and the featured watches.
    private JPanel makeHomePanel() {
        JLabel heading = Theme.makeLabel("Welcome to JLA TIMEPIECE", Theme.georgiaFont(28), Theme.DARK_GREEN);
        JLabel sub = Theme.makeLabel("A collection of timeless pieces.", Theme.normalFont(16), Theme.DARK_GREEN);
        JLabel featuredTitle = Theme.makeHeading("FEATURED WATCHES");
        Theme.alignCenter(heading, sub, featuredTitle);

        // the top part: the three labels one under the other
        JPanel top = Theme.makeColumn();
        top.add(heading);
        top.add(Theme.gap(4));
        top.add(sub);
        top.add(Theme.gap(16));
        top.add(featuredTitle);

        // the featured watches: GridLayout(0, 3, ...) = any number of rows, exactly 3 columns
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
        // fixed width so the horizontal scroll bar never appears
        content.setPreferredSize(new Dimension(1030, content.getPreferredSize().height));

        // wrap the content in a scroll pane so the user can scroll down
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.CREAM);
        panel.add(Theme.makeScroll(content), BorderLayout.CENTER);
        return panel;
    }

    //  Builds the Browse Watches page: every watch grouped by brand. Each button opens the details window.
    private JPanel makeBrowsePanel() {
        JPanel page = new JPanel();
        Theme.setupPage(page, 12, 18);      // cream page, 12 pixel gaps, 18 pixels of padding
        page.add(Theme.makeHeading("BROWSE WATCHES"), BorderLayout.NORTH);

        // the gallery builds all the cards; here we only decide what their buttons do
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

    //  Running JLADash directly also starts at the welcome page.
    public static void main(String[] args) {
        WelcomePage.open();
    }
}
