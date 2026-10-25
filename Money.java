package jlaluxurywatch;

public class Money {

    public static String format(double amount) {
        return "₱" + String.format("%,.2f", amount);
    }
}
