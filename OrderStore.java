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

public class OrderStore {

    private ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private WatchStore watchStore;

    private static final String ORDERS_FILE = "data/orders.txt";
    private static final String LOCK_FILE = "data/orders.lock";

    private String lastError = "";

    public OrderStore(WatchStore watchStore) {
        this.watchStore = watchStore;
        refresh();
    }

    public ArrayList<Transaction> getTransactions() {
        return new ArrayList<Transaction>(transactions);
    }

    public String getLastError() {
        return lastError;
    }

    public boolean addTransaction(Transaction transaction) {
        return runLocked(new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                refresh();

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

    public static final int CANCEL_OK = 0;
    public static final int CANCEL_TOO_LATE = 1;
    public static final int CANCEL_SAVE_FAILED = 2;

    public int cancelOrder(Transaction transaction, String reason, String staffName) {
        final int[] result = {CANCEL_SAVE_FAILED};
        runLocked(new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                refresh();
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
                    FileLock lock = channel.lock();
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

    private static String clean(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\t", " ").replace("\r", " ").replace("\n", " ");
    }

    public String generateReferenceCode() {
        refresh();
        String allowedLetters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        String code = "";
        boolean isUnique = false;
        while (!isUnique) {
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

    public Transaction findByReferenceCode(String code) {
        refresh();
        String wantedCode = code.trim();
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getReferenceCode().equalsIgnoreCase(wantedCode)) {
                return transactions.get(i);
            }
        }
        if (!wantedCode.isEmpty() && !wantedCode.contains("-")) {
            return findByReferenceCode("JLA-" + wantedCode);
        }
        return null;
    }

    public int countByStatus(String status) {
        int count = 0;
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getStatus().equals(status)) {
                count++;
            }
        }
        return count;
    }

    public double getTotalSales() {
        double total = 0;
        for (int i = 0; i < transactions.size(); i++) {
            if (!transactions.get(i).getStatus().equals(Transaction.CANCELLED)) {
                total += transactions.get(i).getTotal();
            }
        }
        return total;
    }

    public double getReceivedSales() {
        double total = 0;
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getStatus().equals(Transaction.RECEIVED)) {
                total += transactions.get(i).getTotal();
            }
        }
        return total;
    }

    public void refresh() {
        File file = new File(ORDERS_FILE);
        if (!file.exists()) {
            return;
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

    private void loadLine(String line) {
        String[] fields = line.split("\t", -1);
        if (fields.length < 8) {
            return;
        }
        String cancelReason = "";
        if (fields.length > 8) {
            cancelReason = fields[8];
        }
        try {
            int id = Integer.parseInt(fields[0]);
            Transaction existingTransaction = findById(id);
            if (existingTransaction != null) {
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
                        loadedTransaction.setQuantity(Integer.parseInt(fields[10]));
                    }
                    loadedTransaction.setHandledBy(fields.length > 11 ? fields[11] : "");
                    transactions.add(loadedTransaction);
                }
            }
        } catch (RuntimeException e) {
            System.out.println("Skipped a bad line in " + ORDERS_FILE);
        }
    }

    private Watch makeWatchCopy(String watchName, String savedPrice) {
        Watch current = watchStore.findByName(watchName);
        double price = 0;
        if (!savedPrice.equals("")) {
            price = Double.parseDouble(savedPrice);
        } else if (current != null) {
            price = current.getPrice();
        }
        if (current != null) {
            return new Watch(current.getBrand(), current.getModel(), price, current.getImagePath(),
                    current.getCaseSize(), current.getMovement(), current.getWaterResistance(),
                    current.getStrap(), current.getDial(), current.getDescription());
        }
        return new Watch(watchName, "", price);
    }

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
            boolean writeFailed = writer.checkError();
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

    private Transaction findById(int id) {
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getTransactionId() == id) {
                return transactions.get(i);
            }
        }
        return null;
    }
}
