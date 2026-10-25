package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class WatchDetailsDialog extends JDialog {

    public WatchDetailsDialog(JLADash dash, Watch watch) {
        super(dash, "Watch Details", true);

        JPanel imagePanel = Theme.makeImageBox(watch.getImagePath(), 290, 360, 260, 320);

        JLabel brand = Theme.makeLabel(watch.getBrand().toUpperCase(), Theme.boldFont(13), Theme.DARK_GOLD);

        JTextArea modelText = Theme.makeReadOnlyText(watch.getModel(), 2, 22);
        modelText.setFont(Theme.georgiaFont(20));

        JLabel price = Theme.makeLabel(Money.format(watch.getPrice()), Theme.boldFont(20), Theme.DARK_GREEN);

        JSeparator line = new JSeparator();
        line.setForeground(Theme.GOLD);

        String descriptionText = watch.getDescription();
        if (descriptionText.equals("")) {
            descriptionText = "A timeless piece from the JLA TIMEPIECE collection.";
        }
        JTextArea description = Theme.makeReadOnlyText(descriptionText, 4, 30);

        JTextArea specsArea = Theme.makeReadOnlyText(watch.getSpecsText() + "Availability: " + watch.getAvailabilityText(), 8, 30);

        JPanel info = Theme.makeColumn();
        Theme.alignLeft(brand, modelText, price, line, description, specsArea);
        info.add(brand);
        info.add(modelText);
        info.add(Theme.gap(4));
        info.add(price);
        info.add(Theme.gap(8));
        info.add(line);
        info.add(Theme.gap(8));
        info.add(description);
        info.add(Theme.gap(10));
        info.add(specsArea);

        JButton close = Theme.greenButton("Close");
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JButton purchase = Theme.goldButton("Purchase This Watch");
        purchase.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                dash.goToPurchase(watch);
            }
        });

        if (!watch.isAvailable()) {
            purchase.setText("Not Available");
            purchase.setEnabled(false);
        }

        JPanel root = Theme.makeDialogRoot(18, 14);
        root.add(imagePanel, BorderLayout.WEST);
        root.add(info, BorderLayout.CENTER);
        root.add(Theme.makeRow("right", close, purchase), BorderLayout.SOUTH);
        Theme.finishDialog(this, root, dash);
    }
}
