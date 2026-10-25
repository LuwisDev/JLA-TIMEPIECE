package jlaluxurywatch;

public class CashierWelcome extends WelcomeFrame {

    public CashierWelcome() {
        super(new WatchStore(), "CASHIER PORTAL", "LOGIN");
    }

    @Override
    protected void onButtonClick() {
        CashierLogin.open();
        dispose();
    }

    public static void open() {
        new CashierWelcome().setVisible(true);
    }

    public static void main(String[] args) {
        open();
    }
}
