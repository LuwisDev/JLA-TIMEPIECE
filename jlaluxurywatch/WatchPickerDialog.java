package jlaluxurywatch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// WatchPickerDialog
// The window opened by "Choose Watch" in Purchase Watch.
// It shows every watch (using the same WatchGallery as Browse) and lets the customer pick one.
public class WatchPickerDialog extends JDialog {

    public WatchPickerDialog(JLADash dash, WatchStore watchStore, PurchasePanel purchasePanel) {
        super(dash, "Choose a Watch", true);

        // ---- header: title and a hint ----
        JLabel title = Theme.makeHeading("CHOOSE A WATCH");
        JLabel hint = new JLabel("Pick the watch you want to purchase.");
        hint.setFont(Theme.BODY_FONT);
        hint.setForeground(Theme.DARK_GREEN);
        JPanel header = new JPanel(new BorderLayout(0, 2));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(hint, BorderLayout.SOUTH);

        // ---- the gallery: here each card's button selects the watch and closes the window ----
        WatchGallery gallery = new WatchGallery(watchStore, "Select Watch", 730);
        ArrayList<WatchCard> cards = gallery.getCards();
        for (int i = 0; i < cards.size(); i++) {
            final WatchCard card = cards.get(i);
            card.getButton().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    purchasePanel.setSelectedWatch(card.getWatch());
                    dispose();
                }
            });
        }

        JButton cancel = Theme.greenButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(cancel);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Theme.CREAM);
        root.setBorder(new EmptyBorder(16, 16, 14, 16));
        root.add(header, BorderLayout.NORTH);
        root.add(Theme.makeScroll(gallery), BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        root.setPreferredSize(new Dimension(800, 620));
        setContentPane(root);
        pack();
        setResizable(false);
        setLocationRelativeTo(dash);
    }
}
