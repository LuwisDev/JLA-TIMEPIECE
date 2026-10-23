package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// LoginPage  (ABSTRACT CLASS)
// The login window used by the Cashier program and the Admin program.
// It asks for a username and password and checks them with the AccountStore.
//
// OOP IDEAS
//  - INHERITANCE:  CashierLogin and AdminLogin extend this class.
//  - ABSTRACTION:  the child class only says WHICH dashboard to open (openDashboard).
//  - POLYMORPHISM: this class calls openDashboard() without knowing which child it is.
//
// A cashier account cannot log in on the Admin page and the other way around:
// login() also checks the role.
public abstract class LoginPage extends JFrame {
    private AccountStore accounts;
    private String role;                    // Account.CASHIER or Account.ADMIN
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;

    protected LoginPage(AccountStore accounts, String role, String heading) {
        this.accounts = accounts;
        this.role = role;

        setTitle("JLA TIMEPIECE - " + heading);
        setSize(Dashboard.WINDOW_WIDTH, Dashboard.WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        JLabel title = Theme.makeLabel("JLA TIMEPIECE", Theme.georgiaFont(36), Theme.GOLD);
        JLabel sub = Theme.makeLabel(heading, Theme.normalFont(18), Theme.CREAM);
        Theme.alignCenter(title, sub);

        // ---- the cream login card ----
        usernameField = new JTextField();
        Theme.styleField(usernameField);
        passwordField = new JPasswordField();
        Theme.styleField(passwordField);        // a password box is also a text box, so the same style works

        messageLabel = Theme.makeLabel(" ", Theme.BOLD_FONT, new Color(160, 30, 30));

        JButton loginButton = Theme.goldButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tryLogin();
            }
        });
        usernameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                passwordField.requestFocus();
            }
        });   // Enter moves to the password
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tryLogin();
            }
        });                     // Enter logs in

        JPanel form = Theme.makeStackedPanel(6);
        form.add(Theme.makeLabel("Username:"));
        form.add(usernameField);
        form.add(Theme.makeLabel("Password:"));
        form.add(passwordField);

        JPanel card = Theme.makeCard(24, 28);
        card.setPreferredSize(new Dimension(380, 290));
        card.setMaximumSize(new Dimension(380, 290));
        card.add(form, BorderLayout.NORTH);
        card.add(messageLabel, BorderLayout.CENTER);
        card.add(loginButton, BorderLayout.SOUTH);
        Theme.alignCenter(card);

        JPanel center = Theme.makeColumn(Theme.DARK_GREEN);
        center.add(Theme.pushDown());
        center.add(title);
        center.add(Theme.gap(6));
        center.add(sub);
        center.add(Theme.gap(28));
        center.add(card);
        center.add(Theme.pushDown());
        setContentPane(center);
    }

    //  Checks the typed username and password. On success, opens the dashboard and closes this window.
    private void tryLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        int secondsLocked = accounts.getLockSecondsLeft(username);
        if (secondsLocked > 0) {
            messageLabel.setText("Too many tries. Wait " + secondsLocked + " seconds.");
            passwordField.setText("");
            return;
        }
        Account account = accounts.login(username, password, role);
        if (account == null) {
            secondsLocked = accounts.getLockSecondsLeft(username);      // did this wrong try lock the username?
            if (secondsLocked > 0) {
                messageLabel.setText("Too many tries. Locked " + secondsLocked + " seconds.");
            } else {
                messageLabel.setText("Wrong username or password (" + accounts.getAttemptsLeft(username) + " tries left).");
            }
            passwordField.setText("");
            return;
        }
        openDashboard(account);
        dispose();
    }

    //  ABSTRACT: each child class opens its own dashboard for the account that logged in.
    protected abstract void openDashboard(Account account);
}
