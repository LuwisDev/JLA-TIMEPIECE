package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public abstract class LoginPage extends JFrame {
    private AccountStore accounts;
    private String role;
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

        usernameField = new JTextField();
        Theme.styleField(usernameField);
        passwordField = new JPasswordField();
        Theme.styleField(passwordField);

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
        });
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tryLogin();
            }
        });

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
            secondsLocked = accounts.getLockSecondsLeft(username);
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

    protected abstract void openDashboard(Account account);
}
