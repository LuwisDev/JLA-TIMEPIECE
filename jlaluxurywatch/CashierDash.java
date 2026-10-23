package jlaluxurywatch;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// CashierDash
// The Cashier dashboard (opened after the cashier logs in). Like JLADash, it only says WHAT
// it contains (two pages); the window, the sidebar and the page switching come from Dashboard.
public class CashierDash extends Dashboard {
    private OrderStore orderStore;
    private OrdersPanel ordersPanel;
    private CashierTransactionsPanel transactionsPanel;

    public CashierDash(Account cashier, OrderStore orderStore) {
        super("JLA TIMEPIECE - Cashier", "JLA CASHIER", "Cashier: " + cashier.getFullName());
        this.orderStore = orderStore;

        // ---- the two pages ----
        ordersPanel = new OrdersPanel(orderStore, cashier.getFullName() + " (" + cashier.getUsername() + ")");
        transactionsPanel = new CashierTransactionsPanel(orderStore);
        addPage("orders", ordersPanel);
        addPage("transactions", transactionsPanel);

        // ---- the sidebar buttons ----
        addMenuButton("Orders", "orders");
        addMenuButton("Transactions", "transactions");
        addBottomButton("Logout", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CashierWelcome.open();    // back to the login window
                dispose();
            }
        });

        finishLayout();     // must be the LAST step
    }

    // POLYMORPHISM: the parent class Dashboard calls this method every time a page is opened.
    // The cashier's version reloads the orders, so the cashier always sees orders that the
    // Customer program just saved.
    @Override
    protected void beforeShowingPage(String pageName) {
        orderStore.refresh();               // read the orders file again
        ordersPanel.refreshData();          // redraw the Orders page
        transactionsPanel.refreshData();    // redraw the Transactions page
    }
}
