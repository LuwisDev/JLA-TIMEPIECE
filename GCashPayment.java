package jlaluxurywatch;

public class GCashPayment extends Payment {

    private static final String STORE_GCASH_NUMBER = "09684338974";

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

    @Override
    public String getPayToText() {
        return "GCash number: " + STORE_GCASH_NUMBER;
    }
}
