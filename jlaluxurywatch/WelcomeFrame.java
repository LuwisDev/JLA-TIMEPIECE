package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// WelcomeFrame  (ABSTRACT CLASS)
// The welcome window design shared by the Customer, Cashier and Admin programs.
// Left: title + ONE button. Right: banner + featured watches.
//
// OOP IDEAS
//  - INHERITANCE:  CustomerWelcome (WelcomePage), CashierWelcome and AdminWelcome extend this class.
//  - ABSTRACTION:  the child class only says its role text, its button text, and what the button does.
public abstract class WelcomeFrame extends JFrame {
    private final Color darkgreen = Theme.DARK_GREEN;
    private final Color gold = Theme.GOLD;
    private final Color cream = Theme.CREAM;

    private WatchStore watchStore;

    //  roleText = e.g. "CASHIER PORTAL", buttonText = e.g. "LOGIN". The stores only feed the featured cards.
    protected WelcomeFrame(WatchStore watchStore, String roleText, String buttonText) {
        this.watchStore = watchStore;

        setTitle("JLA TIMEPIECE");
        setSize(Dashboard.WINDOW_WIDTH, Dashboard.WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(makeLeftPanel(roleText, buttonText), BorderLayout.WEST);
        mainPanel.add(makeRightPanel(), BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    //  ABSTRACT: each welcome page decides what its button opens.
    protected abstract void onButtonClick();

    // ================= LEFT SIDE: one button for each kind of user =================

    private JPanel makeLeftPanel(String roleText, String buttonText) {
        // makeColumn = a panel that puts its parts one under the other (top to bottom)
        JPanel left = Theme.makeColumn(darkgreen);
        left.setPreferredSize(new Dimension(330, 700));
        left.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, gold));   // a gold line on the right edge

        JLabel title = makeCenteredLabel("JLA TIMEPIECE", new Font("Serif", Font.BOLD, 25), gold);
        JLabel line1 = makeCenteredLabel("LUXURY", new Font("Serif", Font.BOLD, 25), cream);
        JLabel line2 = makeCenteredLabel("WATCHES", new Font("Serif", Font.BOLD, 25), cream);
        JLabel who = makeCenteredLabel(roleText, new Font("Times New Roman", Font.BOLD, 16), cream);

        JButton mainButton = makeRoleButton(buttonText);
        mainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onButtonClick();
            }
        });

        left.add(Theme.gap(55));
        left.add(title);
        left.add(Theme.gap(60));
        left.add(line1);
        left.add(Theme.gap(10));
        left.add(line2);
        left.add(Theme.gap(90));
        left.add(who);
        left.add(Theme.gap(18));
        left.add(mainButton);
        left.add(Theme.pushDown());
        return left;
    }

    private JLabel makeCenteredLabel(String text, Font font, Color color) {
        JLabel label = Theme.makeLabel(text, font, color);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        Theme.alignCenter(label);
        return label;
    }

    //  The big gold button on the left side.
    private JButton makeRoleButton(String text) {
        JButton button = Theme.goldButton(text);
        button.setBorder(Theme.goldBorder(2, 8, 10));
        button.setPreferredSize(new Dimension(250, 42));
        button.setMaximumSize(new Dimension(250, 42));
        Theme.alignCenter(button);
        return button;
    }

    // ================= RIGHT SIDE: banner + featured watches =================

    private JPanel makeRightPanel() {
        ImagePanel right = new ImagePanel("images/welcome_bg.jpg", new Color(41, 42, 35));
        right.setLayout(new BorderLayout(0, 10));
        Theme.addPadding(right, 20);

        // banner with the slogan on top
        ImagePanel banner = new ImagePanel("images/welcome_banner.jpg", darkgreen);
        banner.setPreferredSize(new Dimension(100, 140));
        banner.setBorder(Theme.goldLine(2));
        banner.setLayout(new BorderLayout());
        JLabel slogan = new JLabel("  BEYOND TIME, BEYOND ORDINARY");
        slogan.setFont(new Font("Times New Roman", Font.BOLD | Font.ITALIC, 16));
        slogan.setForeground(Color.WHITE);
        Theme.addPadding(slogan, 0, 6, 6, 0);
        banner.add(slogan, BorderLayout.SOUTH);

        // heading + the six featured watches
        JLabel featured = new JLabel("FEATURED WATCHES", SwingConstants.CENTER);
        featured.setFont(new Font("Times New Roman", Font.BOLD | Font.ITALIC, 25));
        featured.setForeground(Color.BLACK);

        JPanel grid = new JPanel(new GridLayout(2, 3, 18, 14));
        grid.setOpaque(false);
        ArrayList<Watch> watches = watchStore.getFeaturedWatches();
        for (int i = 0; i < watches.size() && i < 6; i++) {
            grid.add(makeCard(watches.get(i)));
        }

        JPanel center = Theme.makeBorderPanel(0, 8);
        center.add(featured, BorderLayout.NORTH);
        center.add(grid, BorderLayout.CENTER);

        right.add(banner, BorderLayout.NORTH);
        right.add(center, BorderLayout.CENTER);
        return right;
    }

    //  One small card: the watch picture and its name.
    private JPanel makeCard(Watch watch) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(darkgreen);
        card.setBorder(Theme.goldLine(2));

        JLabel picture = new JLabel();
        picture.setHorizontalAlignment(SwingConstants.CENTER);
        Theme.showImage(picture, watch.getImagePath(), 150, 125, "");   // "" = show nothing if the picture is missing

        JLabel name = Theme.makeLabel(watch.getBrand() + " " + watch.getModel(), new Font("Serif", Font.BOLD, 15), cream);
        name.setHorizontalAlignment(SwingConstants.CENTER);
        Theme.addPadding(name, 0, 4, 8, 4);

        card.add(picture, BorderLayout.CENTER);
        card.add(name, BorderLayout.SOUTH);
        return card;
    }

    //  A panel that paints a picture over its whole area (cropped, never stretched).
    private static class ImagePanel extends JPanel {
        private Image image;

        ImagePanel(String path, Color fallback) {
            setBackground(fallback);
            if (new File(path).exists()) image = new ImageIcon(path).getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image == null) return;
            int iw = image.getWidth(null), ih = image.getHeight(null);
            if (iw <= 0 || ih <= 0) return;
            double scale = Math.max((double) getWidth() / iw, (double) getHeight() / ih);   // "cover"
            int w = (int) Math.ceil(iw * scale), h = (int) Math.ceil(ih * scale);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(image, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
            g2.dispose();
        }
    }
}
