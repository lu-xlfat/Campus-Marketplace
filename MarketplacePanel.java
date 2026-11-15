
package campusmarketplace.ui;

import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import campusmarketplace.model.Product;
import campusmarketplace.model.User;
import campusmarketplace.service.ProductService;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MarketplacePanel extends JPanel {

    private final User currentUser;
    private final ProductService productService = new ProductService();

    private final JTextField searchField = new JTextField(18);

    private final JComboBox<String> categoryBox =
            new JComboBox<>(new String[]{
                "All Categories",
                "Textbooks",
                "Electronics",
                "Calculators",
                "Furniture",
                "Stationery",
                "Other"
            });

    private final JComboBox<String> sortBox =
            new JComboBox<>(new String[]{
                "Newest First",
                "Price: Low to High",
                "Price: High to Low",
                "Title: A to Z"
            });

    private final JButton searchButton =
            new JButton("Search");

    private final JButton refreshButton =
            new JButton("Refresh");

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{
                        "ID", "Title", "Category",
                        "Price (BDT)", "Status"
                    }, 0) {

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable productTable = new JTable(tableModel);

    private final JLabel resultLabel =
            new JLabel("Loading products...");

    private int latestRequest = 0;
    
    private List<Product> displayedProducts = new ArrayList<>();

    public MarketplacePanel(User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "A logged-in student is required.");
        }

        this.currentUser = user;

        setLayout(new BorderLayout(10, 10));

        createSearchPanel();
        createTablePanel();

        loadProducts();
    }

    private void createSearchPanel() {

        JPanel topPanel = new JPanel(new BorderLayout(5, 10));

        JLabel heading = new JLabel("Campus Marketplace");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));

        topPanel.add(heading, BorderLayout.NORTH);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));

        filters.add(new JLabel("Search:"));
        filters.add(searchField);

        filters.add(new JLabel("Category:"));
        filters.add(categoryBox);

        filters.add(new JLabel("Sort:"));
        filters.add(sortBox);

        filters.add(searchButton);
        filters.add(refreshButton);

        topPanel.add(filters, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        searchButton.addActionListener(e -> loadProducts());
        refreshButton.addActionListener(e -> loadProducts());

        searchField.addActionListener(e -> loadProducts());

        categoryBox.addActionListener(e -> loadProducts());
        sortBox.addActionListener(e -> loadProducts());
    }

    private void createTablePanel() {

        productTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        productTable.setRowHeight(28);
        productTable.setAutoCreateRowSorter(true);
        
        // Hide the internal Product ID from students
        productTable.removeColumn(productTable.getColumn("ID"));
        
        productTable.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2
                && SwingUtilities.isLeftMouseButton(e)) {
            openSelectedProduct();
            }
            }
        });

        add(new JScrollPane(productTable), BorderLayout.CENTER);

        JPanel bottomPanel =
                new JPanel(new GridLayout(1, 1));

        bottomPanel.add(resultLabel);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void loadProducts() {

        final int requestId = ++latestRequest;

        final String keyword = searchField.getText();
        final String category =
                (String) categoryBox.getSelectedItem();
        final String sortOption =
                (String) sortBox.getSelectedItem();

        searchButton.setEnabled(false);
        refreshButton.setEnabled(false);

        resultLabel.setText("Loading products...");

        SwingWorker<List<Product>, Void> worker =
                new SwingWorker<>() {

            @Override
            protected List<Product> doInBackground()
                    throws Exception {

                return productService.searchMarketplace(
                        currentUser.getUserId(),
                        keyword,
                        category,
                        sortOption
                );
            }

            @Override
            protected void done() {

                if (requestId != latestRequest) {
                    return;
                }

                searchButton.setEnabled(true);
                refreshButton.setEnabled(true);

                try {

                    List<Product> products = get();
                    
                    displayedProducts = products;

                    tableModel.setRowCount(0);

                    for (Product product : products) {

                        tableModel.addRow(new Object[]{
                            product.getProductId(),
                            product.getTitle(),
                            product.getCategory(),
                            product.getPrice(),
                            product.getStatus()
                        });
                    }

                    resultLabel.setText(
                            products.size() + " product(s) found.");

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();
                    resultLabel.setText("Loading interrupted.");

                } catch (ExecutionException ex) {

                    resultLabel.setText("Unable to load products.");

                    JOptionPane.showMessageDialog(
                            MarketplacePanel.this,
                            "Could not load marketplace products.",
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }
    
private void openSelectedProduct() {

    int selectedRow = productTable.getSelectedRow();

    if (selectedRow == -1) {
        return;
    }

    int modelRow =
            productTable.convertRowIndexToModel(selectedRow);

    int productId =
            (Integer) tableModel.getValueAt(modelRow, 0);

    Product selectedProduct = displayedProducts.stream()
            .filter(p -> p.getProductId() == productId)
            .findFirst()
            .orElse(null);

    if (selectedProduct == null) {
        JOptionPane.showMessageDialog(
                this, "Product details not found.");
        return;
    }

    Window parent = SwingUtilities.getWindowAncestor(this);

    
ProductDetailDialog dialog =
        new ProductDetailDialog(
                parent, selectedProduct, currentUser);

dialog.setVisible(true);

// Reload the marketplace after closing the dialog
loadProducts();

}

}

