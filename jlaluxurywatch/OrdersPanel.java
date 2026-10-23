package jlaluxurywatch;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

// OrdersPanel
// The cashier's working screen: the orders that are still waiting (Pending, Reserved or Processing).
//
//   - search an order by its reference code (the code printed on the customer's receipt)
//   - a NEW order is "Pending": it has no status yet. The cashier is the one who marks it:
//       choose Reserved, Processing or Processed/Received in the drop-down, then click Update Status
//   - Cancel Order             (for orders made by mistake)
//   - the customer's receipt is shown on the right
//
// Finished and cancelled orders leave this screen and go to the Transactions screen.
public class OrdersPanel extends JPanel {
    private OrderStore store;
    private String staffName;                        // the logged-in cashier, saved on every status change
    private Transaction currentOrder;                 // the order the cashier is looking at (null = none yet)
    private ArrayList<Transaction> shownOrders = new ArrayList<Transaction>();   // orders in the table, in the same order as the rows

    // ----- parts of the screen that change while the program runs -----
    private JTable ordersTable;
    private DefaultTableModel tableModel;             // the data inside the table (rows and columns)
    private JTextField referenceCodeField = new JTextField();
    private JTextArea receiptArea;
    private JLabel hintLabel;
    private JLabel summaryLabel;
    private JComboBox<String> statusBox;             // the statuses the cashier can choose
    private JButton updateStatusButton;
    private JButton cancelOrderButton;

    public OrdersPanel(OrderStore store, String staffName) {
        this.store = store;
        this.staffName = staffName;
        Theme.setupPage(this, 10, 20);

        hintLabel = Theme.makeLabel(" ", Theme.BOLD_FONT, Theme.DARK_GREEN);
        summaryLabel = Theme.makeLabel(" ", Theme.BODY_FONT, Theme.DARK_GREEN);
        receiptArea = Theme.makeReceiptArea();

        // ---------------- TOP: heading + search by reference code ----------------
        // makeFindRow builds the "Reference code: [____] [Find] [Refresh]" row
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

        // ---------------- CENTER LEFT: table of waiting orders ----------------
        // A JTable shows rows and columns. The DefaultTableModel holds the actual data.
        tableModel = new DefaultTableModel(new String[]{"Order #", "Reference", "Customer", "Status"}, 0);
        ordersTable = new JTable(tableModel);
        Theme.styleTable(ordersTable);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);    // only one row can be selected
        int[] columnWidths = {75, 100, 125, 110};
        for (int i = 0; i < columnWidths.length; i++) {
            ordersTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }
        // runs every time the selected row changes (the cashier clicks a row)
        ordersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                showOrderOfSelectedRow();
            }
        });

        // ---------------- CENTER RIGHT: the receipt of the current order ----------------
        JPanel center = Theme.makeBorderPanel(12, 0);
        center.add(Theme.makeTableScroll(ordersTable), BorderLayout.CENTER);
        center.add(Theme.makeTitledScroll(receiptArea, "Customer Receipt", 300), BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        // ---------------- BOTTOM: hint, buttons and counts ----------------
        // The cashier picks the status here. Nothing is marked automatically.
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

    // ---------------------------------------------------------------------------
    //                         drawing the screen
    // ---------------------------------------------------------------------------

    //  Reloads the waiting orders (newest first) and keeps the current order selected.
    public void refreshData() {
        ArrayList<Transaction> allTransactions = store.getTransactions();

        tableModel.setRowCount(0);      // remove all rows...
        shownOrders.clear();
        // ...and add them again, going from the last (newest) order to the first
        for (int i = allTransactions.size() - 1; i >= 0; i--) {
            Transaction order = allTransactions.get(i);
            if (order.isWaiting()) {
                shownOrders.add(order);
                tableModel.addRow(new Object[]{order.getTransactionId(), order.getReferenceCode(),
                        order.getCustomerName(), order.getStatus()});
            }
        }

        // if the current order is still waiting, select its row again
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

    //  The cashier clicked a row: that order becomes the current order.
    private void showOrderOfSelectedRow() {
        int row = ordersTable.getSelectedRow();     // -1 means no row is selected
        if (row >= 0 && row < shownOrders.size()) {
            currentOrder = shownOrders.get(row);
            showCurrentOrder();
        }
    }

    //  Turns the status drop-down and the two buttons on or off together.
    private void setControlsEnabled(boolean enabled) {
        statusBox.setEnabled(enabled);
        updateStatusButton.setEnabled(enabled);
        cancelOrderButton.setEnabled(enabled);
    }

    //  Shows the receipt of the current order and turns the right buttons on or off.
    private void showCurrentOrder() {
        // no order chosen yet
        if (currentOrder == null) {
            receiptArea.setText("Select an order from the list, or type a reference code and click Find.");
            hintLabel.setText(" ");
            setControlsEnabled(false);
            return;
        }

        receiptArea.setText(currentOrder.getReceiptText());
        receiptArea.setCaretPosition(0);        // scroll the receipt back to the top
        String status = currentOrder.getStatus();

        if (currentOrder.isWaiting()) {
            setControlsEnabled(true);
            // suggest the next step, but the cashier can choose any status
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
                // POLYMORPHISM: each Payment type writes its own hint (cash amount, or GCash/QR number to check)
                hintLabel.setText(currentOrder.getPayment().getCashierHint(
                        Money.format(currentOrder.getTotal()), currentOrder.getPaymentReference()));
            }
        } else {
            // finished or cancelled: nothing more to do, only look at the receipt
            setControlsEnabled(false);
            hintLabel.setText("This order is " + status + ". You can see it in Transactions.");
        }
    }

    // ---------------------------------------------------------------------------
    //                         what the buttons do
    // ---------------------------------------------------------------------------

    //  Looks for an order by its reference code (any status) and shows its receipt.
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

    //  The cashier marks the order with the status chosen in the drop-down (after a Yes / No question).
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
            // handing over the watch is final: remind the cashier to check the money first
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

    //  Cancels an order made by mistake (or any order that is not finished yet).
    private void cancelOrder() {
        if (currentOrder == null) {
            return;
        }
        // the cashier picks one of these reasons from a drop-down list
        String[] cancelReasons = {"Ordered by mistake", "Customer changed mind", "Payment not received", "Other"};
        String message = "Cancel order #" + currentOrder.getTransactionId() + " ("
                + currentOrder.getCustomerName() + ")?\n\nChoose a reason:";
        // GCash / QR customers paid before coming to the cashier, so remind the cashier to refund
        if (currentOrder.getPayment().isPaidOnline()) {
            message += "\n\nIf the customer already paid by " + currentOrder.getPayment().getName()
                    + ", please refund the payment.";
        }
        String chosenReason = Theme.askChoice(this, message, "Cancel Order", cancelReasons);
        if (chosenReason == null) {
            return;     // the cashier closed the window without choosing: do nothing
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
