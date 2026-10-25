package jlaluxurywatch;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Theme {

    public static final Color DARK_GREEN = new Color(10, 48, 34);
    public static final Color GOLD = new Color(197, 166, 107);
    public static final Color DARK_GOLD = new Color(150, 120, 60);
    public static final Color CREAM = new Color(233, 227, 210);
    public static final Color WHITE = Color.WHITE;

    public static final Font HEADING_FONT = new Font("Georgia", Font.BOLD, 24);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);

    public static JButton goldButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(GOLD);
        button.setForeground(DARK_GREEN);
        button.setFont(BOLD_FONT);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(new LineBorder(DARK_GOLD, 1), new EmptyBorder(9, 18, 9, 18)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JButton greenButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(DARK_GREEN);
        button.setForeground(CREAM);
        button.setFont(BOLD_FONT);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(goldBorder(1, 9, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JLabel makeHeading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(HEADING_FONT);
        label.setForeground(DARK_GREEN);
        return label;
    }

    public static JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BOLD_FONT);
        label.setForeground(DARK_GREEN);
        return label;
    }

    public static JPanel makeFindRow(JTextField field, Runnable whenFind) {
        return makeFindRow(field, whenFind, null);
    }

    public static JPanel makeFindRow(JTextField field, Runnable whenFind, Runnable whenRefresh) {
        styleField(field);
        field.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                whenFind.run();
            }
        });
        JButton findButton = greenButton("Find");
        findButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                whenFind.run();
            }
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(findButton);
        if (whenRefresh != null) {
            JButton refreshButton = greenButton("Refresh");
            refreshButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    whenRefresh.run();
                }
            });
            buttons.add(refreshButton);
        }
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(makeLabel("Reference code:"), BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        row.add(buttons, BorderLayout.EAST);
        return row;
    }

    public static void styleField(JTextField field) {
        field.setFont(BODY_FONT);
        field.setForeground(DARK_GREEN);
        field.setBackground(WHITE);
        field.setBorder(goldBorder(1, 6, 8));
    }

    public static JScrollPane makeScroll(Component view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CREAM);
        return scroll;
    }

    public static void styleTable(JTable table) {
        table.setFont(BODY_FONT);
        table.setForeground(DARK_GREEN);
        table.setBackground(WHITE);
        table.setRowHeight(28);
        table.setGridColor(CREAM);
        table.setSelectionBackground(GOLD);
        table.setSelectionForeground(DARK_GREEN);
        table.setDefaultEditor(Object.class, null);
        table.getTableHeader().setFont(BOLD_FONT);
        table.getTableHeader().setBackground(DARK_GREEN);
        table.getTableHeader().setForeground(CREAM);
        table.getTableHeader().setReorderingAllowed(false);
    }

    public static void styleList(JList<String> list) {
        list.setFont(BODY_FONT);
        list.setForeground(DARK_GREEN);
        list.setBackground(WHITE);
        list.setSelectionBackground(GOLD);
        list.setSelectionForeground(DARK_GREEN);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public static JTextArea makeReceiptArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setForeground(DARK_GREEN);
        area.setBackground(WHITE);
        addPadding(area, 10);
        return area;
    }

    public static Font georgiaFont(int size) {
        return new Font("Georgia", Font.BOLD, size);
    }

    public static Font normalFont(int size) {
        return new Font("Segoe UI", Font.PLAIN, size);
    }

    public static Font boldFont(int size) {
        return new Font("Segoe UI", Font.BOLD, size);
    }

    public static Component gap(int pixels) {
        return Box.createVerticalStrut(pixels);
    }

    public static Component pushDown() {
        return Box.createVerticalGlue();
    }

    public static void addPadding(JComponent thing, int top, int left, int bottom, int right) {
        thing.setBorder(new EmptyBorder(top, left, bottom, right));
    }

    public static void addPadding(JComponent thing, int allSides) {
        addPadding(thing, allSides, allSides, allSides, allSides);
    }

    public static Border goldLine(int lineSize) {
        return new LineBorder(GOLD, lineSize);
    }

    public static Border goldBorder(int lineSize, int paddingUpDown, int paddingLeftRight) {
        return new CompoundBorder(new LineBorder(GOLD, lineSize),
                new EmptyBorder(paddingUpDown, paddingLeftRight, paddingUpDown, paddingLeftRight));
    }

    public static Border goldBorder(int lineSize, int padding) {
        return goldBorder(lineSize, padding, padding);
    }

    public static JPanel makeColumn() {
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        return column;
    }

    public static JPanel makeColumn(Color background) {
        JPanel column = makeColumn();
        column.setBackground(background);
        column.setOpaque(true);
        return column;
    }

    public static JPanel makeBorderPanel(int sideGap, int upDownGap) {
        JPanel panel = new JPanel(new BorderLayout(sideGap, upDownGap));
        panel.setOpaque(false);
        return panel;
    }

    public static JPanel makeStackedPanel(int gapBetweenRows) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, gapBetweenRows));
        panel.setOpaque(false);
        return panel;
    }

    public static JPanel makeRow(String side, Component... parts) {
        int alignment = FlowLayout.CENTER;
        if (side.equals("left")) alignment = FlowLayout.LEFT;
        if (side.equals("right")) alignment = FlowLayout.RIGHT;
        JPanel row = new JPanel(new FlowLayout(alignment, 10, 0));
        row.setOpaque(false);
        for (int i = 0; i < parts.length; i++) {
            row.add(parts[i]);
        }
        return row;
    }

    public static JPanel makeCard(int paddingUpDown, int paddingLeftRight) {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(CREAM);
        card.setBorder(goldBorder(2, paddingUpDown, paddingLeftRight));
        return card;
    }

    public static void setupPage(JPanel page, int gap, int padding) {
        page.setLayout(new BorderLayout(gap, gap));
        page.setBackground(CREAM);
        addPadding(page, padding);
    }

    public static JPanel makeDialogRoot(int sideGap, int upDownGap) {
        JPanel root = new JPanel(new BorderLayout(sideGap, upDownGap));
        root.setBackground(CREAM);
        addPadding(root, 20, 20, 16, 20);
        return root;
    }

    public static void finishDialog(JDialog dialog, JPanel root, Component owner) {
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);
    }

    public static JPanel makeTopBar(String headingText, Component rightSide) {
        JPanel bar = makeBorderPanel(0, 0);
        bar.add(makeHeading(headingText), BorderLayout.WEST);
        bar.add(rightSide, BorderLayout.EAST);
        return bar;
    }

    public static JLabel makeLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    public static void alignLeft(JComponent... parts) {
        for (int i = 0; i < parts.length; i++) {
            parts[i].setAlignmentX(Component.LEFT_ALIGNMENT);
        }
    }

    public static void alignCenter(JComponent... parts) {
        for (int i = 0; i < parts.length; i++) {
            parts[i].setAlignmentX(Component.CENTER_ALIGNMENT);
        }
    }

    public static JPanel makeWrappedText(String text, int width, Font font, Color color, String alignment) {
        FontMetrics metrics = new JLabel().getFontMetrics(font);
        ArrayList<String> lines = new ArrayList<String>();
        String[] paragraphs = text.split("\n");
        for (int p = 0; p < paragraphs.length; p++) {
            String[] words = paragraphs[p].split(" ");
            String line = "";
            for (int w = 0; w < words.length; w++) {
                String longerLine = line.isEmpty() ? words[w] : line + " " + words[w];
                if (line.isEmpty() || metrics.stringWidth(longerLine) <= width) {
                    line = longerLine;
                } else {
                    lines.add(line);
                    line = words[w];
                }
            }
            lines.add(line);
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        for (int i = 0; i < lines.size(); i++) {
            JLabel label = makeLabel(lines.get(i), font, color);
            if (alignment.equals("center")) {
                alignCenter(label);
            } else {
                alignLeft(label);
            }
            panel.add(label);
        }
        return panel;
    }

    public static JTextArea makeReadOnlyText(String text, int rows, int columns) {
        JTextArea area = new JTextArea(text, rows, columns);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setFont(BODY_FONT);
        area.setForeground(DARK_GREEN);
        area.setBorder(null);
        return area;
    }

    public static JPanel makeFormRow(String labelText, Component input) {
        JLabel label = makeLabel(labelText);
        label.setVerticalAlignment(SwingConstants.TOP);
        label.setPreferredSize(new Dimension(150, 20));
        addPadding(label, 7, 0, 0, 0);
        JPanel row = makeBorderPanel(10, 0);
        addPadding(row, 4, 0, 4, 0);
        row.add(label, BorderLayout.WEST);
        row.add(input, BorderLayout.CENTER);
        return row;
    }

    public static void showImage(JLabel label, String imagePath, int maxWidth, int maxHeight, String missingText) {
        ImageIcon icon = loadImage(imagePath, maxWidth, maxHeight);
        if (icon != null) {
            label.setIcon(icon);
            label.setText("");
        } else {
            label.setIcon(null);
            label.setText(missingText);
        }
    }

    public static JPanel makeImageBox(String imagePath, int boxWidth, int boxHeight, int imageWidth, int imageHeight) {
        JLabel image = new JLabel();
        image.setHorizontalAlignment(SwingConstants.CENTER);
        image.setForeground(CREAM);
        image.setFont(BODY_FONT);
        showImage(image, imagePath, imageWidth, imageHeight, "Watch image");

        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(DARK_GREEN);
        box.setBorder(goldLine(2));
        box.setPreferredSize(new Dimension(boxWidth, boxHeight));
        box.add(image, BorderLayout.CENTER);
        return box;
    }

    public static JScrollPane makeTableScroll(Component view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setBorder(goldLine(2));
        scroll.getViewport().setBackground(WHITE);
        return scroll;
    }

    public static JScrollPane makeTitledScroll(Component view, String title, int width) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(width, 0));
        TitledBorder titled = new TitledBorder(goldLine(2), title);
        titled.setTitleFont(BOLD_FONT);
        titled.setTitleColor(DARK_GREEN);
        scroll.setBorder(titled);
        return scroll;
    }

    public static <T> JComboBox<T> makeComboBox(T[] choices) {
        JComboBox<T> box = new JComboBox<T>(choices);
        box.setFont(BODY_FONT);
        box.setBackground(WHITE);
        box.setForeground(DARK_GREEN);
        return box;
    }

    public static void whenTyping(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { action.run(); }
            public void removeUpdate(DocumentEvent e) { action.run(); }
            public void changedUpdate(DocumentEvent e) { action.run(); }
        });
    }

    public static String askChoice(Component parent, String question, String title, String[] choices) {
        Object chosen = JOptionPane.showInputDialog(parent, question, title,
                JOptionPane.WARNING_MESSAGE, null, choices, choices[0]);
        return (String) chosen;
    }

    public static String askPassword(Component parent, String prompt, String title) {
        JPasswordField passwordField = new JPasswordField();
        int answer = JOptionPane.showConfirmDialog(parent, new Object[]{prompt, passwordField},
                title, JOptionPane.OK_CANCEL_OPTION);
        if (answer != JOptionPane.OK_OPTION) {
            return null;
        }
        return new String(passwordField.getPassword());
    }

    public static void showMessage(Component parent, String text) {
        JOptionPane.showMessageDialog(parent, text);
    }

    public static void showError(Component parent, String text, String title) {
        JOptionPane.showMessageDialog(parent, text, title, JOptionPane.ERROR_MESSAGE);
    }

    public static boolean askYesNo(Component parent, String question, String title) {
        int answer = JOptionPane.showConfirmDialog(parent, question, title, JOptionPane.YES_NO_OPTION);
        return answer == JOptionPane.YES_OPTION;
    }

    public static boolean askYesNoWarning(Component parent, String question, String title) {
        int answer = JOptionPane.showConfirmDialog(parent, question, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return answer == JOptionPane.YES_OPTION;
    }

    public static ImageIcon loadImage(String path, int maxWidth, int maxHeight) {
        if (path == null || path.equals("")) return null;
        File file = new File(path);
        if (!file.exists()) return null;
        ImageIcon icon = new ImageIcon(path);
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();
        if (w <= 0 || h <= 0) return null;
        double ratio = Math.min((double) maxWidth / w, (double) maxHeight / h);
        int newWidth = (int) (w * ratio);
        int newHeight = (int) (h * ratio);
        Image scaled = icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
