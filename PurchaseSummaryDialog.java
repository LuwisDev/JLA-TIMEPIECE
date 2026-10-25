package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class PurchaseSummaryDialog extends JDialog {
    private boolean confirmed = false;
    private JTextField referenceField;
    private String paymentReference = "";

    public PurchaseSummaryDialog(JLADash dash, Watch watch, String customer, Payment payment, int quantity) {
        super(dash, "Purchase Details", true);

        JPanel leftColumn = Theme.makeBorderPanel(0, 10);
        leftColumn.add(Theme.makeImageBox(watch.getImagePath(), 190, 230, 170, 200), BorderLayout.CENTER);

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

        String instruction = payment.getCustomerInstruction()
                + " After you confirm, your order receipt will show a reference code. "
                + "Show that code to the cashier so your payment can be matched to this purchase.";
        JTextArea note = Theme.makeReadOnlyText(instruction, 6, 28);

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

        if (!payment.getPayToText().equals("")) {
            JLabel payTo = Theme.makeLabel(payment.getPayToText(), Theme.boldFont(16), Theme.DARK_GREEN);
            Theme.alignLeft(payTo);
            info.add(Theme.gap(6));
            info.add(payTo);
        }

        if (!payment.getQrImagePath().equals("")) {
            JLabel qr = Theme.makeLabel("", new Font("Segoe UI", Font.ITALIC, 12), Theme.DARK_GOLD);
            qr.setHorizontalAlignment(SwingConstants.CENTER);
            Theme.showImage(qr, payment.getQrImagePath(), 170, 170, "QR image not found");
            qr.setToolTipText("Add this file: " + payment.getQrImagePath());

            JPanel qrPanel = new JPanel(new BorderLayout());
            qrPanel.setBackground(Theme.WHITE);
            qrPanel.setBorder(Theme.goldBorder(2, 6));
            qrPanel.setPreferredSize(new Dimension(190, 190));
            qrPanel.add(qr, BorderLayout.CENTER);
            leftColumn.add(qrPanel, BorderLayout.SOUTH);
        }

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

        JButton back = Theme.greenButton("Back");
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JButton confirm = Theme.goldButton("Confirm Purchase");
        confirm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (payment.isPaidOnline()) {
                    String typedReference = referenceField.getText().trim();
                    if (typedReference.isEmpty()) {
                        Theme.showMessage(PurchaseSummaryDialog.this, "Please enter the reference number from your payment receipt.");
                        return;
                    }
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

        JPanel root = Theme.makeDialogRoot(18, 14);
        root.add(leftColumn, BorderLayout.WEST);
        root.add(info, BorderLayout.CENTER);
        root.add(Theme.makeRow("right", back, confirm), BorderLayout.SOUTH);
        Theme.finishDialog(this, root, dash);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    private JPanel makeDetailRow(String labelText, String valueText) {
        JLabel label = Theme.makeLabel(labelText);
        label.setPreferredSize(new Dimension(150, 20));
        label.setVerticalAlignment(SwingConstants.TOP);

        JPanel value = Theme.makeWrappedText(valueText, 200, Theme.BODY_FONT, Theme.DARK_GREEN, "left");

        JPanel row = Theme.makeBorderPanel(8, 0);
        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        return row;
    }
}
