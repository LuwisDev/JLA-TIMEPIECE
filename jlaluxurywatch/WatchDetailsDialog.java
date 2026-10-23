package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// WatchDetailsDialog
// The window that opens when "View Details" is clicked: a big picture on the left,
// name, price, description and specifications on the right.
//
// A JDialog is a small window that belongs to another window (here the dashboard).
public class WatchDetailsDialog extends JDialog {

    public WatchDetailsDialog(JLADash dash, Watch watch) {
        super(dash, "Watch Details", true);     // true = "modal": the dashboard is blocked until this closes

        // ---- left: picture ----
        JPanel imagePanel = Theme.makeImageBox(watch.getImagePath(), 290, 360, 260, 320);

        // ---- right: details ----
        JLabel brand = Theme.makeLabel(watch.getBrand().toUpperCase(), Theme.boldFont(13), Theme.DARK_GOLD);

        JTextArea modelText = Theme.makeReadOnlyText(watch.getModel(), 2, 22);
        modelText.setFont(Theme.georgiaFont(20));

        JLabel price = Theme.makeLabel(Money.format(watch.getPrice()), Theme.boldFont(20), Theme.DARK_GREEN);

        JSeparator line = new JSeparator();
        line.setForeground(Theme.GOLD);

        // if the watch has no description, show a default sentence
        String descriptionText = watch.getDescription();
        if (descriptionText.equals("")) {
            descriptionText = "A timeless piece from the JLA TIMEPIECE collection.";
        }
        JTextArea description = Theme.makeReadOnlyText(descriptionText, 4, 30);

        // only the specs that are filled in are shown (the Watch class builds this text)
        JTextArea specsArea = Theme.makeReadOnlyText(watch.getSpecsText() + "Availability: In stock", 8, 30);

        // all the right-side items stacked in one column
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

        // ---- buttons ----
        JButton close = Theme.greenButton("Close");
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });        // dispose() closes this window
        JButton purchase = Theme.goldButton("Purchase This Watch");
        purchase.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                dash.goToPurchase(watch);       // go to Purchase Watch with this watch already chosen
            }
        });

        // ---- put the three parts together ----
        JPanel root = Theme.makeDialogRoot(18, 14);
        root.add(imagePanel, BorderLayout.WEST);
        root.add(info, BorderLayout.CENTER);
        root.add(Theme.makeRow("right", close, purchase), BorderLayout.SOUTH);
        Theme.finishDialog(this, root, dash);     // sizes the window and centers it on the dashboard
    }
}
