package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class PurchasePanel extends JPanel {
    private WatchStore watchStore;
    private OrderStore orderStore;
    private JLADash dash;
    private Watch selectedWatch;
    private JLabel watchImage;
    private JLabel watchName;
    private JLabel watchPrice;
    private JButton chooseButton;
    private JTextField customerField;
    private JComboBox<Payment> paymentBox;
    private JSpinner quantitySpinner;
    private JTextArea receiptArea;

    private static final String NAME_PATTERN = "[\\p{L} .'-]{2,60}";
    private static final int MAX_WAITING_ORDERS = 3;

    public PurchasePanel(WatchStore watchStore, OrderStore orderStore, JLADash dash) {
        this.watchStore = watchStore;
        this.orderStore = orderStore;
        this.dash = dash;
        Theme.setupPage(this, 12, 20);
        add(Theme.makeHeading("PURCHASE WATCH"), BorderLayout.NORTH);

        watchImage = Theme.makeLabel("No watch", Theme.BODY_FONT, Theme.CREAM);
        watchImage.setHorizontalAlignment(SwingConstants.CENTER);
        watchImage.setPreferredSize(new Dimension(90, 90));
        watchName = Theme.makeLabel("No watch selected", Theme.boldFont(13), Theme.CREAM);
        watchPrice = Theme.makeLabel("Click \"Choose Watch\"", Theme.BOLD_FONT, Theme.GOLD);

        JPanel watchText = new JPanel(new GridLayout(2, 1, 0, 4));
        watchText.setOpaque(false);
        watchText.add(watchName);
        watchText.add(watchPrice);

        JPanel watchCard = new JPanel(new BorderLayout(12, 0));
        watchCard.setBackground(Theme.DARK_GREEN);
        watchCard.setBorder(Theme.goldBorder(2, 8));
        watchCard.add(watchImage, BorderLayout.WEST);
        watchCard.add(watchText, BorderLayout.CENTER);

        chooseButton = Theme.greenButton("Choose Watch");
        chooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openWatchPicker();
            }
        });

        customerField = new JTextField();
        Theme.styleField(customerField);

        paymentBox = Theme.makeComboBox(Payment.getAllPayments());

        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, Transaction.MAX_QUANTITY, 1));
        quantitySpinner.setFont(Theme.BODY_FONT);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.setBackground(Theme.CREAM);
        form.add(Theme.makeLabel("Customer name:"));
        form.add(customerField);
        form.add(Theme.makeLabel("Quantity (max " + Transaction.MAX_QUANTITY + "):"));
        form.add(quantitySpinner);
        form.add(Theme.makeLabel("Payment method:"));
        form.add(paymentBox);

        JPanel top = Theme.makeColumn();
        Theme.alignLeft(watchCard, chooseButton, form);
        chooseButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        top.add(watchCard);
        top.add(Theme.gap(10));
        top.add(chooseButton);
        top.add(Theme.gap(20));
        top.add(form);

        JButton proceed = Theme.goldButton("Proceed to Purchase");
        proceed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                proceedToPurchase();
            }
        });

        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(Theme.CREAM);
        left.add(top, BorderLayout.NORTH);
        left.add(proceed, BorderLayout.SOUTH);

        receiptArea = Theme.makeReceiptArea();

        add(left, BorderLayout.CENTER);
        add(Theme.makeTitledScroll(receiptArea, "Receipt", 380), BorderLayout.EAST);
    }

    public void setSelectedWatch(Watch watch) {
        selectedWatch = watch;
        Theme.showImage(watchImage, watch.getImagePath(), 90, 90, "No image");
        watchName.setText(watch.getDisplayName());
        watchPrice.setText(Money.format(watch.getPrice()) + " each");
        chooseButton.setText("Change Watch");
    }

    private void openWatchPicker() {
        JDialog picker = new JDialog(dash, "Choose a Watch", true);

        JPanel header = Theme.makeBorderPanel(0, 2);
        header.add(Theme.makeHeading("CHOOSE A WATCH"), BorderLayout.NORTH);
        header.add(Theme.makeLabel("Pick the watch you want to purchase.", Theme.BODY_FONT, Theme.DARK_GREEN),
                BorderLayout.SOUTH);

        WatchGallery gallery = new WatchGallery(watchStore, "Select Watch", 730);
        for (WatchCard card : gallery.getCards()) {
            if (!card.getWatch().isAvailable()) {
                card.getButton().setText("Not Available");
                card.getButton().setEnabled(false);
            }
            card.getButton().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setSelectedWatch(card.getWatch());
                    picker.dispose();
                }
            });
        }

        JButton cancel = Theme.greenButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                picker.dispose();
            }
        });

        JPanel root = Theme.makeDialogRoot(0, 10);
        root.add(header, BorderLayout.NORTH);
        root.add(Theme.makeScroll(gallery), BorderLayout.CENTER);
        root.add(Theme.makeRow("right", cancel), BorderLayout.SOUTH);
        root.setPreferredSize(new Dimension(800, 620));
        Theme.finishDialog(picker, root, dash);
        picker.setVisible(true);
    }

    private void proceedToPurchase() {
        if (selectedWatch == null) {
            Theme.showMessage(this, "Please choose a watch first.");
            return;
        }
        String customer = customerField.getText().trim();
        if (customer.isEmpty()) {
            Theme.showMessage(this, "Please enter the customer name.");
            return;
        }
        if (!customer.matches(NAME_PATTERN)) {
            Theme.showMessage(this, "Please enter a real name: 2 to 60 letters (spaces, . ' - are allowed).");
            return;
        }
        if (orderStore.countWaitingByCustomer(customer) >= MAX_WAITING_ORDERS) {
            Theme.showMessage(this, "You already have " + MAX_WAITING_ORDERS
                    + " orders waiting for the cashier.\nPlease wait until they are finished.");
            return;
        }
        Payment payment = (Payment) paymentBox.getSelectedItem();
        int quantity = (Integer) quantitySpinner.getValue();

        PurchaseSummaryDialog summary = new PurchaseSummaryDialog(dash, selectedWatch, customer, payment, quantity);
        summary.setVisible(true);
        if (!summary.isConfirmed()) {
            return;
        }

        String paymentReference = summary.getPaymentReference();
        if (!paymentReference.equals("") && orderStore.isPaymentRefUsed(paymentReference)) {
            Theme.showMessage(this, "This reference number was already used for another purchase.");
            return;
        }

        String referenceCode = orderStore.generateReferenceCode();
        Transaction transaction = new Transaction(customer, selectedWatch, payment, referenceCode, paymentReference, quantity);
        if (!orderStore.addTransaction(transaction)) {
            Theme.showError(this, "Sorry, your order could NOT be saved. No order was placed.\nPlease tell the cashier.\n"
                    + orderStore.getLastError(), "Save failed");
            return;
        }

        receiptArea.setText(transaction.getReceiptText());
        Theme.showMessage(this, "Order placed! Your receipt is shown on the right.");
        customerField.setText("");
        quantitySpinner.setValue(1);
    }
}
