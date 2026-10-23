package jlaluxurywatch;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Transaction
// One order made by a customer: who ordered, which watch, how they pay, and what stage
// (status) the order is in.
//
// OOP IDEAS
//  - ENCAPSULATION: the fields are private. The status only changes through setStatus().
//  - COMPOSITION ("has-a"): a Transaction HAS a Watch and HAS a Payment. It keeps those
//    objects inside instead of copying their details.
//  - POLYMORPHISM: constructor overloading, and toString() is overridden.
public class Transaction {

    // ----- the possible statuses. "static final" = constants (fixed values with a name) -----
    public static final String PENDING = "Pending";                // just ordered: NO status yet, the cashier must mark it
    public static final String RESERVED = "Reserved";              // marked by the cashier: the watch is set aside for the customer
    public static final String PROCESSING = "Processing";          // marked by the cashier: the order is being prepared
    public static final String RECEIVED = "Processed/Received";    // marked by the cashier: the customer got the watch
    public static final String CANCELLED = "Cancelled";            // order was cancelled
    public static final String[] STATUSES = {PENDING, RESERVED, PROCESSING, RECEIVED, CANCELLED};
    //  The statuses the cashier can choose from when marking an order.
    public static final String[] CASHIER_MARKS = {RESERVED, PROCESSING, RECEIVED};

    //  The most watches one order may contain (also limits fake / spam orders).
    public static final int MAX_QUANTITY = 5;

    // nextId is "static": ONE number shared by all Transaction objects (not one per object).
    private static int nextId = 1001;

    // ----- data of one order (private = encapsulation) -----
    private int transactionId;
    private String customerName;
    private Watch watch;
    private Payment payment;
    private String referenceCode;       // code made by the store, like JLA-7K4M2Q
    private String paymentReference;    // reference number from the customer's GCash / QR receipt
    private String status;
    private String cancelReason = "";
    private LocalDateTime dateTime;
    private int quantity = 1;           // how many watches of this model
    private String handledBy = "";      // the staff member who last changed the status (audit trail)

    // ----- constructors (overloading: same name, different parameters) -----

    public Transaction(String customerName, Watch watch, Payment payment) {
        this(customerName, watch, payment, "", "");
    }

    public Transaction(String customerName, Watch watch, Payment payment, String referenceCode) {
        this(customerName, watch, payment, referenceCode, "");
    }

    public Transaction(String customerName, Watch watch, Payment payment,
                       String referenceCode, String paymentReference) {
        this(customerName, watch, payment, referenceCode, paymentReference, 1);
    }

    //  The main constructor: used when a customer places a new order.
    public Transaction(String customerName, Watch watch, Payment payment,
                       String referenceCode, String paymentReference, int quantity) {
        this.quantity = Math.max(1, Math.min(MAX_QUANTITY, quantity));
        this.transactionId = nextId++;           // use the number, then add 1 for the next order
        this.customerName = customerName;
        this.watch = watch;
        this.payment = payment;
        this.referenceCode = referenceCode;
        this.paymentReference = paymentReference;
        this.status = PENDING;                   // a new order is NOT Reserved/Processing/Received: the cashier marks it
        this.dateTime = LocalDateTime.now();     // the date and time right now
    }

    //  Used when an order is loaded back from the saved file (id, date and status already exist).
    public Transaction(int transactionId, LocalDateTime dateTime, String customerName, Watch watch,
                       Payment payment, String referenceCode, String paymentReference, String status) {
        this.transactionId = transactionId;
        this.dateTime = dateTime;
        this.customerName = customerName;
        this.watch = watch;
        this.payment = payment;
        this.referenceCode = referenceCode;
        this.paymentReference = paymentReference;
        this.status = status;
    }

