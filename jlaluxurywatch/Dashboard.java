package jlaluxurywatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.awt.event.ActionEvent;

// Dashboard  (ABSTRACT CLASS)
// The main window used by ALL three programs: JLADash (customer), CashierDash (cashier)
// and AdminDash (admin).
// They look the same: a dark green menu on the left (the "sidebar") and a content area
// on the right that shows one "page" at a time.
// Before, that code was copied in both classes. Now it is written ONCE here.
//
// OOP IDEAS USED HERE
//  - INHERITANCE:   this class extends JFrame (a Swing window). JLADash, CashierDash and AdminDash
//                   extend THIS class, so they get the window, sidebar and page switching for free.
//  - ABSTRACTION:   the child classes only say WHICH pages and buttons they want
//                   (addPage, addMenuButton). They do not need to know how it is built.
//  - POLYMORPHISM:  beforeShowingPage() is "abstract": each child class writes its own version.
//                   The cashier refreshes its orders there, the customer does nothing.
//
// HOW A CHILD CLASS USES IT
//   super("Window title", "Sidebar title");   // 1. build the empty window
//   addPage("name", somePanel);               // 2. add pages
//   addMenuButton("Text", "name");            // 3. add a menu button that opens a page
//   finishLayout();                           // 4. put everything together
public abstract class Dashboard extends JFrame {

    //  Every window of the program has this size: 1300 x 700.
    public static final int WINDOW_WIDTH = 1300;
    public static final int WINDOW_HEIGHT = 700;

    // CardLayout = a layout that keeps many panels stacked like cards and shows ONE at a time.
    private CardLayout cardLayout;
    private JPanel mainPanel;       // holds the sidebar (left) and the pages (right)
    private JPanel sidebar;         // the dark green menu
    private JPanel contentPanel;    // the area where the pages are stacked
    private ArrayList<JButton> bottomButtons = new ArrayList<JButton>();   // buttons placed at the bottom of the sidebar

    //  Same as below, but without a line of text under the sidebar title.
    public Dashboard(String windowTitle, String sidebarTitle) {
        this(windowTitle, sidebarTitle, "");
    }

    // Builds the empty window with the sidebar title. Pages and buttons are added afterwards.
    // The subtitle is a small line under the title (for example "Cashier: Maria"); "" = none.
    public Dashboard(String windowTitle, String sidebarTitle, String subtitle) {
        setTitle(windowTitle);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // closing the window ends the program
        setResizable(false);

        // BorderLayout = 5 areas: NORTH, SOUTH, EAST, WEST and CENTER.
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.CREAM);
        mainPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        // makeColumn = a panel that puts its parts one under the other (top to bottom)
        sidebar = Theme.makeColumn(Theme.DARK_GREEN);
        sidebar.setPreferredSize(new Dimension(230, WINDOW_HEIGHT));
        Theme.addPadding(sidebar, 25, 12, 15, 12);     // top, left, bottom, right

        JLabel title = Theme.makeLabel(sidebarTitle, Theme.georgiaFont(22), Theme.GOLD);
        sidebar.add(title);
        if (subtitle.isEmpty()) {
            sidebar.add(Theme.gap(35));       // an empty gap of 35 pixels
        } else {
            JLabel sub = Theme.makeLabel(subtitle, Theme.normalFont(13), Theme.CREAM);
            sidebar.add(Theme.gap(6));
            sidebar.add(sub);
            sidebar.add(Theme.gap(29));
            Theme.alignCenter(sub);
        }
        Theme.alignCenter(title);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Theme.CREAM);
    }

    // ---------------------------------------------------------------------------
    //                 methods the child classes call to build the window
    // ---------------------------------------------------------------------------

    //  Adds a page. The name is used later to open it, like showPage("orders").
    protected void addPage(String pageName, JPanel page) {
        contentPanel.add(page, pageName);
    }

    //  Adds a gold button to the top part of the sidebar. Clicking it opens the page with that name.
    protected void addMenuButton(String text, String pageName) {
        JButton button = makeSidebarButton(Theme.goldButton(text));
        // When the button is clicked, actionPerformed runs and opens the page with that name.
        // "e" is the click event (we do not need it here).
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPage(pageName);
            }
        });
        sidebar.add(button);
        sidebar.add(Theme.gap(12));
    }

    //  Adds a green button just above Exit (for example "Back to Welcome").
    protected void addBottomButton(String text, ActionListener whenClicked) {
        JButton button = makeSidebarButton(Theme.greenButton(text));
        button.addActionListener(whenClicked);
        bottomButtons.add(button);    // we only remember it; finishLayout() places it at the bottom
    }

    //  Last step: adds the Exit button, puts the sidebar and pages in the window and shows it centered.
    protected void finishLayout() {
        sidebar.add(Theme.pushDown());    // pushes everything after it to the bottom

        for (int i = 0; i < bottomButtons.size(); i++) {
            sidebar.add(bottomButtons.get(i));
            sidebar.add(Theme.gap(12));
        }

        JButton exitButton = makeSidebarButton(Theme.greenButton("Exit"));
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ask "Yes / No" before closing the program
                if (Theme.askYesNo(Dashboard.this, "Exit JLA TIMEPIECE?", "Confirm Exit")) {
                    System.exit(0);
                }
            }
        });
        sidebar.add(exitButton);

        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
        pack();                         // make the window the size its content needs
        setLocationRelativeTo(null);    // null = center the window on the screen
    }

    // ---------------------------------------------------------------------------
    //                               showing pages
    // ---------------------------------------------------------------------------

    //  Opens a page. Runs beforeShowingPage() first so the page can refresh itself.
    public void showPage(String pageName) {
        beforeShowingPage(pageName);
        cardLayout.show(contentPanel, pageName);
    }

    // ABSTRACT METHOD: it has no body here. Every child class MUST write its own version.
    // It runs just before a page is shown (the cashier reloads the orders here).
    protected abstract void beforeShowingPage(String pageName);

    //  Gives a sidebar button the same size and alignment.
    private JButton makeSidebarButton(JButton button) {
        button.setMaximumSize(new Dimension(200, 42));
        Theme.alignCenter(button);
        return button;
    }
}
