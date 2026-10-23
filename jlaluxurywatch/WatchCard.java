package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;

// WatchCard
// A single watch card: picture, name, price and a button.
// The screen that creates the card decides what the button does (View Details, Select Watch...).
//
// OOP: INHERITANCE - it extends JPanel, so a WatchCard IS a panel and can be added to any screen.
public class WatchCard extends JPanel {
    private Watch watch;       // the watch this card shows
    private JButton button;    // the card's button (the screen adds the click action)

    public WatchCard(Watch watch, String buttonText) {
        this.watch = watch;
        setLayout(new BorderLayout(6, 6));
        setBackground(Theme.DARK_GREEN);
        setBorder(Theme.goldBorder(2, 10));     // gold line, 2 pixels thick, 10 pixels of padding

        // ---- picture: show the image, or the text "Watch image" if the file is missing ----
        JLabel image = Theme.makeLabel("", Theme.BODY_FONT, Theme.CREAM);
        image.setHorizontalAlignment(SwingConstants.CENTER);
        image.setPreferredSize(new Dimension(150, 135));
        Theme.showImage(image, watch.getImagePath(), 150, 130, "Watch image");

        // long names are cut into lines that fit inside 150 pixels, so the card does not get wider
        JPanel name = Theme.makeWrappedText(watch.getBrand() + "\n" + watch.getModel(), 150,
                Theme.BOLD_FONT, Theme.CREAM, "center");

        JLabel price = Theme.makeLabel(Money.format(watch.getPrice()), Theme.BOLD_FONT, Theme.GOLD);
        price.setHorizontalAlignment(SwingConstants.CENTER);

        button = Theme.goldButton(buttonText);

        // name on top, price under it
        JPanel info = Theme.makeBorderPanel(0, 4);
        info.add(name, BorderLayout.CENTER);
        info.add(price, BorderLayout.SOUTH);

        // the card = picture (top), info (middle), button (bottom)
        add(image, BorderLayout.NORTH);
        add(info, BorderLayout.CENTER);
        add(button, BorderLayout.SOUTH);
    }

    public Watch getWatch() {
        return watch;
    }

    public JButton getButton() {
        return button;
    }
}
