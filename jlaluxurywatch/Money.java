package jlaluxurywatch;

// Money
// A tiny helper class. It turns a number like 850000 into the text "₱850,000.00".
//
// Why a separate class? Before, the same line
//     "₱" + String.format("%,.2f", price)
// was copied in many screens. Now every screen calls Money.format(price), so if we
// ever want to change how prices look, we change it in ONE place.
//
// "static" means you do NOT need to create an object (no "new Money()").
// You just write Money.format(...).
public class Money {

    //  Turns an amount into text, for example 1500000 becomes "₱1,500,000.00".
    public static String format(double amount) {
        // %,.2f means: put commas between thousands (,) and always show 2 decimals (.2f)
        return "₱" + String.format("%,.2f", amount);
    }
}
