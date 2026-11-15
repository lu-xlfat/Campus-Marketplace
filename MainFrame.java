
package campusmarketplace.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;

import campusmarketplace.model.User;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.*;

public class MainFrame extends JFrame {

    private final User currentUser;
    private final JPanel contentPanel = new JPanel();
    
    private JLabel welcomeLabel;


    public MainFrame(User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "A logged-in user is required.");
        }

        this.currentUser = user;

        setTitle("Campus Marketplace - Dashboard");
        setSize(950, 600);
        setMinimumSize(new java.awt.Dimension(750, 450));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        
        addWindowListener(new WindowAdapter() {

        @Override
        public void windowClosing(WindowEvent e) {

            int choice = JOptionPane.showConfirmDialog(
                MainFrame.this,
                "Are you sure you want to exit Campus Marketplace?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
            }
        }
        });


        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        createHeader();
        createNavigation();
        createContent();

        showWelcome();
    }

    private void createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(
                15, 20, 15, 20));

        JLabel title = new JLabel("Campus Marketplace");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

       
        welcomeLabel = new JLabel(
        "Logged in as: " + currentUser.getFullName());


        JButton logoutButton = new JButton("Logout");

        logoutButton.addActionListener(e -> logout());

        JPanel rightPanel = new JPanel();
        rightPanel.add(welcomeLabel);
        rightPanel.add(logoutButton);

        header.add(title, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    
private void createNavigation() {

    JPanel navigation = new JPanel(
            new GridLayout(7, 1, 5, 10));

    navigation.setBorder(
            BorderFactory.createEmptyBorder(
                    20, 15, 20, 15));

    JButton homeButton =
            new JButton("Dashboard");

    JButton marketplaceButton =
            new JButton("Marketplace");

    JButton listingsButton =
            new JButton("My Listings");

    JButton reservationsButton =
            new JButton("My Reservations");

    JButton purchasesButton =
            new JButton("My Purchases");

    JButton inboxButton =
            new JButton("Inbox");

    JButton profileButton =
            new JButton("Profile");

    // Dashboard
    homeButton.addActionListener(
            e -> showWelcome());

    // Marketplace
    marketplaceButton.addActionListener(
            e -> setContent(
                    new MarketplacePanel(currentUser)));

    // My Listings
    listingsButton.addActionListener(
            e -> setContent(
                    new MyListingsPanel(this, currentUser)));

    // My Reservations
    reservationsButton.addActionListener(
            e -> setContent(
                    new MyReservationsPanel(currentUser)));

    // My Purchases
    purchasesButton.addActionListener(
            e -> setContent(
                    new MyPurchasesPanel(currentUser)));

    // Inbox
    inboxButton.addActionListener(
            e -> setContent(
                    new InboxPanel(currentUser)));

    // Profile 
    
    
profileButton.addActionListener(
        e -> setContent(
                new ProfilePanel(
                        currentUser,
                        this::refreshUserDisplay)));



    // Add buttons to navigation panel
    navigation.add(homeButton);
    navigation.add(marketplaceButton);
    navigation.add(listingsButton);
    navigation.add(reservationsButton);
    navigation.add(purchasesButton);
    navigation.add(inboxButton);
    navigation.add(profileButton);

    add(navigation, BorderLayout.WEST);
}


    private void createContent() {

        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(
                20, 20, 20, 20));

        add(contentPanel, BorderLayout.CENTER);
    }

    
    private void showWelcome() {

    setContent(new DashboardPanel(currentUser));

    }
 

    private void showPlaceholder(String section) {

        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel(
                section + " - Coming Soon",
                SwingConstants.CENTER);

        label.setFont(new Font("SansSerif", Font.BOLD, 22));

        panel.add(label, BorderLayout.CENTER);

        setContent(panel);
    }

    private void setContent(JPanel panel) {

        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
        
        updateWindowTitle(panel);
    }

    private void logout() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }
    
public void refreshUserDisplay() {

    welcomeLabel.setText(
            "Logged in as: " + currentUser.getFullName());

    welcomeLabel.revalidate();
    welcomeLabel.repaint();
}

private void updateWindowTitle(
        javax.swing.JComponent panel) {

    String section = "Dashboard";

    if (panel instanceof MarketplacePanel) {
        section = "Marketplace";

    } else if (panel instanceof MyListingsPanel) {
        section = "My Listings";

    } else if (panel instanceof MyReservationsPanel) {
        section = "My Reservations";

    } else if (panel instanceof MyPurchasesPanel) {
        section = "My Purchases";

    } else if (panel instanceof InboxPanel) {
        section = "Inbox";

    } else if (panel instanceof ProfilePanel) {
        section = "Profile";
    }

    setTitle("Campus Marketplace - " + section);
}

}
