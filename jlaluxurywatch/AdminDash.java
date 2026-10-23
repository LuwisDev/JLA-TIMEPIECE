package jlaluxurywatch;

import javax.swing.*;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// AdminDash
// The Admin dashboard. The admin can:
//   1. Manage Watches       - add, edit and delete watches (CRUD)
//   2. Cashier Accounts     - create accounts for cashiers
//   3. Transactions         - see every transaction, including past ones
//
// The Change Password window used to be its own class (ChangePasswordDialog). It was only
// used by the admin, so it is now the method changePassword() in this class.
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

        finishLayout();     // must be the LAST step
    }

    //  Reloads the orders and the cashier list every time a page is opened.
    @Override
    protected void beforeShowingPage(String pageName) {
        orderStore.refresh();
        transactionsPanel.refreshData();
        cashiersPanel.refreshData();
    }

    // Lets the logged-in admin change his or her OWN password (the old password must be typed first).
    // It keeps asking until the password was changed or the admin clicks Cancel.
    // Returns true if the password was changed. Call it like: AdminDash.changePassword(parent, accounts, account);
    public static boolean changePassword(Component parent, AccountStore accounts, Account account) {
        String message = " ";       // the line at the top of the window (shows what went wrong)
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
                return false;       // the admin clicked Cancel
            }
            String newPassword = new String(newField.getPassword());
            if (!newPassword.equals(new String(confirmField.getPassword()))) {
                message = "The two new passwords are not the same.";
                continue;           // ask again
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
