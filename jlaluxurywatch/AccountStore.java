package jlaluxurywatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;

// AccountStore
// The staff accounts (admin and cashiers), saved in data/accounts.txt.
//   - login(...) is used by the Cashier and Admin login screens.
//   - addCashier / deleteCashier / resetPassword are used by the Admin.
//
// The first time the program runs, one default admin is created:
//      username: admin      password: admin123
public class AccountStore {

    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private static final String ACCOUNTS_FILE = "data/accounts.txt";

    //  Shortest password allowed for new passwords.
    public static final int MIN_PASSWORD_LENGTH = 6;

    // ----- login protection: after MAX_ATTEMPTS wrong tries the username is locked for LOCK_SECONDS -----
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_SECONDS = 60;
    // "static" = shared by the Admin and Cashier login windows, so switching windows does not reset it
    private static HashMap<String, Integer> failedAttempts = new HashMap<String, Integer>();
    private static HashMap<String, Long> lockedUntil = new HashMap<String, Long>();

    private ArrayList<Account> accounts = new ArrayList<Account>();

    public AccountStore() {
        load();
        if (findByUsername(DEFAULT_ADMIN_USERNAME) == null) {
            accounts.add(Account.create(DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD,
                    Account.ADMIN, "Administrator"));
            save();
        }
    }

    //  Checks username + password + role. Returns the Account, or null if anything is wrong.
    public Account login(String username, String password, String role) {
        String key = username.trim().toLowerCase();
        if (getLockSecondsLeft(username) > 0) {
            return null;        // locked: do not even check the password
        }
        load();     // read the file again: the admin may have just created this cashier
        Account account = findByUsername(username);
        if (account != null && account.getRole().equals(role) && account.checkPassword(password)) {
            failedAttempts.remove(key);
            lockedUntil.remove(key);
            return account;
        }
        // wrong: count it (also for usernames that do not exist, so nobody can find out which exist)
        int count = failedAttempts.containsKey(key) ? failedAttempts.get(key) + 1 : 1;
        if (count >= MAX_ATTEMPTS) {
            lockedUntil.put(key, System.currentTimeMillis() + LOCK_SECONDS * 1000L);
            failedAttempts.remove(key);
        } else {
            failedAttempts.put(key, count);
        }
        return null;
    }

    //  How many seconds this username is still locked (0 = not locked).
    public int getLockSecondsLeft(String username) {
        Long until = lockedUntil.get(username.trim().toLowerCase());
        if (until == null) {
            return 0;
        }
        long msLeft = until - System.currentTimeMillis();
        if (msLeft <= 0) {
            lockedUntil.remove(username.trim().toLowerCase());
            return 0;
        }
        return (int) Math.ceil(msLeft / 1000.0);
    }

    //  How many wrong tries are left before the username is locked.
    public int getAttemptsLeft(String username) {
        String key = username.trim().toLowerCase();
        return MAX_ATTEMPTS - (failedAttempts.containsKey(key) ? failedAttempts.get(key) : 0);
    }

    //  True if this account is the built-in admin and still uses the published default password.
    public boolean usesDefaultPassword(Account account) {
        return account.getUsername().equalsIgnoreCase(DEFAULT_ADMIN_USERNAME)
                && account.checkPassword(DEFAULT_ADMIN_PASSWORD);
    }

