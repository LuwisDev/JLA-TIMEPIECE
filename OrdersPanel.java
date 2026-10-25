package jlaluxurywatch;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

public class OrdersPanel extends JPanel {
    private OrderStore store;
    private String staffName;
    private Transaction currentOrder;
    private ArrayList<Transaction> shownOrders = new ArrayList<Transaction>();

    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private JTextField referenceCodeField = new JTextField();
    private JTextArea receiptArea;
    private JLabel hintLabel;
    private JLabel summaryLabel;
    private JComboBox<String> statusBox;
    private JButton updateStatusButton;
    private JButton cancelOrderButton;

    public OrdersPanel(OrderStore store, String staffName) {
        this.store = store;
        this.staffName = staffName;
        Theme.setupPage(this, 10, 20);

        hintLabel = Theme.makeLabel(" ", Theme.BOLD_FONT, Theme.DARK_GREEN);
        summaryLabel = Theme.makeLabel(" ", Theme.BODY_FONT, Theme.DARK_GREEN);
        receiptArea = Theme.makeReceiptArea();

        JPanel top = Theme.makeBorderPanel(0, 10);
        top.add(Theme.makeHeading("ORDERS"), BorderLayout.NORTH);
        Runnable whenFind = new Runnable() {
            @Override
            public void run() {
                findOrder();
            }
        };
        Runnable whenRefresh = new Runnable() {
            @Override
            public void run() {
                OrdersPanel.this.store.refresh();
                refreshData();
            }
        };
        top.add(Theme.makeFindRow(referenceCodeField, whenFind, whenRefresh), BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Order #", "Reference", "Customer", "Status"}, 0);
        ordersTable = new JTable(tableModel);
        Theme.styleTable(ordersTable);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] columnWidths = {75, 100, 125, 110};
        for (int i = 0; i < columnWidths.length; i++) {
            ordersTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }
        ordersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                showOrderOfSelectedRow();
            }
        });

        JPanel center = Theme.makeBorderPanel(12, 0);
        center.add(Theme.makeTableScroll(ordersTable), BorderLayout.CENTER);
        center.add(Theme.makeTitledScroll(receiptArea, "Customer Receipt", 300), BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        statusBox = Theme.makeComboBox(Transaction.CASHIER_MARKS);
        updateStatusButton = Theme.goldButton("Update Status");
        updateStatusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateStatus();
            }
        });
        cancelOrderButton = Theme.greenButton("Cancel Order");
        cancelOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelOrder();
            }
        });

        JPanel south = Theme.makeStackedPanel(8);
        south.add(hintLabel);
        south.add(Theme.makeRow("left", Theme.makeLabel("Mark order as:"), statusBox, updateStatusButton, cancelOrderButton));
        south.add(summaryLabel);
        add(south, BorderLayout.SOUTH);

        refreshData();
    }

    public void refreshData() {
        ArrayList<Transaction> allTransactions = store.getTransactions();

        tableModel.setRowCount(0);
        shownOrders.clear();
        for (int i = allTransactions.size() - 1; i >= 0; i--) {
            Transaction order = allTransactions.get(i);
            if (order.isWaiting()) {
                shownOrders.add(order);
                tableModel.addRow(new Object[]{order.getTransactionId(), order.getReferenceCode(),
                        order.getCustomerName(), order.getStatus()});
            }
        }

        if (currentOrder != null) {
            for (int i = 0; i < shownOrders.size(); i++) {
                if (shownOrders.get(i) == currentOrder) {
                    ordersTable.setRowSelectionInterval(i, i);
                }
            }
        }
        showCurrentOrder();

        summaryLabel.setText("Pending: " + store.countByStatus(Transaction.PENDING)
                + "    Reserved: " + store.countByStatus(Transaction.RESERVED)
                + "    Processing: " + store.countByStatus(Transaction.PROCESSING)
                + "    (finished and cancelled orders are in Transactions)");
    }

    private void showOrderOfSelectedRow() {
        int row = ordersTable.getSelectedRow();
        if (row >= 0 && row < shownOrders.size()) {
            currentOrder = shownOrders.get(row);
            showCurrentOrder();
        }
    }

    private void setControlsEnabled(boolean enabled) {
        statusBox.setEnabled(enabled);
        updateStatusButton.setEnabled(enabled);
        cancelOrderButton.setEnabled(enabled);
    }

    private void showCurrentOrder() {
        if (currentOrder == null) {
            receiptArea.setText("Select an order from the list, or type a reference code and click Find.");
            hintLabel.setText(" ");
            setControlsEnabled(false);
            return;
        }

        receiptArea.setText(currentOrder.getReceiptText());
        receiptArea.setCaretPosition(0);
        String status = currentOrder.getStatus();

        if (currentOrder.isWaiting()) {
            setControlsEnabled(true);
            if (status.equals(Transaction.PENDING)) {
                statusBox.setSelectedItem(Transaction.RESERVED);
            } else if (status.equals(Transaction.RESERVED)) {
                statusBox.setSelectedItem(Transaction.PROCESSING);
            } else {
                statusBox.setSelectedItem(Transaction.RECEIVED);
            }
            if (status.equals(Transaction.PROCESSING)) {
                hintLabel.setText("Hand the watch to the customer, then mark the order as Processed/Received.");
            } else {
                hintLabel.setText(currentOrder.getPayment().getCashierHint(
                        Money.format(currentOrder.getTotal()), currentOrder.getPaymentReference()));
            }
        } else {
            setControlsEnabled(false);
            hintLabel.setText("This order is " + status + ". You can see it in Transactions.");
        }
    }

    private void findOrder() {
        String code = referenceCodeField.getText().trim();
        if (code.isEmpty()) {
            Theme.showMessage(this, "Please enter the reference code.");
            return;
        }
        Transaction foundOrder = store.findByReferenceCode(code);
        if (foundOrder == null) {
            Theme.showMessage(this, "No order found with that reference code.");
            return;
        }
        currentOrder = foundOrder;
        refreshData();
    }

    private void updateStatus() {
        if (currentOrder == null) {
            return;
        }
        String newStatus = (String) statusBox.getSelectedItem();
        if (newStatus.equals(currentOrder.getStatus())) {
            Theme.showMessage(this, "This order is already " + newStatus + ".");
            return;
        }
        String question = "Mark order #" + currentOrder.getTransactionId() + " (" + currentOrder.getCustomerName()
                + ") as " + newStatus + "?";
        if (newStatus.equals(Transaction.RECEIVED)) {
            question += "\n\nHave you received the full payment of " + Money.format(currentOrder.getTotal()) + "?";
        }
        if (Theme.askYesNo(this, question, "Update Status")) {
            if (!store.updateStatus(currentOrder, newStatus, staffName)) {
                Theme.showError(this, "The change could NOT be saved. Please try again.\n" + store.getLastError(),
                        "Save failed");
            }
        }
        refreshData();
    }

    private void cancelOrder() {
        if (currentOrder == null) {
            return;
        }
        String[] cancelReasons = {"Ordered by mistake", "Customer changed mind", "Payment not received", "Other"};
        String message = "Cancel order #" + currentOrder.getTransactionId() + " ("
                + currentOrder.getCustomerName() + ")?\n\nChoose a reason:";
        if (currentOrder.getPayment().isPaidOnline()) {
            message += "\n\nIf the customer already paid by " + currentOrder.getPayment().getName()
                    + ", please refund the payment.";
        }
        String chosenReason = Theme.askChoice(this, message, "Cancel Order", cancelReasons);
        if (chosenReason == null) {
            return;
        }
        int result = store.cancelOrder(currentOrder, chosenReason, staffName);
        if (result == OrderStore.CANCEL_TOO_LATE) {
            Theme.showMessage(this, "This order can no longer be cancelled.");
        } else if (result == OrderStore.CANCEL_SAVE_FAILED) {
            Theme.showError(this, "The cancellation could NOT be saved. Please try again.\n" + store.getLastError(),
                    "Save failed");
        }
        refreshData();
    }
}
