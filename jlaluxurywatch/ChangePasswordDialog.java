package jlaluxurywatch;

import javax.swing.*;
import java.awt.Component;

// ChangePasswordDialog
// Lets the logged-in admin change his or her OWN password (the old password must be typed first).
// Call: ChangePasswordDialog.show(parent, accountStore, account);
public class ChangePasswordDialog {

    //  Keeps asking until the password was changed or the admin clicks Cancel. Returns true if it was changed.
    public static boolean show(Component parent, AccountStore accounts, Account account) {
        String message = " ";
        while (true) {
            JPasswordField oldField = new JPasswordField();
            JPasswordField newField = new JPasswordField();
            JPasswordField confirmField = new JPasswordField();
            int answer = JOptionPane.showConfirmDialog(parent, new Object[]{
                    message,
                    "Current password:", oldField,
                    "New password (at least " + AccountStore.MIN_PASSWORD_LENGTH + " characters):", newField,
                    "Type the new password again:", confirmField},
                    "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return false;
            }
            String newPassword = new String(newField.getPassword());
            if (!newPassword.equals(new String(confirmField.getPassword()))) {
                message = "The two new passwords are not the same.";
                continue;
            }
            String error = accounts.changePassword(account, new String(oldField.getPassword()), newPassword);
            if (error == null) {
                JOptionPane.showMessageDialog(parent, "Password changed.");
                return true;
            }
            message = error;
        }
    }
}
