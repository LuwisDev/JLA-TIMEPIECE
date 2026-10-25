package jlaluxurywatch;

public class AdminWelcome extends WelcomeFrame {

    public AdminWelcome() {
        super(new WatchStore(), "ADMIN PORTAL", "LOGIN");
    }

    @Override
    protected void onButtonClick() {
        AdminLogin.open();
        dispose();
    }

    public static void open() {
        new AdminWelcome().setVisible(true);
    }

    public static void main(String[] args) {
        open();
    }
}
