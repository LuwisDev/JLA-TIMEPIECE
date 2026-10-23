package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// PurchaseSummaryDialog
// Shows the purchase details before the order is recorded. The customer clicks
// "Confirm Purchase" or "Back".
//
// POLYMORPHISM: this window does not check "is it GCash? is it QR?". It simply asks the
// Payment object what to show (instruction, number, picture, reference label), and each
// kind of payment answers in its own way.
public class PurchaseSummaryDialog extends JDialog {
    private boolean confirmed = false;          // becomes true only if "Confirm Purchase" is clicked
    private JTextField referenceField;          // where the customer types the GCash / QR reference number
    private String paymentReference = "";

    public PurchaseSummaryDialog(JLADash dash, Watch watch, String customer, Payment payment, int quantity) {
        super(dash, "Purchase Details", true);

        // The left column holds the watch picture (and, for QR payments, the QR picture under it).
        JPanel leftColumn = Theme.makeBorderPanel(0, 10);
        leftColumn.add(Theme.makeImageBox(watch.getImagePath(), 190, 230, 170, 200), BorderLayout.CENTER);

        // ---- right: title and the order details ----
        JLabel title = Theme.makeHeading("PURCHASE DETAILS");
        JLabel sub = Theme.makeLabel("Please check your order before you proceed.", Theme.BODY_FONT, Theme.DARK_GREEN);

        JPanel details = Theme.makeStackedPanel(8);
        details.add(makeDetailRow("Watch:", watch.getDisplayName()));
        details.add(makeDetailRow("Price each:", Money.format(watch.getPrice())));
        details.add(makeDetailRow("Quantity:", String.valueOf(quantity)));
        details.add(makeDetailRow("Customer:", customer));
        details.add(makeDetailRow("Payment method:", payment.getName()));

        JLabel totalLabel = Theme.makeLabel("Total to pay: " + Money.format(watch.getPrice() * quantity),
                Theme.boldFont(18), Theme.DARK_GREEN);

        // ---- how to pay: the Payment object gives its own instruction ----
        String instruction = payment.getCustomerInstruction()
                + " After you confirm, your order receipt will show a reference code. "
                + "Show that code to the cashier so your payment can be matched to this purchase.";
        JTextArea note = Theme.makeReadOnlyText(instruction, 6, 28);

        // everything on the right, one under the other
        JPanel info = Theme.makeColumn();
        Theme.alignLeft(title, sub, details, totalLabel, note);
        info.add(title);
        info.add(sub);
        info.add(Theme.gap(14));
        info.add(details);
        info.add(Theme.gap(10));
        info.add(totalLabel);
        info.add(Theme.gap(10));
        info.add(note);

        // "Pay to" text (only GCash has one: the store's GCash number)
        if (!payment.getPayToText().equals("")) {
            JLabel payTo = Theme.makeLabel(payment.getPayToText(), Theme.boldFont(16), Theme.DARK_GREEN);
            Theme.alignLeft(payTo);
            info.add(Theme.gap(6));
            info.add(payTo);
        }

        // QR picture (only the QR payment has one). It goes in the left column, under the watch picture,
        // on a white background because a QR code needs a light background to be scanned.
        if (!payment.getQrImagePath().equals("")) {
            JLabel qr = Theme.makeLabel("", new Font("Segoe UI", Font.ITALIC, 12), Theme.DARK_GOLD);
            qr.setHorizontalAlignment(SwingConstants.CENTER);
            // if the file is missing, the label shows this message instead of a picture
            Theme.showImage(qr, payment.getQrImagePath(), 170, 170, "QR image not found");
            qr.setToolTipText("Add this file: " + payment.getQrImagePath());    // shows when the mouse rests on it

            JPanel qrPanel = new JPanel(new BorderLayout());
            qrPanel.setBackground(Theme.WHITE);
            qrPanel.setBorder(Theme.goldBorder(2, 6));
            qrPanel.setPreferredSize(new Dimension(190, 190));
            qrPanel.add(qr, BorderLayout.CENTER);
            leftColumn.add(qrPanel, BorderLayout.SOUTH);
        }

        // Reference number box (GCash and QR pay outside the cashier, so the customer must type it)
        if (payment.isPaidOnline()) {
            JLabel referenceLabel = Theme.makeLabel(payment.getReferenceFieldLabel());
            referenceField = new JTextField();
            Theme.styleField(referenceField);
            referenceField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            Theme.alignLeft(referenceLabel, referenceField);
            info.add(Theme.gap(8));
            info.add(referenceLabel);
            info.add(Theme.gap(4));
            info.add(referenceField);
        }

        // ---- buttons ----
        JButton back = Theme.greenButton("Back");
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });         // just close, confirmed stays false
        JButton confirm = Theme.goldButton("Confirm Purchase");
        confirm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (payment.isPaidOnline()) {
                    // online payments: the reference number is required
                    String typedReference = referenceField.getText().trim();
                    if (typedReference.isEmpty()) {
                        Theme.showMessage(PurchaseSummaryDialog.this, "Please enter the reference number from your payment receipt.");
                        return;     // stay in the window
                    }
                    // spaces and dashes are allowed while typing (GCash shows the number in groups)
                    String cleaned = typedReference.replaceAll("[\\s-]", "");
                    if (!cleaned.matches("[A-Za-z0-9]{6,30}")) {
                        Theme.showMessage(PurchaseSummaryDialog.this, "The reference number must be 6 to 30 letters or numbers.\nPlease copy it exactly from your payment receipt.");
                        return;
                    }
                    paymentReference = cleaned.toUpperCase();
                }
                confirmed = true;
                dispose();
            }
        });

        // ---- put the three parts together ----
        JPanel root = Theme.makeDialogRoot(18, 14);
        root.add(leftColumn, BorderLayout.WEST);
        root.add(info, BorderLayout.CENTER);
        root.add(Theme.makeRow("right", back, confirm), BorderLayout.SOUTH);
        Theme.finishDialog(this, root, dash);
    }

    //  True only if the customer clicked "Confirm Purchase".
    public boolean isConfirmed() {
        return confirmed;
    }

    //  The reference number typed from the GCash / QR receipt ("" for cash).
    public String getPaymentReference() {
        return paymentReference;
    }

    //  One line of the details: a label on the left and its value on the right.
    private JPanel makeDetailRow(String labelText, String valueText) {
        JLabel label = Theme.makeLabel(labelText);
        label.setPreferredSize(new Dimension(150, 20));
        label.setVerticalAlignment(SwingConstants.TOP);

        // long text is cut into lines that fit inside 200 pixels
        JPanel value = Theme.makeWrappedText(valueText, 200, Theme.BODY_FONT, Theme.DARK_GREEN, "left");

        JPanel row = Theme.makeBorderPanel(8, 0);
        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        return row;
    }
}
