package jlaluxurywatch;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

public class ManageWatchesPanel extends JPanel {
    private WatchStore store;
    private JFrame window;
    private ArrayList<Watch> shownWatches = new ArrayList<Watch>();

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel previewImage;
    private JTextArea previewText;
    private JLabel countLabel;
    private JTextField searchField;

    public ManageWatchesPanel(WatchStore store, JFrame window) {
        this.store = store;
        this.window = window;
        Theme.setupPage(this, 10, 20);

        searchField = new JTextField(16);
        Theme.styleField(searchField);
        JPanel searchRow = Theme.makeRow("right", Theme.makeLabel("Search:"), searchField);
        add(Theme.makeTopBar("MANAGE WATCHES", searchRow), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Brand", "Model", "Price", "Available", "Featured"}, 0);
        table = new JTable(tableModel);
        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                showPreview();
            }
        });

        previewImage = Theme.makeLabel("Select a watch", Theme.BODY_FONT, Theme.CREAM);
        previewImage.setHorizontalAlignment(SwingConstants.CENTER);
        previewImage.setPreferredSize(new Dimension(300, 200));
        JPanel imageBox = new JPanel(new BorderLayout());
        imageBox.setBackground(Theme.DARK_GREEN);
        imageBox.setBorder(Theme.goldLine(2));
        imageBox.add(previewImage, BorderLayout.CENTER);

        previewText = Theme.makeReadOnlyText("", 0, 0);
        JScrollPane previewScroll = new JScrollPane(previewText);
        previewScroll.setBorder(null);
        previewScroll.setOpaque(false);
        previewScroll.getViewport().setOpaque(false);

        JPanel preview = Theme.makeBorderPanel(0, 10);
        preview.setPreferredSize(new Dimension(320, 0));
        preview.add(imageBox, BorderLayout.NORTH);
        preview.add(previewScroll, BorderLayout.CENTER);

        JPanel center = Theme.makeBorderPanel(12, 0);
        center.add(Theme.makeTableScroll(table), BorderLayout.CENTER);
        center.add(preview, BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        JButton addButton = Theme.goldButton("Add Watch");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addWatch();
            }
        });
        JButton editButton = Theme.greenButton("Edit Selected");
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editWatch();
            }
        });
        JButton deleteButton = Theme.greenButton("Delete Selected");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteWatch();
            }
        });
        countLabel = Theme.makeLabel(" ", Theme.BOLD_FONT, Theme.DARK_GREEN);

        JPanel south = Theme.makeBorderPanel(0, 0);
        south.add(Theme.makeRow("left", addButton, editButton, deleteButton), BorderLayout.WEST);
        south.add(countLabel, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        Theme.whenTyping(searchField, new Runnable() {
            @Override
            public void run() {
                refreshData(null);
            }
        });

        refreshData(null);
    }

    private void refreshData(Watch selectThis) {
        tableModel.setRowCount(0);

        String search = searchField.getText().trim().toLowerCase();
        ArrayList<Watch> allWatches = store.getWatches();
        shownWatches = new ArrayList<Watch>();
        for (int i = 0; i < allWatches.size(); i++) {
            if (allWatches.get(i).getDisplayName().toLowerCase().contains(search)) {
                shownWatches.add(allWatches.get(i));
            }
        }

        for (int i = 0; i < shownWatches.size(); i++) {
            Watch watch = shownWatches.get(i);
            tableModel.addRow(new Object[]{watch.getBrand(), watch.getModel(), Money.format(watch.getPrice()),
                    watch.getAvailabilityText(), watch.isFeatured() ? "Yes" : ""});
            if (watch == selectThis) {
                table.setRowSelectionInterval(i, i);
            }
        }
        countLabel.setText(shownWatches.size() + " of " + allWatches.size() + " watches");
        showPreview();
    }

    private Watch getSelectedWatch() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < shownWatches.size()) {
            return shownWatches.get(row);
        }
        return null;
    }

    private void showPreview() {
        Watch watch = getSelectedWatch();
        if (watch == null) {
            previewImage.setIcon(null);
            previewImage.setText("Select a watch");
            previewText.setText("");
            return;
        }
        Theme.showImage(previewImage, watch.getImagePath(), 290, 190, "No image");
        String text = watch.getDisplayName() + "\n" + Money.format(watch.getPrice()) + "\n"
                + watch.getAvailabilityText() + "\n";
        if (!watch.getSpecsText().isEmpty()) text += "\n" + watch.getSpecsText();
        if (!watch.getDescription().isEmpty()) text += "\n" + watch.getDescription();
        previewText.setText(text);
        previewText.setCaretPosition(0);
    }

    private void addWatch() {
        WatchFormDialog form = new WatchFormDialog(window, null);
        form.setVisible(true);
        if (form.isSaved()) {
            String error = store.addWatch(form.getWatch());
            if (error != null) {
                Theme.showMessage(this, error);
                return;
            }
            searchField.setText("");
            refreshData(form.getWatch());
        }
    }

    private void editWatch() {
        Watch selected = getSelectedWatch();
        if (selected == null) {
            Theme.showMessage(this, "Please select a watch to edit.");
            return;
        }
        WatchFormDialog form = new WatchFormDialog(window, selected);
        form.setVisible(true);
        if (form.isSaved()) {
            String error = store.updateWatch(selected, form.getWatch());
            if (error != null) {
                Theme.showMessage(this, error);
                return;
            }
            searchField.setText("");
            refreshData(form.getWatch());
        }
    }

    private void deleteWatch() {
        Watch selected = getSelectedWatch();
        if (selected == null) {
            Theme.showMessage(this, "Please select a watch to delete.");
            return;
        }
        if (Theme.askYesNoWarning(this, "Delete " + selected.getDisplayName()
                + "?\n\nPast transactions of this watch are kept.", "Delete Watch")) {
            store.deleteWatch(selected);
            refreshData(null);
        }
    }
}
