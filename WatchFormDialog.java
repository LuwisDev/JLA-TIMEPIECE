package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class WatchFormDialog extends JDialog {
    private boolean saved = false;
    private Watch resultWatch;

    private JTextField brandField = new JTextField();
    private JTextField modelField = new JTextField();
    private JTextField priceField = new JTextField();
    private JTextField imageField = new JTextField();
    private JTextField caseSizeField = new JTextField();
    private JTextField movementField = new JTextField();
    private JTextField waterField = new JTextField();
    private JTextField strapField = new JTextField();
    private JTextField dialField = new JTextField();
    private JTextArea descriptionArea = new JTextArea(2, 30);
    private JCheckBox featuredBox = new JCheckBox("Show on the customer's Home page (featured)");
    private JRadioButton availableButton = new JRadioButton("Available", true);
    private JRadioButton notAvailableButton = new JRadioButton("Not Available");

    public WatchFormDialog(JFrame owner, Watch watch) {
        super(owner, watch == null ? "Add Watch" : "Edit Watch", true);

        JTextField[] fields = {brandField, modelField, priceField, imageField,
                caseSizeField, movementField, waterField, strapField, dialField};
        for (JTextField field : fields) {
            Theme.styleField(field);
        }
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(Theme.BODY_FONT);
        descriptionArea.setForeground(Theme.DARK_GREEN);
        Theme.addPadding(descriptionArea, 6, 8, 6, 8);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setBorder(Theme.goldLine(1));
        featuredBox.setOpaque(false);
        featuredBox.setFont(Theme.BODY_FONT);
        featuredBox.setForeground(Theme.DARK_GREEN);

        ButtonGroup availabilityGroup = new ButtonGroup();
        availabilityGroup.add(availableButton);
        availabilityGroup.add(notAvailableButton);
        JRadioButton[] availabilityButtons = {availableButton, notAvailableButton};
        for (JRadioButton button : availabilityButtons) {
            button.setOpaque(false);
            button.setFont(Theme.BODY_FONT);
            button.setForeground(Theme.DARK_GREEN);
        }

        if (watch != null) {
            brandField.setText(watch.getBrand());
            modelField.setText(watch.getModel());
            priceField.setText(String.format("%.2f", watch.getPrice()));
            imageField.setText(watch.getImagePath());
            caseSizeField.setText(watch.getCaseSize());
            movementField.setText(watch.getMovement());
            waterField.setText(watch.getWaterResistance());
            strapField.setText(watch.getStrap());
            dialField.setText(watch.getDial());
            descriptionArea.setText(watch.getDescription());
            featuredBox.setSelected(watch.isFeatured());
            if (watch.isAvailable()) {
                availableButton.setSelected(true);
            } else {
                notAvailableButton.setSelected(true);
            }
        }

        JButton browse = Theme.greenButton("Browse...");
        browse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseImage();
            }
        });
        JPanel imageRow = Theme.makeBorderPanel(6, 0);
        imageRow.add(imageField, BorderLayout.CENTER);
        imageRow.add(browse, BorderLayout.EAST);

        JPanel form = Theme.makeColumn();
        form.add(Theme.makeFormRow("Brand *", brandField));
        form.add(Theme.makeFormRow("Model *", modelField));
        form.add(Theme.makeFormRow("Price (PHP) *", priceField));
        form.add(Theme.makeFormRow("Image file", imageRow));
        form.add(Theme.makeFormRow("Case size", caseSizeField));
        form.add(Theme.makeFormRow("Movement", movementField));
        form.add(Theme.makeFormRow("Water resistance", waterField));
        form.add(Theme.makeFormRow("Strap / Bracelet", strapField));
        form.add(Theme.makeFormRow("Dial", dialField));
        form.add(Theme.makeFormRow("Description", descriptionScroll));
        form.add(Theme.makeFormRow("Availability", Theme.makeRow("left", availableButton, notAvailableButton)));
        form.add(Theme.makeFormRow("", featuredBox));

        JButton cancel = Theme.greenButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JButton save = Theme.goldButton("Save Watch");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                trySave();
            }
        });

        JPanel root = Theme.makeDialogRoot(0, 14);
        root.add(Theme.makeHeading(watch == null ? "ADD WATCH" : "EDIT WATCH"), BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(Theme.makeRow("right", cancel, save), BorderLayout.SOUTH);
        Theme.finishDialog(this, root, owner);
    }

    private void chooseImage() {
        File imagesFolder = new File("images");
        JFileChooser chooser = new JFileChooser(imagesFolder.exists() ? imagesFolder : new File("."));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Pictures (png, jpg, gif)", "png", "jpg", "jpeg", "gif"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File chosen = chooser.getSelectedFile();
            String path = chosen.getPath();
            try {
                Path base = new File(".").getCanonicalFile().toPath();
                path = base.relativize(chosen.getCanonicalFile().toPath()).toString();
            } catch (Exception e) {
            }
            imageField.setText(path.replace('\\', '/'));
        }
    }

    private void trySave() {
        String brand = brandField.getText().trim();
        String model = modelField.getText().trim();
        if (brand.isEmpty() || model.isEmpty()) {
            Theme.showMessage(this, "Brand and model are required.");
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceField.getText().replace(",", "").replace("\u20B1", "").trim());
        } catch (NumberFormatException e) {
            Theme.showMessage(this, "Please enter the price as a number, like 45000.");
            return;
        }
        if (price <= 0) {
            Theme.showMessage(this, "The price must be more than 0.");
            return;
        }
        resultWatch = new Watch(brand, model, price, imageField.getText().trim(),
                caseSizeField.getText().trim(), movementField.getText().trim(),
                waterField.getText().trim(), strapField.getText().trim(),
                dialField.getText().trim(), descriptionArea.getText().trim());
        resultWatch.setFeatured(featuredBox.isSelected());
        resultWatch.setAvailable(availableButton.isSelected());
        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public Watch getWatch() { return resultWatch; }
}
