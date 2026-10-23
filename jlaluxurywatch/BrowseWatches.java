package jlaluxurywatch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// BrowseWatches
// The "Browse Watches" page: every watch grouped by brand. Each card's button opens the details window.
public class BrowseWatches extends JPanel {

    public BrowseWatches(WatchStore watchStore, JLADash dash) {
        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.CREAM);
        setBorder(new EmptyBorder(18, 18, 18, 18));

        add(Theme.makeHeading("BROWSE WATCHES"), BorderLayout.NORTH);

        // the gallery builds all the cards; here we only decide what their buttons do
        WatchGallery gallery = new WatchGallery(watchStore, "View Details", 1010);
        ArrayList<WatchCard> cards = gallery.getCards();
        for (int i = 0; i < cards.size(); i++) {
            final WatchCard card = cards.get(i);
            card.getButton().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dash.showDetails(card.getWatch());
                }
            });
        }
        add(Theme.makeScroll(gallery), BorderLayout.CENTER);
    }
}
