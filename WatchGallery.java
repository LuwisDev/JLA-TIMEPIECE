package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class WatchGallery extends JPanel {
    private ArrayList<WatchCard> cards = new ArrayList<WatchCard>();

    public WatchGallery(WatchStore watchStore, String buttonText, int width) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.CREAM);

        ArrayList<Watch> watches = watchStore.getWatches();

        ArrayList<String> brands = new ArrayList<String>();
        for (Watch watch : watches) {
            if (!brands.contains(watch.getBrand())) {
                brands.add(watch.getBrand());
            }
        }

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

        setPreferredSize(new Dimension(width, getPreferredSize().height));
    }

    public ArrayList<WatchCard> getCards() {
        return cards;
    }

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
