package jlaluxurywatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.function.BooleanSupplier;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

// OrderStore
// The list of orders (transactions), saved to data/orders.txt so that the Customer, Cashier
// and Admin programs can share them.
//
// Screens never touch the list directly. They call the methods below (addTransaction,
// updateStatus, cancelOrder, findByReferenceCode...). That is ABSTRACTION.
// It is also ENCAPSULATION: getTransactions() gives out a COPY of the list.
public class OrderStore {

    private ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private WatchStore watchStore;      // used to find the watch of a saved order

    // One order per line. All programs read and write this file.
    private static final String ORDERS_FILE = "data/orders.txt";
    private static final String LOCK_FILE = "data/orders.lock";

    private String lastError = "";      // why the last save failed (shown to the user)

    public OrderStore(WatchStore watchStore) {
        this.watchStore = watchStore;
        refresh();      // read the orders that are already saved in the file
    }

    // ---------------------------------------------------------------------------
    //                               ORDERS
    // ---------------------------------------------------------------------------

    //  A copy of the list of all orders (oldest first).
    public ArrayList<Transaction> getTransactions() {
        return new ArrayList<Transaction>(transactions);
    }

    //  Why the last save failed (empty if nothing failed). Screens show this to the user.
    public String getLastError() {
        return lastError;
    }

