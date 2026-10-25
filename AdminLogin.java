package jlaluxurywatch;

import javax.swing.*;

public class AdminLogin extends LoginPage {
    private AccountStore accountStore;

    public AdminLogin() {
        this(new AccountStore());
    }

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
        if (accountStore.usesDefaultPassword(account)) {
            JOptionPane.showMessageDialog(dash, "You are still using the default password.\nPlease choose a new one now.",
                    "Change your password", JOptionPane.WARNING_MESSAGE);
            AdminDash.changePassword(dash, accountStore, account);
        }
    }

    public static void open() {
        new AdminLogin().setVisible(true);
    }

    public static void main(String[] args) {
        open();
    }
}
