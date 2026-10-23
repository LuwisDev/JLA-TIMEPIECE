package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

// CashierTransactionsPanel
// The history of finished orders (Processed/Received) and Cancelled orders.
// The Previous / Next buttons let the cashier go back to older transactions.
public class CashierTransactionsPanel extends JPanel {
    private OrderStore store;
    private ArrayList<Transaction> shownTransactions = new ArrayList<Transaction>();   // same order as the list on screen

    private DefaultListModel<String> listModel;       // the text lines of the list
    private JList<String> transactionList;
    private JComboBox<String> filterBox;              // All / Processed/Received / Cancelled
    private JTextArea detailsArea;
    private JTextField referenceCodeField = new JTextField();   // the "find by reference code" box
    private JLabel positionLabel;                     // "Transaction 2 of 5"
    private JLabel totalLabel;
    private JButton previousButton;
    private JButton nextButton;

    public CashierTransactionsPanel(OrderStore store) {
        this.store = store;
        Theme.setupPage(this, 10, 20);

        detailsArea = Theme.makeReceiptArea();
        positionLabel = Theme.makeLabel(" ", Theme.BOLD_FONT, Theme.DARK_GREEN);
        previousButton = Theme.greenButton("< Previous");
        nextButton = Theme.greenButton("Next >");

        // ---------------- TOP: heading + filter + find by reference code ----------------
        filterBox = Theme.makeComboBox(new String[]{"All", Transaction.RECEIVED, Transaction.CANCELLED});
        JPanel filterRow = Theme.makeRow("right", Theme.makeLabel("Show:"), filterBox);

        JPanel header = Theme.makeBorderPanel(0, 10);
        header.add(Theme.makeTopBar("TRANSACTION HISTORY", filterRow), BorderLayout.NORTH);
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

        // ---------------- LEFT: the list of transactions ----------------
        // A JList shows lines of text. The DefaultListModel holds those lines.
        listModel = new DefaultListModel<String>();
        transactionList = new JList<String>(listModel);
        Theme.styleList(transactionList);
        transactionList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                showSelectedTransaction();
            }
        });   // runs when the selection changes
        JScrollPane listScroll = Theme.makeTableScroll(transactionList);
        listScroll.setPreferredSize(new Dimension(300, 0));

        // ---------------- RIGHT: the receipt of the selected transaction ----------------
        JPanel center = Theme.makeBorderPanel(12, 0);
        center.add(listScroll, BorderLayout.WEST);
        center.add(Theme.makeTableScroll(detailsArea), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // ---------------- BOTTOM: total and Previous / Next ----------------
        totalLabel = Theme.makeLabel(" ", Theme.boldFont(16), Theme.DARK_GREEN);

        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goToPrevious();
            }
        });
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goToNext();
            }
        });

        JPanel bottom = Theme.makeBorderPanel(0, 0);
        bottom.add(totalLabel, BorderLayout.WEST);
        bottom.add(Theme.makeRow("right", positionLabel, previousButton, nextButton), BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        // when the filter changes, rebuild the list (added last so the screen is complete first)
        filterBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshData();
            }
        });
        refreshData();
    }

    //  Finds a finished or cancelled order by its reference code and selects it in the list.
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
        if (!found.isFinished()) {
            // this history only lists finished and cancelled orders
            Theme.showMessage(this, "Order " + found.getReferenceCode() + " (" + found.getCustomerName()
                    + ") is still " + found.getStatus() + ".\nYou can find it in the Orders screen.");
            return;
        }
        filterBox.setSelectedItem("All");       // make sure the filter does not hide it
        refreshData();
        for (int i = 0; i < shownTransactions.size(); i++) {
            if (shownTransactions.get(i) == found) {
                transactionList.setSelectedIndex(i);
            }
        }
    }

    //  Reloads the list (oldest first) and keeps the same transaction selected if it is still there.
    public void refreshData() {
        Transaction previouslySelected = getSelectedTransaction();
        String filter = (String) filterBox.getSelectedItem();

        shownTransactions.clear();
        listModel.clear();
        for (Transaction transaction : store.getTransactions()) {
            // the history only shows finished orders and cancelled orders
            boolean matchesFilter = filter.equals("All") || filter.equals(transaction.getStatus());
            if (transaction.isFinished() && matchesFilter) {
                shownTransactions.add(transaction);
                // the last part of the line: the price, or the word Cancelled
                String lastPart = Money.format(transaction.getTotal());
                if (transaction.getStatus().equals(Transaction.CANCELLED)) {
                    lastPart = "Cancelled";
                }
                listModel.addElement("#" + transaction.getTransactionId() + " | "
                        + transaction.getCustomerName() + " | " + lastPart);
            }
        }

        // select the same transaction as before, or the newest one
        if (!shownTransactions.isEmpty()) {
            int indexToSelect = shownTransactions.size() - 1;
            if (previouslySelected != null) {
                for (int i = 0; i < shownTransactions.size(); i++) {
                    if (shownTransactions.get(i) == previouslySelected) {
                        indexToSelect = i;
                    }
                }
            }
            transactionList.setSelectedIndex(indexToSelect);
        }
        showSelectedTransaction();
        totalLabel.setText("Total sales: " + Money.format(store.getReceivedSales())
                + "    Cancelled: " + store.countByStatus(Transaction.CANCELLED));
    }

    //  The transaction selected in the list (null if nothing is selected).
    private Transaction getSelectedTransaction() {
        int index = transactionList.getSelectedIndex();     // -1 means nothing selected
        if (index >= 0 && index < shownTransactions.size()) {
            return shownTransactions.get(index);
        }
        return null;
    }

    //  Shows the receipt of the selected transaction and updates the Previous / Next buttons.
    private void showSelectedTransaction() {
        int index = transactionList.getSelectedIndex();
        if (index < 0 || index >= shownTransactions.size()) {
            detailsArea.setText("No finished or cancelled orders yet.");
            positionLabel.setText(" ");
            previousButton.setEnabled(false);
            nextButton.setEnabled(false);
            return;
        }
        detailsArea.setText(shownTransactions.get(index).getReceiptText());
        detailsArea.setCaretPosition(0);
        positionLabel.setText("Transaction " + (index + 1) + " of " + shownTransactions.size());
        previousButton.setEnabled(index > 0);                                   // nothing before the first one
        nextButton.setEnabled(index < shownTransactions.size() - 1);            // nothing after the last one
        transactionList.ensureIndexIsVisible(index);                            // scroll the list to it
    }

    //  Goes back to the previous (older) transaction.
    private void goToPrevious() {
        int index = transactionList.getSelectedIndex();
        if (index > 0) {
            transactionList.setSelectedIndex(index - 1);
        }
    }

    //  Goes forward to the next (newer) transaction.
    private void goToNext() {
        int index = transactionList.getSelectedIndex();
        if (index < shownTransactions.size() - 1) {
            transactionList.setSelectedIndex(index + 1);
        }
    }
}
