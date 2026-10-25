package jlaluxurywatch;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

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

    public boolean checkPassword(String plainPassword) {
        return passwordHash.equals(hash(plainPassword));
    }

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
            throw new RuntimeException(e);
        }
    }
}