    // ----- getters -----
    public int getTransactionId() { return transactionId; }
    public String getCustomerName() { return customerName; }
    public Watch getWatch() { return watch; }
    public Payment getPayment() { return payment; }
    public String getReferenceCode() { return referenceCode; }
    public String getPaymentReference() { return paymentReference; }
    public String getStatus() { return status; }
    public String getCancelReason() { return cancelReason; }
    public LocalDateTime getDateTime() { return dateTime; }
    public int getQuantity() { return quantity; }
    public String getHandledBy() { return handledBy; }

    //  True while the cashier still has work to do: Pending, Reserved or Processing.
    public boolean isWaiting() {
        return status.equals(PENDING) || status.equals(RESERVED) || status.equals(PROCESSING);
    }

    //  True when nothing more can happen: Received or Cancelled.
    public boolean isFinished() {
        return status.equals(RECEIVED) || status.equals(CANCELLED);
    }

    //  The amount of the whole order: price of one watch x quantity.
    public double getTotal() {
        return watch.getPrice() * quantity;
    }

    // ----- setters (only the things that are allowed to change) -----
    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }

    public void setHandledBy(String handledBy) {
        this.handledBy = handledBy;
    }

    //  The date as text, like 2026-10-16 14:03.
    public String getFormattedDate() {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    // The receipt shown to the customer and to the cashier.
    // Building the text here (not in the screens) means both programs show the same receipt.
    public String getReceiptText() {
        // "\n" means "new line". "+" joins pieces of text together.
        String receipt = "     JLA TIMEPIECE\n" +
                "------------------------------\n" +
                "Receipt No.: " + transactionId + "\n" +
                "Reference:   " + referenceCode + "\n" +
                "Date:        " + getFormattedDate() + "\n" +
                "Customer:    " + customerName + "\n" +
                "Watch:       " + watch.getDisplayName() + "\n" +
                "Quantity:    " + quantity + "\n" +
                "Price each:  " + Money.format(watch.getPrice()) + "\n" +
                "Total:       " + Money.format(getTotal()) + "\n" +
                "Payment:     " + payment.getName() + "\n";

        // POLYMORPHISM: each Payment gives its own label ("GCash RefNo: " or "QR RefNo:    ")
        if (payment.isPaidOnline() && !paymentReference.equals("")) {
            receipt += payment.getReceiptLabel() + paymentReference + "\n";
        }

        receipt += "Status:      " + status + "\n";
        if (!handledBy.equals("")) {
            receipt += "Handled by:  " + handledBy + "\n";
        }
        receipt += "------------------------------\n";

        // a different message for each status
        if (status.equals(PENDING)) {
            receipt += "Show this reference code\n" +
                    "to the cashier. The cashier\n" +
                    "will update your order status.\n";
        } else if (status.equals(RESERVED)) {
            receipt += "Your watch is reserved\n" +
                    "for you.\n";
        } else if (status.equals(PROCESSING)) {
            receipt += "Your order is being\n" +
                    "processed.\n";
        } else if (status.equals(CANCELLED)) {
            receipt += "This order was cancelled.\n";
            if (!cancelReason.equals("")) {
                receipt += "Reason: " + cancelReason + "\n";
            }
        } else {
            receipt += "Order completed.\n";
        }

        receipt += "------------------------------\n" +
                "Thank you for choosing\nJLA TIMEPIECE!\n";
        return receipt;
    }

    //  One-line summary of the order (handy for printing while testing).
    @Override
    public String toString() {
        String text = "#" + transactionId + " | " + getFormattedDate() + " | "
                + customerName + " | " + watch.getDisplayName() + " | "
                + quantity + " x " + Money.format(watch.getPrice()) + " = " + Money.format(getTotal()) + " | " + payment.getName()
                + " | " + status;
        if (!referenceCode.equals("")) {
            text += " | Ref: " + referenceCode;
        }
        if (payment.isPaidOnline() && !paymentReference.equals("")) {
            text += " | " + payment.getReceiptLabel().trim() + " " + paymentReference;
        }
        if (status.equals(CANCELLED) && !cancelReason.equals("")) {
            text += " | Reason: " + cancelReason;
        }
        return text;
    }
}
