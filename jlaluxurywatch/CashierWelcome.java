package jlaluxurywatch;

// CashierWelcome
// The welcome window of the Cashier program. One button: LOGIN.
// This class has main(), so it is the one you run for the cashier side.
public class CashierWelcome extends WelcomeFrame {

    public CashierWelcome() {
        super(new WatchStore(), "CASHIER PORTAL", "LOGIN");
    }

    @Override
    protected void onButtonClick() {
        CashierLogin.open();
        dispose();
    }

    //  Shows this welcome window. Other classes call this method to come here.
    public static void open() {
        new CashierWelcome().setVisible(true);
    }

    public static void main(String[] args) {
        open();
    }
}