    // Adds a new order, gives it the next order number and saves it to the file.
    // Returns false if it could NOT be saved (then the order is not kept in memory either).
    public boolean addTransaction(Transaction transaction) {
        return runLocked(new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                refresh();   // first read the file, in case another program added orders meanwhile

                // the new order number = the biggest existing number + 1 (the first order is 1001)
                int newTransactionId = 1001;
                for (int i = 0; i < transactions.size(); i++) {
                    int existingId = transactions.get(i).getTransactionId();
                    if (existingId >= newTransactionId) {
                        newTransactionId = existingId + 1;
                    }
                }
                transaction.setTransactionId(newTransactionId);
                transactions.add(transaction);
                if (!saveTransactions()) {
                    transactions.remove(transaction);
                    return false;
                }
                return true;
            }
        });
    }

    // Changes the status of an order and saves it, so the other programs can see it.
    // staffName = who made the change (saved in the file as an audit trail).
    // Returns false if it could not be saved (the old status is put back).
    public boolean updateStatus(Transaction transaction, String newStatus, String staffName) {
        return runLocked(new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                refresh();
                String oldStatus = transaction.getStatus();
                String oldHandledBy = transaction.getHandledBy();
                transaction.setStatus(newStatus);
                transaction.setHandledBy(clean(staffName));
                if (!saveTransactions()) {
                    transaction.setStatus(oldStatus);
                    transaction.setHandledBy(oldHandledBy);
                    return false;
                }
                return true;
            }
        });
    }

    //  Result of cancelOrder: it worked, it is too late to cancel, or the file could not be saved.
    public static final int CANCEL_OK = 0;
    public static final int CANCEL_TOO_LATE = 1;
    public static final int CANCEL_SAVE_FAILED = 2;

    //  Cancels an order that is not finished yet. Returns CANCEL_OK, CANCEL_TOO_LATE or CANCEL_SAVE_FAILED.
    public int cancelOrder(Transaction transaction, String reason, String staffName) {
        final int[] result = {CANCEL_SAVE_FAILED};
        runLocked(new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                refresh();
                // an order that is already received (or already cancelled) cannot be cancelled
                if (transaction.isFinished()) {
                    result[0] = CANCEL_TOO_LATE;
                    return true;
                }
                String oldStatus = transaction.getStatus();
                String oldReason = transaction.getCancelReason();
                String oldHandledBy = transaction.getHandledBy();
                transaction.setStatus(Transaction.CANCELLED);
                transaction.setCancelReason(clean(reason));
                transaction.setHandledBy(clean(staffName));
                if (!saveTransactions()) {
                    transaction.setStatus(oldStatus);
                    transaction.setCancelReason(oldReason);
                    transaction.setHandledBy(oldHandledBy);
                    return false;
                }
                result[0] = CANCEL_OK;
                return true;
            }
        });
        return result[0];
    }

    //  Orders of this customer that are still waiting (Pending / Reserved / Processing). Used to stop spam orders.
    public int countWaitingByCustomer(String customerName) {
        refresh();
        int count = 0;
        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            if (t.isWaiting() && t.getCustomerName().equalsIgnoreCase(customerName.trim())) {
                count++;
            }
        }
        return count;
    }

    // Runs one "read - change - save" action while holding a lock on data/orders.lock, so two
    // programs (customer + cashier) can never overwrite each other's changes at the same moment.
    private boolean runLocked(BooleanSupplier action) {
        synchronized (OrderStore.class) {
            try {
                File dataFolder = new File("data");
                if (!dataFolder.exists()) {
                    dataFolder.mkdir();
                }
                FileChannel channel = FileChannel.open(Paths.get(LOCK_FILE),
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                try {
                    FileLock lock = channel.lock();     // waits if another program holds the lock
                    try {
                        return action.getAsBoolean();
                    } finally {
                        lock.release();
                    }
                } finally {
                    channel.close();
                }
            } catch (IOException | RuntimeException e) {
                lastError = "Could not lock the orders file: " + e.getMessage();
                return false;
            }
        }
    }

    //  Removes tabs and line breaks, because they would break the one-line-per-order file.
    private static String clean(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\t", " ").replace("\r", " ").replace("\n", " ");
    }

    //  Makes a random reference code like JLA-7K4M2Q that no other transaction has.
    public String generateReferenceCode() {
        refresh();
        String allowedLetters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0/O or 1/I, they look alike
        Random random = new Random();
        String code = "";
        boolean isUnique = false;
        while (!isUnique) {                                  // keep trying until the code is new
            code = "JLA-";
            for (int i = 0; i < 6; i++) {
                code += allowedLetters.charAt(random.nextInt(allowedLetters.length()));
            }
            isUnique = true;
            for (int i = 0; i < transactions.size(); i++) {
                if (transactions.get(i).getReferenceCode().equals(code)) {
                    isUnique = false;
                }
            }
        }
        return code;
    }

    //  True if this GCash / QR reference number is already used by an order that is NOT cancelled.
    public boolean isPaymentRefUsed(String paymentReference) {
        refresh();
        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            if (!t.getStatus().equals(Transaction.CANCELLED)
                    && t.getPaymentReference().equalsIgnoreCase(paymentReference)) {
                return true;
            }
        }
        return false;
    }

    //  Finds the order with this reference code (like JLA-7K4M2Q). Returns null if there is none.
    public Transaction findByReferenceCode(String code) {
        refresh();
        String wantedCode = code.trim();
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getReferenceCode().equalsIgnoreCase(wantedCode)) {
                return transactions.get(i);
            }
        }
        // the customer may read out only "7K4M2Q": try again with the "JLA-" start added
        if (!wantedCode.isEmpty() && !wantedCode.contains("-")) {
            return findByReferenceCode("JLA-" + wantedCode);
        }
        return null;   // null = "nothing found"
    }

    //  How many orders have this status.
    public int countByStatus(String status) {
        int count = 0;
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getStatus().equals(status)) {
                count++;
            }
        }
        return count;
    }

    //  Total of all orders that are not cancelled.
    public double getTotalSales() {
        double total = 0;
        for (int i = 0; i < transactions.size(); i++) {
            if (!transactions.get(i).getStatus().equals(Transaction.CANCELLED)) {
                total += transactions.get(i).getTotal();
            }
        }
        return total;
    }

    //  Total of the orders that are Processed/Received (money really received).
    public double getReceivedSales() {
        double total = 0;
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getStatus().equals(Transaction.RECEIVED)) {
                total += transactions.get(i).getTotal();
            }
        }
        return total;
    }

    // ---------------------------------------------------------------------------
    //                    SAVING AND LOADING THE ORDERS FILE
    // ---------------------------------------------------------------------------

    // Reads the orders file. Orders already in memory are UPDATED (so screens that hold them
    // see the new status); orders that are not in memory yet are ADDED.
    public void refresh() {
        File file = new File(ORDERS_FILE);
        if (!file.exists()) {
            return;     // nothing saved yet
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                loadLine(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Could not read " + ORDERS_FILE);
        }
    }

    // Turns ONE line of the file into an order. A line looks like:
    // id, date, customer, watch, payment, referenceCode, paymentRef, status, cancelReason, price
    // (+ quantity, handledBy) separated by TAB. The last four columns are optional so old files still load.
    private void loadLine(String line) {
        String[] fields = line.split("\t", -1);
        if (fields.length < 8) {
            return;     // not a complete line, ignore it
        }
        String cancelReason = "";
        if (fields.length > 8) {
            cancelReason = fields[8];
        }
        try {
            int id = Integer.parseInt(fields[0]);
            Transaction existingTransaction = findById(id);
            if (existingTransaction != null) {
                // we already have this order in memory: just update what can change
                existingTransaction.setStatus(fields[7]);
                existingTransaction.setCancelReason(cancelReason);
                existingTransaction.setHandledBy(fields.length > 11 ? fields[11] : "");
            } else {
                Payment payment = Payment.fromName(fields[4]);
                if (payment != null) {
                    Watch watch = makeWatchCopy(fields[3], fields.length > 9 ? fields[9] : "");
                    Transaction loadedTransaction = new Transaction(id, LocalDateTime.parse(fields[1]),
                            fields[2], watch, payment, fields[5], fields[6], fields[7]);
                    loadedTransaction.setCancelReason(cancelReason);
                    if (fields.length > 10 && !fields[10].equals("")) {
                        loadedTransaction.setQuantity(Integer.parseInt(fields[10]));    // old files have no quantity = 1
                    }
                    loadedTransaction.setHandledBy(fields.length > 11 ? fields[11] : "");
                    transactions.add(loadedTransaction);
                }
            }
        } catch (RuntimeException e) {
            System.out.println("Skipped a bad line in " + ORDERS_FILE);
        }
    }

    // An order keeps its OWN copy of the watch, with the price it had when it was bought.
    // So if the admin later changes the price, renames or deletes the watch, the old
    // transactions do not change and are never lost.
    private Watch makeWatchCopy(String watchName, String savedPrice) {
        Watch current = watchStore.findByName(watchName);
        double price = 0;
        if (!savedPrice.equals("")) {
            price = Double.parseDouble(savedPrice);      // the price at the time of purchase
        } else if (current != null) {
            price = current.getPrice();                  // old file without a saved price
        }
        if (current != null) {
            return new Watch(current.getBrand(), current.getModel(), price, current.getImagePath(),
                    current.getCaseSize(), current.getMovement(), current.getWaterResistance(),
                    current.getStrap(), current.getDial(), current.getDescription());
        }
        return new Watch(watchName, "", price);          // the watch was deleted: keep only its name
    }

    // Writes ALL orders to the file (one line each).
    // The lines are first written to a temporary file, and only when that worked completely the
    // temporary file replaces orders.txt. So a crash in the middle can never destroy the orders.
    private boolean saveTransactions() {
        try {
            File dataFolder = new File("data");
            if (!dataFolder.exists()) {
                dataFolder.mkdir();
            }
            File tempFile = new File(ORDERS_FILE + ".tmp");
            PrintWriter writer = new PrintWriter(new FileWriter(tempFile));
            for (int i = 0; i < transactions.size(); i++) {
                Transaction transaction = transactions.get(i);
                writer.println(transaction.getTransactionId() + "\t"
                        + transaction.getDateTime().toString() + "\t"
                        + clean(transaction.getCustomerName()) + "\t"
                        + clean(transaction.getWatch().getDisplayName()) + "\t"
                        + transaction.getPayment().getName() + "\t"
                        + clean(transaction.getReferenceCode()) + "\t"
                        + clean(transaction.getPaymentReference()) + "\t"
                        + transaction.getStatus() + "\t"
                        + clean(transaction.getCancelReason()) + "\t"
                        + transaction.getWatch().getPrice() + "\t"
                        + transaction.getQuantity() + "\t"
                        + clean(transaction.getHandledBy()));
            }
            boolean writeFailed = writer.checkError();      // PrintWriter hides errors: ask it
            writer.close();
            if (writeFailed) {
                lastError = "The disk refused the data (is it full?).";
                return false;
            }
            try {
                Files.move(tempFile.toPath(), Paths.get(ORDERS_FILE),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(tempFile.toPath(), Paths.get(ORDERS_FILE), StandardCopyOption.REPLACE_EXISTING);
            }
            lastError = "";
            return true;
        } catch (IOException e) {
            lastError = "Could not save " + ORDERS_FILE + ": " + e.getMessage();
            System.out.println(lastError);
            return false;
        }
    }

    //  Finds an order in memory by its order number. Returns null if not found.
    private Transaction findById(int id) {
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getTransactionId() == id) {
                return transactions.get(i);
            }
        }
        return null;
    }
}
