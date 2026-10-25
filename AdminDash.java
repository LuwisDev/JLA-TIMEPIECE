package jlaluxurywatch;

import javax.swing.*;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class AdminDash extends Dashboard {
    private OrderStore orderStore;
    private AccountStore accountStore;
    private Account admin;
    private ManageCashiersPanel cashiersPanel;
    private AdminTransactionsPanel transactionsPanel;

    public AdminDash(Account admin, WatchStore watchStore, OrderStore orderStore, AccountStore accountStore) {
        super("JLA TIMEPIECE - Admin", "JLA ADMIN", "Admin: " + admin.getFullName());
        this.orderStore = orderStore;
        this.accountStore = accountStore;
        this.admin = admin;

        cashiersPanel = new ManageCashiersPanel(accountStore);
        transactionsPanel = new AdminTransactionsPanel(orderStore);
        addPage("watches", new ManageWatchesPanel(watchStore, this));
        addPage("cashiers", cashiersPanel);
        addPage("transactions", transactionsPanel);

        addMenuButton("Manage Watches", "watches");
        addMenuButton("Cashier Accounts", "cashiers");
        addMenuButton("Transactions", "transactions");
        addBottomButton("Change Password", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changePassword(AdminDash.this, AdminDash.this.accountStore, AdminDash.this.admin);
            }
        });
        addBottomButton("Logout", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AdminWelcome.open();
                dispose();
            }
        });

        finishLayout();
    }

    @Override
    protected void beforeShowingPage(String pageName) {
        orderStore.refresh();
        transactionsPanel.refreshData();
        cashiersPanel.refreshData();
    }

    public static boolean changePassword(Component parent, AccountStore accounts, Account account) {
        String message = " ";
        while (true) {
            JPasswordField oldField = new JPasswordField();
            JPasswordField newField = new JPasswordField();
            JPasswordField confirmField = new JPasswordField();
            int answer = JOptionPane.showConfirmDialog(parent, new Object[]{
                    message,
                    "Current password:", oldField,
                    "New password (at least " + AccountStore.MIN_PASSWORD_LENGTH + " characters):", newField,
                    "Type the new password again:", confirmField},
                    "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return false;
            }
            String newPassword = new String(newField.getPassword());
            if (!newPassword.equals(new String(confirmField.getPassword()))) {
                message = "The two new passwords are not the same.";
                continue;
            }
            String error = accounts.changePassword(account, new String(oldField.getPassword()), newPassword);
            if (error == null) {
                Theme.showMessage(parent, "Password changed.");
                return true;
            }
            message = error;
        }
    }
}
