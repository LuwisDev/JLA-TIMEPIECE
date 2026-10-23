package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

// WatchGallery
// Shows all watches grouped by brand: a brand heading, then that brand's watches in rows of three.
// It is used by two screens (Browse Watches and the Choose Watch window), so it was written once here.
public class WatchGallery extends JPanel {
    // every card that was created, so the screen using the gallery can decide what each button does
    private ArrayList<WatchCard> cards = new ArrayList<WatchCard>();

    public WatchGallery(WatchStore watchStore, String buttonText, int width) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.CREAM);

        ArrayList<Watch> watches = watchStore.getWatches();

        // list of brands in the order they first appear (no brand is added twice)
        ArrayList<String> brands = new ArrayList<String>();
        for (Watch watch : watches) {
            if (!brands.contains(watch.getBrand())) {
                brands.add(watch.getBrand());
            }
        }

        // for each brand: a heading, then a grid with that brand's watch cards
        for (String brand : brands) {
            add(makeBrandHeader(brand));
            add(Theme.gap(10));

            JPanel grid = new JPanel(new GridLayout(0, 3, 12, 12));
            grid.setBackground(Theme.CREAM);
            for (Watch watch : watches) {
                if (watch.getBrand().equals(brand)) {
                    WatchCard card = new WatchCard(watch, buttonText);
                    cards.add(card);
                    grid.add(card);
                }
            }
            Theme.alignLeft(grid);
            grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, grid.getPreferredSize().height));
            add(grid);
            add(Theme.gap(24));
        }

        // fixed width keeps the horizontal scroll bar away
        setPreferredSize(new Dimension(width, getPreferredSize().height));
    }

    //  All the cards, so the screen using the gallery can decide what each button does.
    public ArrayList<WatchCard> getCards() {
        return cards;
    }

    //  The brand name in big letters with a gold line under it.
    private JPanel makeBrandHeader(String brand) {
        JLabel label = Theme.makeLabel(brand.toUpperCase(), Theme.georgiaFont(20), Theme.DARK_GREEN);

        JSeparator line = new JSeparator();
        line.setForeground(Theme.GOLD);

        JPanel header = Theme.makeBorderPanel(0, 4);
        header.add(label, BorderLayout.NORTH);
        header.add(line, BorderLayout.SOUTH);
        Theme.alignLeft(header);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, header.getPreferredSize().height));
        return header;
    }
}
