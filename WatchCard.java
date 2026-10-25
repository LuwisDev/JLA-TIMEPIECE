package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;

public class WatchCard extends JPanel {
    private Watch watch;
    private JButton button;

    public WatchCard(Watch watch, String buttonText) {
        this.watch = watch;
        setLayout(new BorderLayout(6, 6));
        setBackground(Theme.DARK_GREEN);
        setBorder(Theme.goldBorder(2, 10));

        JLabel image = Theme.makeLabel("", Theme.BODY_FONT, Theme.CREAM);
        image.setHorizontalAlignment(SwingConstants.CENTER);
        image.setPreferredSize(new Dimension(150, 135));
        Theme.showImage(image, watch.getImagePath(), 150, 130, "Watch image");

        JPanel name = Theme.makeWrappedText(watch.getBrand() + "\n" + watch.getModel(), 150,
                Theme.BOLD_FONT, Theme.CREAM, "center");

        JLabel price = Theme.makeLabel(Money.format(watch.getPrice()), Theme.BOLD_FONT, Theme.GOLD);
        price.setHorizontalAlignment(SwingConstants.CENTER);

        button = Theme.goldButton(buttonText);

        JPanel priceArea = new JPanel(new GridLayout(0, 1));
        priceArea.setOpaque(false);
        priceArea.add(price);
        if (!watch.isAvailable()) {
            JLabel notAvailable = Theme.makeLabel("NOT AVAILABLE", Theme.BOLD_FONT, new Color(255, 130, 130));
            notAvailable.setHorizontalAlignment(SwingConstants.CENTER);
            priceArea.add(notAvailable);
        }

        JPanel info = Theme.makeBorderPanel(0, 4);
        info.add(name, BorderLayout.CENTER);
        info.add(priceArea, BorderLayout.SOUTH);

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
