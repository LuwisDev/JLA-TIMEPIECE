package jlaluxurywatch;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

// AdminTransactionsPanel
// Every transaction of the store, current AND past (newest first): Pending, Reserved,
// Processing, Processed/Received and Cancelled. The admin can only LOOK at them
// (filter by status, search by name / reference / watch). The cashier is the one who changes statuses.
public class AdminTransactionsPanel extends JPanel {
    private OrderStore store;
    private ArrayList<Transaction> shownTransactions = new ArrayList<Transaction>();

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterBox;
    private JTextField searchField;
    private JTextField referenceCodeField = new JTextField();   // the "find by reference code" box
    private JTextArea receiptArea;
    private JLabel totalLabel;

    public AdminTransactionsPanel(OrderStore store) {
        this.store = store;
        Theme.setupPage(this, 10, 20);

        // ---------------- TOP: heading + search + filter + find by reference code ----------------
        String[] filterChoices = new String[Transaction.STATUSES.length + 1];
        filterChoices[0] = "All";
        for (int i = 0; i < Transaction.STATUSES.length; i++) {
            filterChoices[i + 1] = Transaction.STATUSES[i];
        }
        filterBox = Theme.makeComboBox(filterChoices);

        searchField = new JTextField(16);
        Theme.styleField(searchField);

        JPanel filterRow = Theme.makeRow("right", Theme.makeLabel("Search:"), searchField,
                Theme.makeLabel("Status:"), filterBox);

        JPanel header = Theme.makeBorderPanel(0, 10);
        header.add(Theme.makeTopBar("ALL TRANSACTIONS", filterRow), BorderLayout.NORTH);
        Runnable whenFind = new Runnable() {
            @Override
            public void run() {
                findTransaction();
            }
        };
        Runnable whenRefresh = new Runnable() {
            @Override
            public void run() {
                store.refresh();
                refreshData();
            }
        };
        header.add(Theme.makeFindRow(referenceCodeField, whenFind, whenRefresh), BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // ---------------- CENTER LEFT: the table ----------------
        tableModel = new DefaultTableModel(new String[]{"Order", "Date", "Customer", "Watch", "Qty", "Payment", "Amount", "Status"}, 0);
        table = new JTable(tableModel);
        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] widths = {50, 115, 80, 125, 35, 65, 95, 125};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                showReceipt();
            }
        });

        // ---------------- CENTER RIGHT: the receipt ----------------
        receiptArea = Theme.makeReceiptArea();

        JPanel center = Theme.makeBorderPanel(12, 0);
        center.add(Theme.makeTableScroll(table), BorderLayout.CENTER);
        center.add(Theme.makeTitledScroll(receiptArea, "Receipt", 300), BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        // ---------------- BOTTOM: totals ----------------
        totalLabel = Theme.makeLabel(" ", Theme.boldFont(16), Theme.DARK_GREEN);
        add(totalLabel, BorderLayout.SOUTH);

        // added last so the screen is complete first
        filterBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshData();
            }
        });
        Theme.whenTyping(searchField, new Runnable() {
            @Override
            public void run() {
                refreshData();
            }
        });      // search while the user types
        refreshData();
    }

    //  Finds an order (any status) by its reference code and selects its row in the table.
    private void findTransaction() {
        String code = referenceCodeField.getText().trim();
        if (code.isEmpty()) {
            Theme.showMessage(this, "Please enter the reference code.");
            return;
        }
        Transaction found = store.findByReferenceCode(code);
        if (found == null) {
            Theme.showMessage(this, "No order found with that reference code.");
            return;
        }
        searchField.setText("");                // clear the filters, so the order cannot be hidden
        filterBox.setSelectedItem("All");
        refreshData();
        for (int i = 0; i < shownTransactions.size(); i++) {
            if (shownTransactions.get(i) == found) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));   // scroll the table to the row
            }
        }
    }

    //  Reloads the table (newest first) using the status filter and the search text.
    public void refreshData() {
        Transaction previouslySelected = getSelectedTransaction();
        String filter = (String) filterBox.getSelectedItem();
        String search = searchField.getText().trim().toLowerCase();
        ArrayList<Transaction> allTransactions = store.getTransactions();

        tableModel.setRowCount(0);
        shownTransactions.clear();
        for (int i = allTransactions.size() - 1; i >= 0; i--) {
            Transaction transaction = allTransactions.get(i);
            boolean matchesStatus = filter.equals("All") || filter.equals(transaction.getStatus());
            String searchableText = (transaction.getCustomerName() + " " + transaction.getReferenceCode() + " "
                    + transaction.getWatch().getDisplayName() + " " + transaction.getTransactionId()).toLowerCase();
            boolean matchesSearch = search.isEmpty() || searchableText.contains(search);
            if (matchesStatus && matchesSearch) {
                shownTransactions.add(transaction);
                tableModel.addRow(new Object[]{transaction.getTransactionId(), transaction.getFormattedDate(),
                        transaction.getCustomerName(), transaction.getWatch().getDisplayName(),
                        transaction.getQuantity(), transaction.getPayment().getName(),
                        Money.format(transaction.getTotal()), transaction.getStatus()});
                if (transaction == previouslySelected) {
                    int lastRow = shownTransactions.size() - 1;
                    table.setRowSelectionInterval(lastRow, lastRow);
                }
            }
        }
        showReceipt();
        totalLabel.setText("Showing " + shownTransactions.size() + " of " + allTransactions.size() + " transactions"
                + "    Received sales: " + Money.format(store.getReceivedSales())
                + "    Pending: " + store.countByStatus(Transaction.PENDING)
                + "    Cancelled: " + store.countByStatus(Transaction.CANCELLED));
    }

    private Transaction getSelectedTransaction() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < shownTransactions.size()) {
            return shownTransactions.get(row);
        }
        return null;
    }

    private void showReceipt() {
        Transaction selected = getSelectedTransaction();
        if (selected == null) {
            receiptArea.setText("Select a transaction to see its receipt.");
        } else {
            receiptArea.setText(selected.getReceiptText());
            receiptArea.setCaretPosition(0);
        }
    }
}
