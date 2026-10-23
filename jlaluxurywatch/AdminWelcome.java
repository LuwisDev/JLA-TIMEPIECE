package jlaluxurywatch;

// AdminWelcome
// The welcome window of the Admin program. One button: LOGIN.
// This class has main(), so it is the one you run for the admin side.
public class AdminWelcome extends WelcomeFrame {

    public AdminWelcome() {
        super(new WatchStore(), "ADMIN PORTAL", "LOGIN");
    }

    @Override
    protected void onButtonClick() {
        AdminLogin.open();
        dispose();
    }

    //  Shows this welcome window. Other classes call this method to come here.
    public static void open() {
        new AdminWelcome().setVisible(true);
    }

    public static void main(String[] args) {
        open();
    }
}
