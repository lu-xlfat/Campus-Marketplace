
package campusmarketplace.ui;

import campusmarketplace.model.User;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class DashboardPanel extends JPanel {

    private static final Color BG =
            new Color(240, 240, 240);

    private static final Color CARD_BG =
            new Color(247, 249, 252);

    private static final Color CARD_BORDER =
            new Color(186, 201, 217);

    private static final Color ACCENT =
            new Color(139, 168, 198);

    private static final Color DARK =
            new Color(36, 55, 76);

    private static final Color MUTED =
            new Color(99, 117, 136);

    public DashboardPanel(User currentUser) {

        if (currentUser == null) {
            throw new IllegalArgumentException(
                    "A logged-in user is required.");
        }

        setLayout(new BorderLayout());
        setBackground(BG);

        JPanel page = new JPanel(
                new BorderLayout(0, 20));

        page.setBackground(BG);
        page.setBorder(
                new EmptyBorder(12, 10, 18, 10));

        // Welcome and section heading
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);

        topPanel.setLayout(
                new BoxLayout(
                        topPanel,
                        BoxLayout.Y_AXIS));

        JLabel welcome = new JLabel(
                "Welcome, "
                + currentUser.getFullName()
                + "!");

        welcome.setFont(
                new Font("Segoe UI", Font.BOLD, 25));

        welcome.setForeground(DARK);
        welcome.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "Buy and sell second-hand items with students.");

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));

        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        JSeparator separator = new JSeparator();
        separator.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        JLabel sectionTitle = new JLabel(
                "Explore Campus Marketplace");

        sectionTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 18));

        sectionTitle.setForeground(DARK);
        sectionTitle.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        topPanel.add(welcome);
        topPanel.add(Box.createVerticalStrut(8));
        topPanel.add(subtitle);
        topPanel.add(Box.createVerticalStrut(20));
        topPanel.add(separator);
        topPanel.add(Box.createVerticalStrut(18));
        topPanel.add(sectionTitle);

        page.add(topPanel, BorderLayout.NORTH);

        // Feature cards: 2 columns and 3 rows
        JPanel featureGrid = new JPanel(
                new GridLayout(3, 2, 14, 12));

        featureGrid.setOpaque(false);

        featureGrid.add(createFeatureCard(
                "Marketplace",
                "Browse available products"));

        featureGrid.add(createFeatureCard(
                "My Listings",
                "Manage your products"));

        featureGrid.add(createFeatureCard(
                "My Reservations",
                "View reserved items"));

        featureGrid.add(createFeatureCard(
                "My Purchases",
                "Review purchased items"));

        featureGrid.add(createFeatureCard(
                "Inbox",
                "Read and send messages"));

        featureGrid.add(createFeatureCard(
                "Profile",
                "Manage your account"));

        page.add(featureGrid, BorderLayout.CENTER);

       
        
        add(page, BorderLayout.CENTER);

    }

    private JPanel createFeatureCard(
            String title,
            String description) {

        JPanel card = new JPanel(
                new BorderLayout(12, 0));

        card.setBackground(CARD_BG);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(
                                CARD_BORDER, 1, true),
                        new EmptyBorder(
                                12, 14, 12, 14)
                )
        );

        card.setPreferredSize(
                new Dimension(230, 88));

        // Soft-blue accent matching sidebar buttons
        JPanel accentBar = new JPanel();

        accentBar.setBackground(ACCENT);

        accentBar.setPreferredSize(
                new Dimension(4, 36));

        card.add(
                accentBar,
                BorderLayout.WEST);

        JPanel textPanel = new JPanel();

        textPanel.setOpaque(false);

        textPanel.setLayout(
                new BoxLayout(
                        textPanel,
                        BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 15));

        titleLabel.setForeground(DARK);

        titleLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        JLabel descriptionLabel =
                new JLabel(description);

        descriptionLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        descriptionLabel.setForeground(MUTED);

        descriptionLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        textPanel.add(titleLabel);

        textPanel.add(
                Box.createVerticalStrut(6));

        textPanel.add(descriptionLabel);

        card.add(
                textPanel,
                BorderLayout.CENTER);

        return card;
    }
}
