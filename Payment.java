package jlaluxurywatch;

public abstract class Payment {

    public abstract String getName();

    public abstract boolean isPaidOnline();

    public abstract String getReferenceFieldLabel();

    public abstract String getReceiptLabel();

    public abstract String getCustomerInstruction();

    public abstract String getCashierHint(String formattedAmount, String customerReference);

    public String getPayToText() {
        return "";
    }

    public String getQrImagePath() {
        return "";
    }

    @Override
    public String toString() {
        return getName();
    }

    public static Payment[] getAllPayments() {
        return new Payment[]{new GCashPayment(), new QrCodePayment(), new CashPayment()};
    }

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
