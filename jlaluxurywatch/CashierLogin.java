package jlaluxurywatch;

import javax.swing.*;

// CashierLogin
// The login window of the Cashier program. Only accounts with the Cashier role (created by
// the admin) can enter. This class has main(), so it is the one you run for the cashier side.
public class CashierLogin extends LoginPage {

    public CashierLogin() {
        super(new AccountStore(), Account.CASHIER, "Cashier Login");
    }

    //  POLYMORPHISM: LoginPage calls this after a successful login.
    @Override
    protected void openDashboard(Account account) {
        OrderStore orderStore = new OrderStore(new WatchStore());
        new CashierDash(account, orderStore).setVisible(true);
    }

    //  Shows this login window. Other classes call this method to come here.
    public static void open() {
        new CashierLogin().setVisible(true);
    }

    public static void main(String[] args) {
        open();
    }
}
