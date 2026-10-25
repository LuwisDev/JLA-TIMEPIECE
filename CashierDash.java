package jlaluxurywatch;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class CashierDash extends Dashboard {
    private OrderStore orderStore;
    private OrdersPanel ordersPanel;
    private CashierTransactionsPanel transactionsPanel;

    public CashierDash(Account cashier, OrderStore orderStore) {
        super("JLA TIMEPIECE - Cashier", "JLA CASHIER", "Cashier: " + cashier.getFullName());
        this.orderStore = orderStore;

        ordersPanel = new OrdersPanel(orderStore, cashier.getFullName() + " (" + cashier.getUsername() + ")");
        transactionsPanel = new CashierTransactionsPanel(orderStore);
        addPage("orders", ordersPanel);
        addPage("transactions", transactionsPanel);

        addMenuButton("Orders", "orders");
        addMenuButton("Transactions", "transactions");
        addBottomButton("Logout", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CashierWelcome.open();
                dispose();
            }
        });

        finishLayout();
    }

    @Override
    protected void beforeShowingPage(String pageName) {
        orderStore.refresh();
        ordersPanel.refreshData();
        transactionsPanel.refreshData();
    }
}
