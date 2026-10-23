package jlaluxurywatch;

import javax.swing.*;

// AdminLogin
// The login window of the Admin program. This class has main(), so it is the one you run
// for the admin side.
//
// First run: username  admin   password  admin123
public class AdminLogin extends LoginPage {
    private AccountStore accountStore;

    public AdminLogin() {
        this(new AccountStore());
    }

    // the same AccountStore is given to the login check AND kept here for the dashboard
    private AdminLogin(AccountStore accountStore) {
        super(accountStore, Account.ADMIN, "Admin Login");
        this.accountStore = accountStore;
    }

    @Override
    protected void openDashboard(Account account) {
        WatchStore watchStore = new WatchStore();
        OrderStore orderStore = new OrderStore(watchStore);
        AdminDash dash = new AdminDash(account, watchStore, orderStore, accountStore);
        dash.setVisible(true);
        // the default password is written in the source code, so ask for a new one right away
        if (accountStore.usesDefaultPassword(account)) {
            JOptionPane.showMessageDialog(dash, "You are still using the default password.\nPlease choose a new one now.",
                    "Change your password", JOptionPane.WARNING_MESSAGE);
            AdminDash.changePassword(dash, accountStore, account);
        }
    }

    //  Shows this login window. Other classes call this method to come here.
    public static void open() {
        new AdminLogin().setVisible(true);
    }

    public static void main(String[] args) {
        open();
    }
}
