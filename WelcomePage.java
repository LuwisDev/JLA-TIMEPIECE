package jlaluxurywatch;

public class WelcomePage extends WelcomeFrame {
    private WatchStore watchStore;
    private OrderStore orderStore;

    public WelcomePage() {
        this(new WatchStore());
    }

    private WelcomePage(WatchStore watchStore) {
        this(watchStore, new OrderStore(watchStore));
    }

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

    public static void open() {
        new WelcomePage().setVisible(true);
    }

    public static void open(WatchStore watchStore, OrderStore orderStore) {
        new WelcomePage(watchStore, orderStore).setVisible(true);
    }

    public static void main(String[] args) {
        open();
    }
}
