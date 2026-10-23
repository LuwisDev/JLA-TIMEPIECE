package jlaluxurywatch;

// GCashPayment
// A child class of Payment ("extends Payment" = inheritance).
// It fills in every abstract method with what is true for GCash.
public class GCashPayment extends Payment {

    // The store's GCash number. "static final" = one shared value that never changes.
    private static final String STORE_GCASH_NUMBER = "09684338974";

    // @Override tells Java "I am replacing a method that the parent class declared".
    @Override
    public String getName() {
        return "GCash";
    }

    @Override
    public boolean isPaidOnline() {
        return true;
    }

    @Override
    public String getReferenceFieldLabel() {
        return "GCash reference no.:";
    }

    @Override
    public String getReceiptLabel() {
        return "GCash RefNo: ";
    }

    @Override
    public String getCustomerInstruction() {
        return "Send the exact amount to the GCash number below, then type the reference number from your GCash receipt.";
    }

    @Override
    public String getCashierHint(String formattedAmount, String customerReference) {
        return "Check the GCash payment of " + formattedAmount + ". Customer's reference no.: " + customerReference;
    }

    // This one is optional in the parent class. Only GCash overrides it, to show the number.
    @Override
    public String getPayToText() {
        return "GCash number: " + STORE_GCASH_NUMBER;
    }
}
