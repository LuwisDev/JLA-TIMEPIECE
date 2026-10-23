package jlaluxurywatch;

// Payment  (ABSTRACT CLASS)
// This is the "idea" of a payment method. It is not GCash, QR or Cash yet - those are the
// three child classes (GCashPayment, QrCodePayment, CashPayment) that "extend" this class.
//
// OOP IDEAS USED HERE
//  - INHERITANCE:    GCashPayment, QrCodePayment and CashPayment all extend Payment.
//  - ABSTRACTION:    the "abstract" methods below only say WHAT every payment must be able
//                    to do. Each child class decides HOW.
//  - POLYMORPHISM:   the rest of the program only talks to a "Payment". When it calls
//                    payment.getCustomerInstruction(), Java automatically runs the version
//                    of the child class (GCash, QR or Cash). That is why we no longer need
//                    long "if (payment.equals("GCash")) ... else if ..." chains.
//
// You cannot write "new Payment()" because the class is abstract (it is only an idea).
public abstract class Payment {

    // ---------- abstract methods: every child class MUST write its own version ----------

    //  The name shown on screen and saved in the file, like "GCash".
    public abstract String getName();

    //  True when the customer pays outside the cashier (GCash / QR) and must type a reference number.
    public abstract boolean isPaidOnline();

    //  The label above the reference number box, like "GCash reference no.:".
    public abstract String getReferenceFieldLabel();

    //  The label used on the receipt (13 characters so the receipt lines stay aligned).
    public abstract String getReceiptLabel();

    //  What the customer must do to pay (shown in the Purchase Details window).
    public abstract String getCustomerInstruction();

    //  What the cashier must check before receiving the order.
    public abstract String getCashierHint(String formattedAmount, String customerReference);

    // ---------- normal methods with a default answer: a child class may OVERRIDE them ----------

    //  Extra "pay here" text, for example the store's GCash number. Empty means nothing to show.
    public String getPayToText() {
        return "";
    }

    //  Picture file of a QR code to show. Empty means no picture.
    public String getQrImagePath() {
        return "";
    }

    // toString() decides how an object looks as text. The Payment drop-down list (JComboBox)
    // calls it to know what to write for each choice, so it shows "GCash", "QR Code", "Cash".
    @Override
    public String toString() {
        return getName();
    }

    // ---------- static helpers: called with Payment.something(...) ----------

    //  All the payment methods the store accepts, in the order they appear in the drop-down list.
    public static Payment[] getAllPayments() {
        return new Payment[]{new GCashPayment(), new QrCodePayment(), new CashPayment()};
    }

    // Turns saved text (like "GCash") back into a Payment object.
    // Used when the orders file is loaded. Returns null if the name is not known.
    public static Payment fromName(String name) {
        Payment[] allPayments = getAllPayments();
        for (int i = 0; i < allPayments.length; i++) {
            if (allPayments[i].getName().equals(name)) {
                return allPayments[i];
            }
        }
        return null;
    }
}
