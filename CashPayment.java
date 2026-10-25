package jlaluxurywatch;

public class CashPayment extends Payment {

    @Override
    public String getName() {
        return "Cash";
    }

    @Override
    public boolean isPaidOnline() {
        return false;
    }

    @Override
    public String getReferenceFieldLabel() {
        return "";
    }

    @Override
    public String getReceiptLabel() {
        return "";
    }

    @Override
    public String getCustomerInstruction() {
        return "Pay in cash at the cashier.";
    }

    @Override
    public String getCashierHint(String formattedAmount, String customerReference) {
        return "Collect " + formattedAmount + " in cash from the customer.";
    }
}