    //  An account changes its OWN password (the old one must be typed). Returns an error message, or null if it worked.
    public String changePassword(Account account, String oldPassword, String newPassword) {
        load();
        Account saved = findByUsername(account.getUsername());
        if (saved == null || !saved.checkPassword(oldPassword)) {
            return "The current password is wrong.";
        }
        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            return "The new password needs at least " + MIN_PASSWORD_LENGTH + " characters.";
        }
        if (newPassword.equals(oldPassword) || newPassword.equals(DEFAULT_ADMIN_PASSWORD)) {
            return "Please choose a different password.";
        }
        saved.setPassword(newPassword);
        account.setPassword(newPassword);       // keep the logged-in Account object in step with the file
        if (!save()) {
            return "The new password could not be saved.";
        }
        return null;
    }

    //  All the cashier accounts.
    public ArrayList<Account> getCashiers() {
        load();
        ArrayList<Account> result = new ArrayList<Account>();
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getRole().equals(Account.CASHIER)) {
                result.add(accounts.get(i));
            }
        }
        return result;
    }

    //  Creates a cashier account. Returns an error message, or null if it worked.
    public String addCashier(String fullName, String username, String password) {
        load();
        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            return "Please fill in the full name, username and password.";
        }
        if (username.length() < 3 || username.matches(".*\\s.*")) {
            return "The username needs at least 3 characters and no spaces.";
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "The password needs at least " + MIN_PASSWORD_LENGTH + " characters.";
        }
        if (findByUsername(username) != null) {
            return "That username is already taken.";
        }
        Account created = Account.create(username, password, Account.CASHIER, clean(fullName));
        accounts.add(created);
        if (!save()) {
            accounts.remove(created);
            return "The account could not be saved.";
        }
        return null;
    }

    //  Deletes a cashier account (an admin account can never be deleted here).
    public boolean deleteCashier(Account account) {
        load();
        Account saved = findByUsername(account.getUsername());
        if (saved != null && saved.getRole().equals(Account.CASHIER)) {
            accounts.remove(saved);
            return save();
        }
        return true;
    }

    //  Gives a cashier a new password. Returns an error message, or null if it worked.
    public String resetPassword(Account account, String newPassword) {
        load();
        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            return "The password needs at least " + MIN_PASSWORD_LENGTH + " characters.";
        }
        Account saved = findByUsername(account.getUsername());
        if (saved == null) {
            return "That account no longer exists.";
        }
        saved.setPassword(newPassword);
        if (!save()) {
            return "The new password could not be saved.";
        }
        return null;
    }

    private Account findByUsername(String username) {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getUsername().equalsIgnoreCase(username.trim())) {
                return accounts.get(i);
            }
        }
        return null;
    }

    // ---------------------------- file ----------------------------

    private void load() {
        File file = new File(ACCOUNTS_FILE);
        if (!file.exists()) {
            return;
        }
        accounts.clear();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                String[] f = line.split("\t", -1);       // username, hash, role, full name
                if (f.length >= 4) {
                    accounts.add(new Account(f[0], f[1], f[2], f[3]));
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Could not read " + ACCOUNTS_FILE);
        }
    }

    //  Removes tabs and line breaks (they would break the one-line-per-account file).
    private static String clean(String text) {
        return text.replace("\t", " ").replace("\r", " ").replace("\n", " ");
    }

    // Writes all accounts. They go to a temporary file first and only replace accounts.txt when
    // everything was written, so a crash can never leave a half-empty accounts file.
    // Returns false if saving failed.
    private boolean save() {
        try {
            File dataFolder = new File("data");
            if (!dataFolder.exists()) {
                dataFolder.mkdir();
            }
            File tempFile = new File(ACCOUNTS_FILE + ".tmp");
            PrintWriter writer = new PrintWriter(new FileWriter(tempFile));
            for (int i = 0; i < accounts.size(); i++) {
                Account a = accounts.get(i);
                writer.println(a.getUsername() + "\t" + a.getPasswordHash() + "\t" + a.getRole() + "\t" + clean(a.getFullName()));
            }
            boolean writeFailed = writer.checkError();
            writer.close();
            if (writeFailed) {
                return false;
            }
            try {
                Files.move(tempFile.toPath(), Paths.get(ACCOUNTS_FILE),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(tempFile.toPath(), Paths.get(ACCOUNTS_FILE), StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            System.out.println("Could not save " + ACCOUNTS_FILE);
            return false;
        }
    }
}
