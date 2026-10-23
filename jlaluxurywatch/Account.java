package jlaluxurywatch;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

// Account
// A login account of the store staff: either an ADMIN or a CASHIER.
// (Customers do not need an account.)
//
// OOP: ENCAPSULATION - the password is never kept as plain text. Only a SHA-256 "hash"
// (a scrambled version that cannot be turned back) is stored, and checkPassword() compares hashes.
public class Account {
    public static final String ADMIN = "Admin";
    public static final String CASHIER = "Cashier";

    private String username;
    private String passwordHash;
    private String role;
    private String fullName;

    public Account(String username, String passwordHash, String role, String fullName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
    }

    //  Creates an account from a plain password (the password is hashed here).
    public static Account create(String username, String plainPassword, String role, String fullName) {
        return new Account(username, hash(plainPassword), role, fullName);
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }

    public void setPassword(String plainPassword) {
        this.passwordHash = hash(plainPassword);
    }

    //  True if the typed password is the right one.
    public boolean checkPassword(String plainPassword) {
        return passwordHash.equals(hash(plainPassword));
    }

    //  Turns a password into a SHA-256 hash written in hexadecimal letters.
    private static String hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                hex.append(String.format("%02x", bytes[i]));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);   // SHA-256 always exists in Java
        }
    }
}
