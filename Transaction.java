package jlaluxurywatch;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {

    public static final String PENDING = "Pending";
    public static final String RESERVED = "Reserved";
    public static final String PROCESSING = "Processing";
    public static final String RECEIVED = "Processed/Received";
    public static final String CANCELLED = "Cancelled";
    public static final String[] STATUSES = {PENDING, RESERVED, PROCESSING, RECEIVED, CANCELLED};
    public static final String[] CASHIER_MARKS = {RESERVED, PROCESSING, RECEIVED};

    public static final int MAX_QUANTITY = 5;

    private static int nextId = 1001;

    private int transactionId;
    private String customerName;
    private Watch watch;
    private Payment payment;
    private String referenceCode;
    private String paymentReference;
    private String status;
    private String cancelReason = "";
    private LocalDateTime dateTime;
    private int quantity = 1;
    private String handledBy = "";

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

    public Transaction(String customerName, Watch watch, Payment payment,
                       String referenceCode, String paymentReference, int quantity) {
        this.quantity = Math.max(1, Math.min(MAX_QUANTITY, quantity));
        this.transactionId = nextId++;
        this.customerName = customerName;
        this.watch = watch;
        this.payment = payment;
        this.referenceCode = referenceCode;
        this.paymentReference = paymentReference;
        this.status = PENDING;
        this.dateTime = LocalDateTime.now();
    }

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

    public boolean isWaiting() {
        return status.equals(PENDING) || status.equals(RESERVED) || status.equals(PROCESSING);
    }

    public boolean isFinished() {
        return status.equals(RECEIVED) || status.equals(CANCELLED);
    }

    public double getTotal() {
        return watch.getPrice() * quantity;
    }

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

    public String getFormattedDate() {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getReceiptText() {
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

        if (payment.isPaidOnline() && !paymentReference.equals("")) {
            receipt += payment.getReceiptLabel() + paymentReference + "\n";
        }

        receipt += "Status:      " + status + "\n";
        if (!handledBy.equals("")) {
            receipt += "Handled by:  " + handledBy + "\n";
        }
        receipt += "------------------------------\n";

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
