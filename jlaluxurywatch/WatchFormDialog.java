package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// WatchFormDialog
// The window used to ADD a new watch or EDIT an existing one.
// Pass null as the watch to add a new one. After the window closes, call isSaved() and getWatch().
public class WatchFormDialog extends JDialog {
    private boolean saved = false;
    private Watch resultWatch;
    private final Watch editingWatch;

    private JTextField brandField = new JTextField();
    private JTextField modelField = new JTextField();
    private JTextField priceField = new JTextField();
    private JTextField imageField = new JTextField();
    private JTextArea descriptionArea = new JTextArea(2, 30);
    private JCheckBox featuredBox = new JCheckBox("Show on the customer's Home page (featured)");

    public WatchFormDialog(JFrame owner, Watch watch) {
        super(owner, watch == null ? "Add Watch" : "Edit Watch", true);
        this.editingWatch = watch;

        // give every text box the store style
        JTextField[] fields = {brandField, modelField, priceField, imageField};
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

        // when editing, fill the boxes with the watch's current values
        if (watch != null) {
            brandField.setText(watch.getBrand());
            modelField.setText(watch.getModel());
            priceField.setText(String.format("%.2f", watch.getPrice()));
            imageField.setText(watch.getImagePath());
            descriptionArea.setText(watch.getDescription());
            featuredBox.setSelected(watch.isFeatured());
        }

        // image row: the text box + a Browse button that opens a file chooser
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

        // the form: one row for each "label + input" (makeFormRow puts the label on the left)
        JPanel form = Theme.makeColumn();
        form.add(Theme.makeFormRow("Brand *", brandField));
        form.add(Theme.makeFormRow("Model *", modelField));
        form.add(Theme.makeFormRow("Price (PHP) *", priceField));
        form.add(Theme.makeFormRow("Image file", imageRow));
        form.add(Theme.makeFormRow("Description", descriptionScroll));
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

    //  Opens a file chooser and writes the picked picture's path (relative to the project folder) in the box.
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
                // different drive or other problem: keep the full path
            }
            imageField.setText(path.replace('\\', '/'));
        }
    }

    //  Checks the form and builds the Watch. Stays open if something is wrong.
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
        // Keep existing technical specifications when editing, even though they are hidden in this simpler form.
        String caseSize = editingWatch == null ? "" : editingWatch.getCaseSize();
        String movement = editingWatch == null ? "" : editingWatch.getMovement();
        String waterResistance = editingWatch == null ? "" : editingWatch.getWaterResistance();
        String strap = editingWatch == null ? "" : editingWatch.getStrap();
        String dial = editingWatch == null ? "" : editingWatch.getDial();
        resultWatch = new Watch(brand, model, price, imageField.getText().trim(),
                caseSize, movement, waterResistance, strap, dial, descriptionArea.getText().trim());
        resultWatch.setFeatured(featuredBox.isSelected());
        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public Watch getWatch() { return resultWatch; }
}
