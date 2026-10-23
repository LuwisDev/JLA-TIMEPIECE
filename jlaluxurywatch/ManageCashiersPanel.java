package jlaluxurywatch;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// ManageCashiersPanel
// The admin creates the accounts the cashiers use to log in.
// Left: the list of cashiers (with Reset Password and Delete). Right: the "Create Account" form.
public class ManageCashiersPanel extends JPanel {
    private AccountStore accounts;
    private ArrayList<Account> shownCashiers = new ArrayList<Account>();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField fullNameField = new JTextField();
    private JTextField usernameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();

    public ManageCashiersPanel(AccountStore accounts) {
        this.accounts = accounts;
        Theme.setupPage(this, 10, 20);
        add(Theme.makeHeading("CASHIER ACCOUNTS"), BorderLayout.NORTH);

        // ---------------- LEFT: the cashier list ----------------
        tableModel = new DefaultTableModel(new String[]{"Full name", "Username"}, 0);
        table = new JTable(tableModel);
        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton resetButton = Theme.greenButton("Reset Password");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetPassword();
            }
        });
        JButton deleteButton = Theme.greenButton("Delete Account");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteCashier();
            }
        });

        JPanel left = Theme.makeBorderPanel(0, 10);
        left.add(Theme.makeTableScroll(table), BorderLayout.CENTER);
        left.add(Theme.makeRow("left", resetButton, deleteButton), BorderLayout.SOUTH);

        // ---------------- RIGHT: the Create Account form ----------------
        Theme.styleField(fullNameField);
        Theme.styleField(usernameField);
        Theme.styleField(passwordField);

        JButton createButton = Theme.goldButton("Create Cashier Account");
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createCashier();
            }
        });

        JPanel form = Theme.makeStackedPanel(6);
        form.add(Theme.makeLabel("Full name:"));
        form.add(fullNameField);
        form.add(Theme.makeLabel("Username:"));
        form.add(usernameField);
        form.add(Theme.makeLabel("Password (at least " + AccountStore.MIN_PASSWORD_LENGTH + " characters):"));
        form.add(passwordField);

        JPanel card = Theme.makeCard(16, 18);
        card.add(Theme.makeLabel("New Cashier Account", Theme.georgiaFont(18), Theme.DARK_GREEN), BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(createButton, BorderLayout.SOUTH);

        JPanel right = Theme.makeBorderPanel(0, 0);
        right.setPreferredSize(new Dimension(360, 0));
        right.add(card, BorderLayout.NORTH);

        JPanel center = Theme.makeBorderPanel(14, 0);
        center.add(left, BorderLayout.CENTER);
        center.add(right, BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        shownCashiers = accounts.getCashiers();
        for (Account cashier : shownCashiers) {
            tableModel.addRow(new Object[]{cashier.getFullName(), cashier.getUsername()});
        }
    }

    private Account getSelectedCashier() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < shownCashiers.size()) {
            return shownCashiers.get(row);
        }
        return null;
    }

    private void createCashier() {
        String error = accounts.addCashier(fullNameField.getText().trim(),
                usernameField.getText().trim(), new String(passwordField.getPassword()));
        if (error != null) {
            Theme.showMessage(this, error);
            return;
        }
        Theme.showMessage(this, "Cashier account created for " + fullNameField.getText().trim()
                + ".\nThey can now log in on the Cashier program.");
        fullNameField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        refreshData();
    }

    private void resetPassword() {
        Account selected = getSelectedCashier();
        if (selected == null) {
            Theme.showMessage(this, "Please select a cashier first.");
            return;
        }
        String newPassword = Theme.askPassword(this, "New password for " + selected.getUsername() + ":", "Reset Password");
        if (newPassword != null) {      // null = the admin clicked Cancel
            String error = accounts.resetPassword(selected, newPassword);
            Theme.showMessage(this, error == null ? "Password changed." : error);
        }
    }

    private void deleteCashier() {
        Account selected = getSelectedCashier();
        if (selected == null) {
            Theme.showMessage(this, "Please select a cashier first.");
            return;
        }
        if (Theme.askYesNoWarning(this, "Delete the account of " + selected.getFullName()
                + " (" + selected.getUsername() + ")?", "Delete Account")) {
            if (!accounts.deleteCashier(selected)) {
                Theme.showMessage(this, "The account could not be deleted (the file could not be saved).");
            }
            refreshData();
        }
    }
}
