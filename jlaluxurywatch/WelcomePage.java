package jlaluxurywatch;

// WelcomePage (Customer)
// The welcome window of the Customer program. One button: ENTER.
// This class has main(), so it is the one you run for the customer side.
public class WelcomePage extends WelcomeFrame {
    private WatchStore watchStore;
    private OrderStore orderStore;

    //  Used when the program starts: loads the watches and the saved orders.
    public WelcomePage() {
        this(new WatchStore());
    }

    private WelcomePage(WatchStore watchStore) {
        this(watchStore, new OrderStore(watchStore));
    }

    //  Used when we come BACK from the dashboard: we keep the same stores.
    public WelcomePage(WatchStore watchStore, OrderStore orderStore) {
        super(watchStore, "WELCOME, CUSTOMER", "ENTER");
        this.watchStore = watchStore;
        this.orderStore = orderStore;
    }

    @Override
    protected void onButtonClick() {
        new JLADash(watchStore, orderStore).setVisible(true);
        dispose();
    }

    //  Shows a new welcome page (loads the stores).
    public static void open() {
        new WelcomePage().setVisible(true);
    }

    //  Shows the welcome page again with the SAME stores, so nothing is lost.
    public static void open(WatchStore watchStore, OrderStore orderStore) {
        new WelcomePage(watchStore, orderStore).setVisible(true);
    }

    public static void main(String[] args) {
        open();
    }
}
