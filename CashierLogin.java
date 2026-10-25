package jlaluxurywatch;

import javax.swing.*;

public class CashierLogin extends LoginPage {

    public CashierLogin() {
        super(new AccountStore(), Account.CASHIER, "Cashier Login");
    }

    @Override
    protected void openDashboard(Account account) {
        OrderStore orderStore = new OrderStore(new WatchStore());
        new CashierDash(account, orderStore).setVisible(true);
    }

    public static void open() {
        new CashierLogin().setVisible(true);
    }

    public static void main(String[] args) {
        open();
    }
}
