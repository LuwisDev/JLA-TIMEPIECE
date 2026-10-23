package jlaluxurywatch;

// CashPayment
// A child class of Payment for paying in cash at the cashier.
// Nothing is paid online, so there is no reference number.
public class CashPayment extends Payment {

    @Override
    public String getName() {
        return "Cash";
    }

    @Override
    public boolean isPaidOnline() {
        return false;
    }

    // Cash has no reference number, so these two are never shown.
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
