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

// Theme
// Colors, fonts and small helper methods shared by every screen.
//
// Everything here is "static", so you use it like Theme.GOLD or Theme.goldButton("OK")
// without creating an object. Keeping the look in ONE class means every screen has the
// same style, and changing a color here changes the whole program.
public class Theme {

    // ----- colors. new Color(red, green, blue): each number is from 0 to 255 -----
    public static final Color DARK_GREEN = new Color(10, 48, 34);
    public static final Color GOLD = new Color(197, 166, 107);
    public static final Color DARK_GOLD = new Color(150, 120, 60);
    public static final Color CREAM = new Color(233, 227, 210);
    public static final Color WHITE = Color.WHITE;

    // ----- fonts. new Font(name, style, size) -----
    public static final Font HEADING_FONT = new Font("Georgia", Font.BOLD, 24);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);

    //  Gold button with dark green text.
    public static JButton goldButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(GOLD);
        button.setForeground(DARK_GREEN);
        button.setFont(BOLD_FONT);
        button.setOpaque(true);                 // needed so the background color is really painted
        button.setFocusPainted(false);          // no dotted box around the text when clicked
        // CompoundBorder = two borders in one: a thin colored line outside + empty space inside (padding)
        button.setBorder(new CompoundBorder(new LineBorder(DARK_GOLD, 1), new EmptyBorder(9, 18, 9, 18)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));   // the mouse turns into a hand
        return button;
    }

    //  Dark green button with cream text and a gold outline.
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

    //  Big title text used at the top of each screen.
    public static JLabel makeHeading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(HEADING_FONT);
        label.setForeground(DARK_GREEN);
        return label;
    }

    //  Bold label, used in front of text boxes ("Customer name:").
    public static JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BOLD_FONT);
        label.setForeground(DARK_GREEN);
        return label;
    }

    // The "Reference code: [______] [Find]" row used on the Transactions screens.
    // whenFind runs when the button is clicked or Enter is pressed in the box.
    public static JPanel makeFindRow(JTextField field, Runnable whenFind) {
        return makeFindRow(field, whenFind, null);
    }

    //  Same row, with a Refresh button next to Find. whenRefresh = null means no Refresh button.
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

    //  Gives a text box the store style (white background, gold border, padding).
    public static void styleField(JTextField field) {
        field.setFont(BODY_FONT);
        field.setForeground(DARK_GREEN);
        field.setBackground(WHITE);
        field.setBorder(goldBorder(1, 6, 8));
    }

    //  A scroll pane with only an up/down scroll bar (no horizontal bar).
    public static JScrollPane makeScroll(Component view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);   // how far one mouse-wheel click scrolls
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CREAM);
        return scroll;
    }

    //  Gives a table (JTable) the same green and gold look as the rest of the app.
    public static void styleTable(JTable table) {
        table.setFont(BODY_FONT);
        table.setForeground(DARK_GREEN);
        table.setBackground(WHITE);
        table.setRowHeight(28);
        table.setGridColor(CREAM);
        table.setSelectionBackground(GOLD);               // color of the clicked row
        table.setSelectionForeground(DARK_GREEN);
        table.setDefaultEditor(Object.class, null);       // null editor = cells cannot be typed into
        table.getTableHeader().setFont(BOLD_FONT);        // the header is the first row with the column names
        table.getTableHeader().setBackground(DARK_GREEN);
        table.getTableHeader().setForeground(CREAM);
        table.getTableHeader().setReorderingAllowed(false);   // columns cannot be dragged around
    }

    //  Gives a list (JList) the same look.
    public static void styleList(JList<String> list) {
        list.setFont(BODY_FONT);
        list.setForeground(DARK_GREEN);
        list.setBackground(WHITE);
        list.setSelectionBackground(GOLD);
        list.setSelectionForeground(DARK_GREEN);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);   // only one item can be selected
    }

    //  A read-only text box for receipts. Monospaced font = every letter has the same width, so lines line up.
    public static JTextArea makeReceiptArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);          // long lines continue on the next line
        area.setWrapStyleWord(true);     // ...and only break between words
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setForeground(DARK_GREEN);
        area.setBackground(WHITE);
        addPadding(area, 10);
        return area;
    }

    // =====================================================================================
    //                        EASY HELPERS  (short names for long Swing code)
    // =====================================================================================

    // ----- fonts: Theme.georgiaFont(22), Theme.normalFont(14), Theme.boldFont(16) -----

    //  Georgia, bold. Used for titles.
    public static Font georgiaFont(int size) {
        return new Font("Georgia", Font.BOLD, size);
    }

    //  Segoe UI, normal. Used for ordinary text.
    public static Font normalFont(int size) {
        return new Font("Segoe UI", Font.PLAIN, size);
    }

    //  Segoe UI, bold.
    public static Font boldFont(int size) {
        return new Font("Segoe UI", Font.BOLD, size);
    }

    // ----- spacing and borders -----

    //  An empty gap (in pixels) between two things in a column made with makeColumn().
    public static Component gap(int pixels) {
        return Box.createVerticalStrut(pixels);
    }

    //  Empty space that grows and pushes everything after it to the bottom of a column.
    public static Component pushDown() {
        return Box.createVerticalGlue();
    }

    //  Empty space INSIDE the edges of a panel, label, etc.
    public static void addPadding(JComponent thing, int top, int left, int bottom, int right) {
        thing.setBorder(new EmptyBorder(top, left, bottom, right));
    }

    //  Same padding on all four sides.
    public static void addPadding(JComponent thing, int allSides) {
        addPadding(thing, allSides, allSides, allSides, allSides);
    }

    //  Only a gold line (no padding). lineSize = how thick, in pixels.
    public static Border goldLine(int lineSize) {
        return new LineBorder(GOLD, lineSize);
    }

    //  A gold line with padding inside it.
    public static Border goldBorder(int lineSize, int paddingUpDown, int paddingLeftRight) {
        return new CompoundBorder(new LineBorder(GOLD, lineSize),
                new EmptyBorder(paddingUpDown, paddingLeftRight, paddingUpDown, paddingLeftRight));
    }

    //  A gold line with the same padding on every side.
    public static Border goldBorder(int lineSize, int padding) {
        return goldBorder(lineSize, padding, padding);
    }

    // ----- panels -----

    //  A see-through panel whose parts are stacked from top to bottom.
    public static JPanel makeColumn() {
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        return column;
    }

    //  Same, but with a colored background (for example the dark green side menu).
    public static JPanel makeColumn(Color background) {
        JPanel column = makeColumn();
        column.setBackground(background);
        column.setOpaque(true);
        return column;
    }

    //  A see-through panel with 5 areas (NORTH, SOUTH, EAST, WEST, CENTER). The gaps are in pixels.
    public static JPanel makeBorderPanel(int sideGap, int upDownGap) {
        JPanel panel = new JPanel(new BorderLayout(sideGap, upDownGap));
        panel.setOpaque(false);
        return panel;
    }

    //  A see-through panel where every part gets its own row, one under the other.
    public static JPanel makeStackedPanel(int gapBetweenRows) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, gapBetweenRows));
        panel.setOpaque(false);
        return panel;
    }

    // A row of parts (buttons, labels, boxes). side is "left", "right" or "center".
    // Example: Theme.makeRow("right", cancelButton, saveButton)
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

    //  A cream box with a gold border (like the login box).
    public static JPanel makeCard(int paddingUpDown, int paddingLeftRight) {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(CREAM);
        card.setBorder(goldBorder(2, paddingUpDown, paddingLeftRight));
        return card;
    }

    //  Sets up a normal page: cream background, 5-area layout and padding around the edges.
    public static void setupPage(JPanel page, int gap, int padding) {
        page.setLayout(new BorderLayout(gap, gap));
        page.setBackground(CREAM);
        addPadding(page, padding);
    }

    //  The main panel inside a dialog window (cream, with padding). Add your parts to it.
    public static JPanel makeDialogRoot(int sideGap, int upDownGap) {
        JPanel root = new JPanel(new BorderLayout(sideGap, upDownGap));
        root.setBackground(CREAM);
        addPadding(root, 20, 20, 16, 20);
        return root;
    }

    //  Last step of every dialog: puts the root in, sizes it, and centers it on the owner window.
    public static void finishDialog(JDialog dialog, JPanel root, Component owner) {
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);
    }

    //  The top bar of a screen: the heading on the left and something (filters...) on the right.
    public static JPanel makeTopBar(String headingText, Component rightSide) {
        JPanel bar = makeBorderPanel(0, 0);
        bar.add(makeHeading(headingText), BorderLayout.WEST);
        bar.add(rightSide, BorderLayout.EAST);
        return bar;
    }

    // ----- labels and text -----

    //  A label with the font and color you choose.
    public static JLabel makeLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    //  Lines parts up on the left side of a column.
    public static void alignLeft(JComponent... parts) {
        for (int i = 0; i < parts.length; i++) {
            parts[i].setAlignmentX(Component.LEFT_ALIGNMENT);
        }
    }

    //  Lines parts up in the middle of a column.
    public static void alignCenter(JComponent... parts) {
        for (int i = 0; i < parts.length; i++) {
            parts[i].setAlignmentX(Component.CENTER_ALIGNMENT);
        }
    }

    // Shows long text on several lines. The text is cut into lines that fit inside the
    // width (in pixels) and each line is its own label. A "\n" in the text starts a new line.
    // alignment = "left" or "center".
    public static JPanel makeWrappedText(String text, int width, Font font, Color color, String alignment) {
        FontMetrics metrics = new JLabel().getFontMetrics(font);    // measures how wide a text is
        ArrayList<String> lines = new ArrayList<String>();
        String[] paragraphs = text.split("\n");
        for (int p = 0; p < paragraphs.length; p++) {
            String[] words = paragraphs[p].split(" ");
            String line = "";
            for (int w = 0; w < words.length; w++) {
                String longerLine = line.isEmpty() ? words[w] : line + " " + words[w];
                if (line.isEmpty() || metrics.stringWidth(longerLine) <= width) {
                    line = longerLine;          // the word still fits on this line
                } else {
                    lines.add(line);            // the line is full, so start a new one
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

    //  Read-only text that wraps and shows the cream background behind it.
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

    //  One row of a form: the label on the left, the input on the right.
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

    // ----- pictures -----

    // Shows a picture in a label. If the picture file is missing, shows missingText instead.
    // Example: Theme.showImage(label, watch.getImagePath(), 150, 130, "Watch image")
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

    //  A dark green box with a gold border and a centered watch picture (or "Watch image" if missing).
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

    // ----- scroll panes -----

    //  A scroll pane with a gold border and a white background (for tables, lists and text boxes).
    public static JScrollPane makeTableScroll(Component view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setBorder(goldLine(2));
        scroll.getViewport().setBackground(WHITE);
        return scroll;
    }

    //  A scroll pane with a gold border and a title (like "Receipt"). It has a fixed width.
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

    // ----- input boxes -----

    //  A drop-down list with the store style. Example: Theme.makeComboBox(new String[]{"A", "B"})
    public static <T> JComboBox<T> makeComboBox(T[] choices) {
        JComboBox<T> box = new JComboBox<T>(choices);
        box.setFont(BODY_FONT);
        box.setBackground(WHITE);
        box.setForeground(DARK_GREEN);
        return box;
    }

    //  Runs the action every time the text in the box changes (the user types or deletes).
    public static void whenTyping(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { action.run(); }
            public void removeUpdate(DocumentEvent e) { action.run(); }
            public void changedUpdate(DocumentEvent e) { action.run(); }
        });
    }

    // ----- message windows -----

    //  Shows a drop-down list of choices in a small window. Returns the chosen text, or null if the user cancelled.
    public static String askChoice(Component parent, String question, String title, String[] choices) {
        Object chosen = JOptionPane.showInputDialog(parent, question, title,
                JOptionPane.WARNING_MESSAGE, null, choices, choices[0]);
        return (String) chosen;
    }

    //  Asks for a password in a small window. Returns what was typed, or null if the user clicked Cancel.
    public static String askPassword(Component parent, String prompt, String title) {
        JPasswordField passwordField = new JPasswordField();
        int answer = JOptionPane.showConfirmDialog(parent, new Object[]{prompt, passwordField},
                title, JOptionPane.OK_CANCEL_OPTION);
        if (answer != JOptionPane.OK_OPTION) {
            return null;
        }
        return new String(passwordField.getPassword());
    }

    //  Shows a message with an OK button.
    public static void showMessage(Component parent, String text) {
        JOptionPane.showMessageDialog(parent, text);
    }

    //  Shows a red error message with a title.
    public static void showError(Component parent, String text, String title) {
        JOptionPane.showMessageDialog(parent, text, title, JOptionPane.ERROR_MESSAGE);
    }

    //  Asks a Yes / No question. Returns true if the user clicked Yes.
    public static boolean askYesNo(Component parent, String question, String title) {
        int answer = JOptionPane.showConfirmDialog(parent, question, title, JOptionPane.YES_NO_OPTION);
        return answer == JOptionPane.YES_OPTION;
    }

    //  Same, with a warning icon (used before deleting things).
    public static boolean askYesNoWarning(Component parent, String question, String title) {
        int answer = JOptionPane.showConfirmDialog(parent, question, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return answer == JOptionPane.YES_OPTION;
    }

    //  Loads an image and shrinks it to fit the box without stretching it. Returns null if missing.
    public static ImageIcon loadImage(String path, int maxWidth, int maxHeight) {
        if (path == null || path.equals("")) return null;
        File file = new File(path);
        if (!file.exists()) return null;
        ImageIcon icon = new ImageIcon(path);
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();
        if (w <= 0 || h <= 0) return null;
        // ratio = how much we must shrink so BOTH the width and the height fit the box
        double ratio = Math.min((double) maxWidth / w, (double) maxHeight / h);
        int newWidth = (int) (w * ratio);
        int newHeight = (int) (h * ratio);
        Image scaled = icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
