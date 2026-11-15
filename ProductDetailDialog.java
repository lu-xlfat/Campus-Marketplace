
package campusmarketplace.ui;

import campusmarketplace.dao.UserDAO;
import campusmarketplace.model.Product;
import campusmarketplace.model.SellerInfo;
import campusmarketplace.model.User;
import campusmarketplace.service.ReservationService;

import java.awt.*;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class ProductDetailDialog extends JDialog {

    // Colors matching Dashboard and Profile
    private static final Color BG =
            new Color(240, 240, 240);

    private static final Color CARD_BG =
            new Color(249, 251, 253);

    private static final Color BORDER =
            new Color(186, 201, 217);

    private static final Color DARK =
            new Color(36, 55, 76);

    private static final Color MUTED =
            new Color(99, 117, 136);

    private static final Color SOFT_BLUE =
            new Color(216, 231, 245);

    private final Product product;
    private final User currentUser;

    private final ReservationService reservationService =
            new ReservationService();

    private final UserDAO userDAO =
            new UserDAO();

    private final JButton reserveButton =
            new JButton("Reserve Product");

    private final JButton messageSellerButton =
            new JButton("Message Seller");

    private final JButton closeButton =
            new JButton("Close");

    private final JLabel sellerNameLabel =
            new JLabel("Loading seller...");

    private final JLabel sellerDepartmentLabel =
            new JLabel("Please wait");

    private final JLabel sellerSessionLabel =
            new JLabel("Academic Session: Not specified");

    public ProductDetailDialog(
            Window parent,
            Product product,
            User user) {

        super(
                parent,
                "Product Details",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        if (product == null || user == null) {
            throw new IllegalArgumentException(
                    "Product and logged-in user are required.");
        }

        this.product = product;
        this.currentUser = user;

        setSize(590, 600);
        setMinimumSize(
                new Dimension(490, 540));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        createInterface();
        loadSellerInfo();

        setLocationRelativeTo(parent);
    }

    // =====================================================
    // 1. CREATE INTERFACE
    // =====================================================

    private void createInterface() {

        JPanel root = new JPanel(
                new BorderLayout(0, 16));

        root.setBackground(BG);

        root.setBorder(
                new EmptyBorder(20, 22, 20, 22));

        // Page heading
        JPanel header = new JPanel();
        header.setOpaque(false);

        header.setLayout(
                new BoxLayout(
                        header,
                        BoxLayout.Y_AXIS));

        JLabel heading =
                new JLabel("Product Details");

        heading.setFont(
                new Font("Segoe UI", Font.BOLD, 23));

        heading.setForeground(DARK);

        JLabel subtitle = new JLabel(
                "Review the product information before reserving.");

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        subtitle.setForeground(MUTED);

        header.add(heading);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);

        root.add(header, BorderLayout.NORTH);

        // Main information card
        JPanel card = new JPanel();

        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS));

        card.setBackground(CARD_BG);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER, 1, true),
                        new EmptyBorder(18, 20, 18, 20)
                )
        );

        // Product title
        JTextArea productTitle =
                new JTextArea(product.getTitle());

        productTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 19));

        productTitle.setForeground(DARK);

        productTitle.setOpaque(false);
        productTitle.setEditable(false);
        productTitle.setFocusable(false);

        productTitle.setLineWrap(true);
        productTitle.setWrapStyleWord(true);
        productTitle.setRows(2);

        productTitle.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        productTitle.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 65));

        card.add(productTitle);
        card.add(Box.createVerticalStrut(5));

        // Category
        JLabel categoryLabel =
                new JLabel(product.getCategory());

        categoryLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        categoryLabel.setForeground(MUTED);

        categoryLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        card.add(categoryLabel);
        card.add(Box.createVerticalStrut(13));

        // Price and availability
        JPanel priceRow = new JPanel(
                new BorderLayout(10, 0));

        priceRow.setOpaque(false);

        JLabel priceLabel = new JLabel(
                "BDT " + product.getPrice().toPlainString());

        priceLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 22));

        priceLabel.setForeground(
                new Color(36, 76, 118));

        priceRow.add(
                priceLabel,
                BorderLayout.WEST);

        JLabel statusLabel = new JLabel(
                product.getStatus(),
                SwingConstants.CENTER
        );

        statusLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 11));

        statusLabel.setOpaque(true);

        if ("AVAILABLE".equals(product.getStatus())) {

            statusLabel.setBackground(
                    new Color(224, 241, 231));

            statusLabel.setForeground(
                    new Color(45, 115, 75));

        } else {

            statusLabel.setBackground(
                    new Color(244, 234, 215));

            statusLabel.setForeground(
                    new Color(135, 95, 40));
        }

        statusLabel.setBorder(
                new EmptyBorder(6, 10, 6, 10));

        priceRow.add(
                statusLabel,
                BorderLayout.EAST);

        priceRow.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        priceRow.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42));

        card.add(priceRow);
        card.add(Box.createVerticalStrut(17));

        // =====================================================
        // SELLER INFORMATION
        // =====================================================

        addDivider(card);
        card.add(Box.createVerticalStrut(15));

        JLabel sellerHeading =
                createSectionHeading("Seller Information");

        card.add(sellerHeading);
        card.add(Box.createVerticalStrut(12));

        JPanel sellerPanel = new JPanel(
                new BorderLayout(12, 0));

        sellerPanel.setOpaque(false);

        // Seller avatar
        JLabel avatar = new JLabel(
                "\u263A",
                SwingConstants.CENTER);

        avatar.setFont(
                new Font("Segoe UI", Font.PLAIN, 24));

        avatar.setForeground(
                new Color(36, 76, 118));

        avatar.setOpaque(true);
        avatar.setBackground(SOFT_BLUE);

        avatar.setPreferredSize(
                new Dimension(45, 45));

        sellerPanel.add(
                avatar,
                BorderLayout.WEST);

        // Seller text
        JPanel sellerText = new JPanel();

        sellerText.setOpaque(false);

        sellerText.setLayout(
                new BoxLayout(
                        sellerText,
                        BoxLayout.Y_AXIS));

        // Seller name
        sellerNameLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 14));

        sellerNameLabel.setForeground(DARK);

        // Seller department
        sellerDepartmentLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        sellerDepartmentLabel.setForeground(MUTED);

        // NEW: Academic Session styling
        sellerSessionLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        sellerSessionLabel.setForeground(MUTED);

        // Add seller details
        sellerText.add(sellerNameLabel);

        sellerText.add(
                Box.createVerticalStrut(5));

        sellerText.add(sellerDepartmentLabel);

        sellerText.add(
                Box.createVerticalStrut(4));

        sellerText.add(sellerSessionLabel);

        sellerPanel.add(
                sellerText,
                BorderLayout.CENTER);

        sellerPanel.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        // Extra height for Academic Session
        sellerPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 80));

        card.add(sellerPanel);

        card.add(Box.createVerticalStrut(15));

        addDivider(card);

        card.add(Box.createVerticalStrut(15));

        // =====================================================
        // PRODUCT DESCRIPTION
        // =====================================================

        JLabel descriptionHeading =
                createSectionHeading("Description");

        card.add(descriptionHeading);
        card.add(Box.createVerticalStrut(8));

        String descriptionText =
                product.getDescription();

        if (descriptionText == null
                || descriptionText.isBlank()) {

            descriptionText =
                    "No description provided.";
        }

        JTextArea description =
                new JTextArea(descriptionText);

        description.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));

        description.setForeground(DARK);
        description.setBackground(CARD_BG);

        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);

        description.setMargin(
                new Insets(5, 5, 5, 5));

        JScrollPane descriptionScroll =
                new JScrollPane(description);

        descriptionScroll.setBorder(
                new LineBorder(
                        new Color(220, 228, 237)));

        descriptionScroll.setPreferredSize(
                new Dimension(400, 75));

        descriptionScroll.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 95));

        descriptionScroll.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        card.add(descriptionScroll);

        root.add(card, BorderLayout.CENTER);

        // =====================================================
        // ACTION BUTTONS
        // =====================================================

        JPanel bottomPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT, 8, 0));

        bottomPanel.setOpaque(false);

        styleButton(messageSellerButton, false);
        styleButton(reserveButton, true);
        styleButton(closeButton, false);

        messageSellerButton.addActionListener(
                e -> openMessageSeller());

        reserveButton.addActionListener(
                e -> reserveProduct());

        closeButton.addActionListener(
                e -> dispose());

        // Reservation restriction
        if (!"AVAILABLE".equals(product.getStatus())
                || product.getSellerId()
                        == currentUser.getUserId()) {

            reserveButton.setEnabled(false);
        }

        // Sellers cannot message themselves
        if (product.getSellerId()
                == currentUser.getUserId()) {

            messageSellerButton.setEnabled(false);
        }

        bottomPanel.add(messageSellerButton);
        bottomPanel.add(reserveButton);
        bottomPanel.add(closeButton);

        root.add(
                bottomPanel,
                BorderLayout.SOUTH);

        setContentPane(root);
    }

    // =====================================================
    // 2. SECTION HEADING
    // =====================================================

    private JLabel createSectionHeading(String title) {

        JLabel label = new JLabel(title);

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 14));

        label.setForeground(DARK);

        label.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        return label;
    }

    // =====================================================
    // 3. DIVIDER
    // =====================================================

    private void addDivider(JPanel panel) {

        JSeparator separator = new JSeparator();

        separator.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        separator.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 1));

        panel.add(separator);
    }

    // =====================================================
    // 4. BUTTON STYLING
    // =====================================================

    private void styleButton(
            JButton button,
            boolean primary) {

        button.setFont(
                new Font(
                        "Segoe UI",
                        primary ? Font.BOLD : Font.PLAIN,
                        12));

        button.setFocusPainted(false);

        button.setBackground(
                primary ? SOFT_BLUE : CARD_BG);

        button.setForeground(
                new Color(36, 76, 118));

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER),
                        new EmptyBorder(9, 13, 9, 13)
                )
        );
    }

    // =====================================================
    // 5. LOAD SELLER INFORMATION
    // =====================================================

    private void loadSellerInfo() {

        SwingWorker<SellerInfo, Void> worker =
                new SwingWorker<>() {

            @Override
            protected SellerInfo doInBackground()
                    throws Exception {

                return userDAO.findSellerInfoById(
                        product.getSellerId());
            }

            @Override
            protected void done() {

                try {

                    SellerInfo info = get();

                    if (info == null) {

                        sellerNameLabel.setText(
                                "Seller unavailable");

                        sellerDepartmentLabel.setText(
                                "No account information found");

                        sellerSessionLabel.setText(
                                "Academic Session: Not specified");

                        return;
                    }

                    String name =
                            info.getFullName();

                    String department =
                            info.getDepartment();

                    String session =
                            info.getAcademicSession();

                    sellerNameLabel.setText(
                            name == null || name.isBlank()
                                    ? "Student"
                                    : name);

                    sellerDepartmentLabel.setText(
                            department == null
                                    || department.isBlank()
                                    ? "Department not specified"
                                    : department);

                    // Academic Session is optional
                    sellerSessionLabel.setText(
                            "Academic Session: "
                            + (session == null || session.isBlank()
                                    ? "Not specified"
                                    : session)
                    );

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    sellerNameLabel.setText(
                            "Unable to load seller");

                    sellerDepartmentLabel.setText("");

                    sellerSessionLabel.setText(
                            "Academic Session: Unavailable");

                } catch (ExecutionException ex) {

                    sellerNameLabel.setText(
                            "Unable to load seller");

                    sellerDepartmentLabel.setText("");

                    sellerSessionLabel.setText(
                            "Academic Session: Unavailable");
                }
            }
        };

        worker.execute();
    }

    // =====================================================
    // 6. RESERVE PRODUCT
    // =====================================================

    private void reserveProduct() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Do you want to reserve this product?\n"
                + product.getTitle(),
                "Confirm Reservation",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        reserveButton.setEnabled(false);

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                return reservationService.reserveProduct(
                        product.getProductId(),
                        currentUser.getUserId()
                );
            }

            @Override
            protected void done() {

                try {

                    boolean success = get();

                    if (success) {

                        JOptionPane.showMessageDialog(
                                ProductDetailDialog.this,
                                "Product reserved successfully!\n"
                                + "Check My Reservations."
                        );

                    } else {

                        JOptionPane.showMessageDialog(
                                ProductDetailDialog.this,
                                "Reservation failed.\n"
                                + "This product may already "
                                + "be reserved or unavailable.",
                                "Reservation Failed",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }

                    dispose();

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    reserveButton.setEnabled(true);

                    JOptionPane.showMessageDialog(
                            ProductDetailDialog.this,
                            "Reservation was interrupted.");

                } catch (ExecutionException ex) {

                    reserveButton.setEnabled(true);

                    JOptionPane.showMessageDialog(
                            ProductDetailDialog.this,
                            "Unable to reserve product. "
                            + "Please try again.",
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    // =====================================================
    // 7. MESSAGE SELLER
    // =====================================================

    private void openMessageSeller() {

        if (currentUser.getUserId()
                == product.getSellerId()) {

            JOptionPane.showMessageDialog(
                    this,
                    "You cannot message yourself.");

            return;
        }

        MessageDialog dialog =
                new MessageDialog(
                        this,
                        currentUser,
                        product.getProductId(),
                        product.getSellerId(),
                        product.getTitle()
                );

        dialog.setVisible(true);
    }
}
