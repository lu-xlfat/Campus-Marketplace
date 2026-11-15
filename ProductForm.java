
package campusmarketplace.ui;

import campusmarketplace.model.Product;
import campusmarketplace.model.User;
import campusmarketplace.service.ProductService;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class ProductForm extends JDialog {

    // Colors matching Campus Marketplace
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

    private final User currentUser;
    private final Product existingProduct;

    private final ProductService productService =
            new ProductService();

    private final JTextField titleField =
            new JTextField(25);

    private final JTextArea descriptionArea =
            new JTextArea(4, 25);

    private final JComboBox<String> categoryBox =
            new JComboBox<>(new String[]{
                "Textbooks",
                "Electronics",
                "Calculators",
                "Furniture",
                "Stationery",
                "Other"
            });

    private final JTextField priceField =
            new JTextField(15);

    private final JButton saveButton =
            new JButton("Save Product");

    private final JButton cancelButton =
            new JButton("Cancel");

    private boolean saved = false;
    private boolean busy = false;

    public ProductForm(
            JFrame parent,
            User user,
            Product product) {

        super(
                parent,
                product == null
                        ? "Add Product"
                        : "Edit Product",
                true
        );

        if (user == null) {
            throw new IllegalArgumentException(
                    "Login required.");
        }

        this.currentUser = user;
        this.existingProduct = product;

        setSize(560, 550);
        setMinimumSize(new Dimension(500, 500));

        setDefaultCloseOperation(
                DO_NOTHING_ON_CLOSE);

        createForm();

        if (existingProduct != null) {
            fillExistingProduct();
        }

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {

                if (!busy) {
                    dispose();
                }
            }
        });

        setLocationRelativeTo(parent);
    }

    private void createForm() {

        JPanel root = new JPanel(
                new BorderLayout(0, 16));

        root.setBackground(BG);

        root.setBorder(
                new EmptyBorder(20, 22, 20, 22));

        JPanel header = new JPanel();

        header.setOpaque(false);

        header.setLayout(
                new BoxLayout(
                        header,
                        BoxLayout.Y_AXIS));

        String headingText =
                existingProduct == null
                        ? "Add New Product"
                        : "Edit Product";

        String subtitleText =
                existingProduct == null
                        ? "Enter the details of the item "
                          + "you want to sell."
                        : "Update the information "
                          + "of your listed product.";

        JLabel heading =
                new JLabel(headingText);

        heading.setFont(
                new Font("Segoe UI", Font.BOLD, 23));

        heading.setForeground(DARK);

        heading.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        JLabel subtitle =
                new JLabel(subtitleText);

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        subtitle.setForeground(MUTED);

        subtitle.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        header.add(heading);

        header.add(
                Box.createVerticalStrut(6));

        header.add(subtitle);

        root.add(header, BorderLayout.NORTH);

        // -----------------------------
        // Product information card
        // -----------------------------
        JPanel formCard =
                new JPanel(new GridBagLayout());

        formCard.setBackground(CARD_BG);

        formCard.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(
                                BORDER, 1, true),
                        new EmptyBorder(
                                18, 20, 18, 20)
                )
        );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        gbc.anchor =
                GridBagConstraints.NORTHWEST;

        // Product title
        styleTextField(titleField);

        titleField.setToolTipText(
                "Enter a clear title for your product.");

        gbc.gridy = 0;
        gbc.insets =
                new Insets(0, 0, 15, 0);

        formCard.add(
                createFieldGroup(
                        "Product Title",
                        titleField),
                gbc
        );

        // Description
        descriptionArea.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));

        descriptionArea.setForeground(DARK);
        descriptionArea.setBackground(Color.WHITE);

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        descriptionArea.setMargin(
                new Insets(8, 10, 8, 10));

        JScrollPane descriptionScroll =
                new JScrollPane(descriptionArea);

        descriptionScroll.setBorder(
                new LineBorder(BORDER));

        descriptionScroll.setPreferredSize(
                new Dimension(200, 105));
        
        
        descriptionScroll.setMinimumSize(
               new Dimension(200, 105));


        descriptionScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        gbc.gridy = 1;

        formCard.add(
                createFieldGroup(
                        "Description",
                        descriptionScroll),
                gbc
        );

  
        // Category and price
        
        categoryBox.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));

        categoryBox.setBackground(Color.WHITE);

        categoryBox.setPreferredSize(
                new Dimension(150, 34));

        styleTextField(priceField);

        priceField.setToolTipText(
                "Enter the selling price in BDT.");

        JPanel detailsRow = new JPanel(
                new GridLayout(1, 2, 14, 0));

        detailsRow.setOpaque(false);

        detailsRow.add(
                createFieldGroup(
                        "Category",
                        categoryBox));

        detailsRow.add(
                createFieldGroup(
                        "Price (BDT)",
                        priceField));

        gbc.gridy = 2;
        gbc.insets =
                new Insets(0, 0, 0, 0);

        formCard.add(detailsRow, gbc);

        // Keep the form at its natural height
        // so the description field remains visible
        JPanel formHolder =
                 new JPanel(new BorderLayout());

        formHolder.setOpaque(false);

        formHolder.add(
                formCard,
                BorderLayout.NORTH);

        root.add(
                formHolder,
                BorderLayout.CENTER);


      
        // Action buttons
        JPanel buttonPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT, 10, 0));

        buttonPanel.setOpaque(false);

        styleButton(cancelButton, false);
        styleButton(saveButton, true);

        cancelButton.setPreferredSize(
                new Dimension(105, 36));

        saveButton.setPreferredSize(
                new Dimension(145, 36));

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        root.add(
                buttonPanel,
                BorderLayout.SOUTH);

        // -----------------------------
        // Button actions
        // -----------------------------
        saveButton.addActionListener(
                e -> saveProduct());

        cancelButton.addActionListener(e -> {

            if (!busy) {
                dispose();
            }
        });

        getRootPane().setDefaultButton(saveButton);

        setContentPane(root);
    }

    // Create a label above a form component
    private JPanel createFieldGroup(
            String labelText,
            JComponent component) {

        JPanel group = new JPanel(
                new BorderLayout(0, 7));

        group.setOpaque(false);

        JLabel label = new JLabel(labelText);

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 12));

        label.setForeground(DARK);

        group.add(
                label,
                BorderLayout.NORTH);

        group.add(
                component,
                BorderLayout.CENTER);

        return group;
    }

    private void styleTextField(
            JTextField field) {

        field.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));

        field.setForeground(DARK);
        field.setBackground(Color.WHITE);

        field.setPreferredSize(
                new Dimension(150, 34));

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER),
                        new EmptyBorder(
                                6, 10, 6, 10)
                )
        );
    }

    private void styleButton(
            JButton button,
            boolean primary) {

        button.setFont(
                new Font(
                        "Segoe UI",
                        primary
                                ? Font.BOLD
                                : Font.PLAIN,
                        12));

        button.setFocusPainted(false);

        button.setBackground(
                primary
                        ? SOFT_BLUE
                        : CARD_BG);

        button.setForeground(
                primary
                        ? new Color(36, 76, 118)
                        : DARK);

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER),
                        new EmptyBorder(
                                8, 15, 8, 15)
                )
        );
    }

    // Load current data when editing
    private void fillExistingProduct() {

        titleField.setText(
                existingProduct.getTitle());

        descriptionArea.setText(
                existingProduct.getDescription());

        categoryBox.setSelectedItem(
                existingProduct.getCategory());

        priceField.setText(
                existingProduct.getPrice()
                        .toPlainString());
    }

    private void setBusy(boolean value) {

        busy = value;

        titleField.setEnabled(!value);
        descriptionArea.setEnabled(!value);
        categoryBox.setEnabled(!value);
        priceField.setEnabled(!value);

        saveButton.setEnabled(!value);
        cancelButton.setEnabled(!value);
    }

    // Existing product save functionality
    private void saveProduct() {

        if (busy) {
            return;
        }

        final String title =
                titleField.getText();

        final String description =
                descriptionArea.getText();

        final String category =
                (String) categoryBox.getSelectedItem();

        final String price =
                priceField.getText();

        setBusy(true);

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                if (existingProduct == null) {

                    return productService.addProduct(
                            currentUser.getUserId(),
                            title,
                            description,
                            category,
                            price
                    );
                }

                return productService.updateProduct(
                        existingProduct.getProductId(),
                        currentUser.getUserId(),
                        title,
                        description,
                        category,
                        price
                );
            }

            @Override
            protected void done() {

                try {

                    boolean success = get();

                    if (success) {

                        saved = true;

                        JOptionPane.showMessageDialog(
                                ProductForm.this,
                                existingProduct == null
                                        ? "Product added successfully!"
                                        : "Product updated successfully!"
                        );

                        dispose();

                    } else {

                        JOptionPane.showMessageDialog(
                                ProductForm.this,
                                "Unable to save product. "
                                + "It may have been reserved "
                                + "or removed.",
                                "Save Failed",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    JOptionPane.showMessageDialog(
                            ProductForm.this,
                            "Operation interrupted."
                    );

                } catch (ExecutionException ex) {

                    Throwable cause = ex.getCause();

                    JOptionPane.showMessageDialog(
                            ProductForm.this,
                            cause instanceof
                                    IllegalArgumentException
                                    ? cause.getMessage()
                                    : "Database operation failed.",
                            "Save Failed",
                            JOptionPane.ERROR_MESSAGE
                    );

                } finally {

                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    // Used by MyListingsPanel after closing the form
    public boolean isSaved() {
        return saved;
    }
}
