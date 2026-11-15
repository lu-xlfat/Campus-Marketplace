
package campusmarketplace.ui;

import campusmarketplace.service.ReservationService;

import campusmarketplace.model.Product;
import campusmarketplace.model.User;
import campusmarketplace.service.ProductService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MyListingsPanel extends JPanel {

    private final MainFrame parent;
    private final User currentUser;
    private final ProductService productService = new ProductService();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{
                        "ID", "Title", "Category", "Price (BDT)", "Status"
                    }, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable productTable = new JTable(tableModel);

    private final JButton addButton = new JButton("Add Product");
    private final JButton editButton = new JButton("Edit Product");
    private final JButton removeButton = new JButton("Remove Product");
    
    private final JButton refreshButton = new JButton("Refresh");
    
    
private final JButton cancelReservationButton =
        new JButton("Cancel Reservation");

private final JButton markSoldButton =
        new JButton("Mark as Sold");

private final ReservationService reservationService =
        new ReservationService();


    private List<Product> products = new ArrayList<>();
    private boolean operationBusy = false;

    public MyListingsPanel(MainFrame parent, User user) {
        this.parent = parent;
        this.currentUser = user;

        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel("My Product Listings");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        add(heading, BorderLayout.NORTH);

        productTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        productTable.setRowHeight(28);
        productTable.setAutoCreateRowSorter(true);
        
        // Hide Product ID from the student interface
        productTable.removeColumn(
             productTable.getColumn("ID"));

        
        
        productTable.getSelectionModel()
        .addListSelectionListener(e -> {

        if (!e.getValueIsAdjusting()) {
        updateActionButtons();
        }
        } );


        add(new JScrollPane(productTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(cancelReservationButton);
        buttonPanel.add(markSoldButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> openProductForm(null));
        editButton.addActionListener(e -> editSelectedProduct());
        removeButton.addActionListener(e -> removeSelectedProduct());
        refreshButton.addActionListener(e -> loadProducts());
        
        
cancelReservationButton.addActionListener(
        e -> cancelSelectedReservation());

markSoldButton.addActionListener(
        e -> markSelectedProductSold());

        updateActionButtons();
        loadProducts();
    }

    private void openProductForm(Product product) {
        ProductForm form = new ProductForm(parent, currentUser, product);
        form.setVisible(true);

        if (form.isSaved()) {
            loadProducts();
        }
    }

    private Product getSelectedProduct() {

        int selectedRow = productTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select a product first.");
            return null;
        }

        int modelRow = productTable.convertRowIndexToModel(selectedRow);

        int productId = (Integer) tableModel.getValueAt(modelRow, 0);

        return products.stream()
                .filter(p -> p.getProductId() == productId)
                .findFirst()
                .orElse(null);
    }

    private void editSelectedProduct() {
        Product product = getSelectedProduct();

        if (product == null) {
            return;
        }

        if (!"AVAILABLE".equals(product.getStatus())) {
            JOptionPane.showMessageDialog(
                    this, "Only available products can be edited.");
            return;
        }

        openProductForm(product);
    }

    private void removeSelectedProduct() {
        Product product = getSelectedProduct();

        if (product == null) {
            return;
        }

        if (!"AVAILABLE".equals(product.getStatus())) {
            JOptionPane.showMessageDialog(
                    this, "Only available products can be removed.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Remove product: " + product.getTitle() + "?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setButtonsEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() throws Exception {
                return productService.removeProduct(
                        product.getProductId(),
                        currentUser.getUserId()
                );
            }

            @Override
            protected void done() {
                setButtonsEnabled(true);

                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(
                                MyListingsPanel.this,
                                "Product removed successfully.");
                    } else {
                        JOptionPane.showMessageDialog(
                                MyListingsPanel.this,
                                "Could not remove this product. "
                                + "Its status may have changed.");
                    }

                    loadProducts();

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Operation interrupted.");

                } catch (ExecutionException ex) {
                    showError("Could not remove product.");
                }
            }
        };

        worker.execute();
    }

    public void loadProducts() {
        refreshButton.setEnabled(false);

        SwingWorker<List<Product>, Void> worker =
                new SwingWorker<>() {

            @Override
            protected List<Product> doInBackground() throws Exception {
                return productService.getMyProducts(
                        currentUser.getUserId()
                );
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true);

                try {
                    products = get();
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
                    
                    updateActionButtons();

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Loading interrupted.");

                } catch (ExecutionException ex) {
                    showError("Unable to load products from database.");
                }
            }
        };

        worker.execute();
    }

    
private void setButtonsEnabled(boolean enabled) {

    operationBusy = !enabled;
    updateActionButtons();
}

private void updateActionButtons() {

    // Disable all actions while an operation is processing
    if (operationBusy) {

        addButton.setEnabled(false);
        editButton.setEnabled(false);
        removeButton.setEnabled(false);
        cancelReservationButton.setEnabled(false);
        markSoldButton.setEnabled(false);
        refreshButton.setEnabled(false);
        return;
    }

    addButton.setEnabled(true);
    refreshButton.setEnabled(true);

    int selectedRow = productTable.getSelectedRow();

    if (selectedRow == -1) {

        editButton.setEnabled(false);
        removeButton.setEnabled(false);
        cancelReservationButton.setEnabled(false);
        markSoldButton.setEnabled(false);
        return;
    }

    int modelRow =
            productTable.convertRowIndexToModel(selectedRow);

    String status =
            (String) tableModel.getValueAt(modelRow, 4);

    boolean available = "AVAILABLE".equals(status);
    boolean reserved = "RESERVED".equals(status);

    editButton.setEnabled(available);
    removeButton.setEnabled(available);

    cancelReservationButton.setEnabled(reserved);

    markSoldButton.setEnabled(
            available || reserved);
}


    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    
private void cancelSelectedReservation() {

    Product product = getSelectedProduct();

    if (product == null) {
        return;
    }

    if (!"RESERVED".equals(product.getStatus())) {
        JOptionPane.showMessageDialog(
                this,
                "Only reserved products can be cancelled.");
        return;
    }

    int choice = JOptionPane.showConfirmDialog(
            this,
            "Cancel the reservation for:\n"
                    + product.getTitle() + "?",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION
    );

    if (choice != JOptionPane.YES_OPTION) {
        return;
    }

    setButtonsEnabled(false);

    SwingWorker<Boolean, Void> worker =
            new SwingWorker<>() {

        @Override
        protected Boolean doInBackground()
                throws Exception {

            return reservationService.cancelReservation(
                    product.getProductId(),
                    currentUser.getUserId()
            );
        }

        @Override
        protected void done() {

            try {
                if (get()) {
                    JOptionPane.showMessageDialog(
                            MyListingsPanel.this,
                            "Reservation cancelled successfully.");
                } else {
                    JOptionPane.showMessageDialog(
                            MyListingsPanel.this,
                            "Unable to cancel reservation. "
                            + "The product status may have changed.");
                }

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                showError("Cancellation interrupted.");

            } catch (ExecutionException ex) {
                showError("Unable to cancel reservation.");

            } finally {
                setButtonsEnabled(true);
                loadProducts();
            }
        }
    };

    worker.execute();
}

private void markSelectedProductSold() {

    Product product = getSelectedProduct();

    if (product == null) {
        return;
    }

    if (!"AVAILABLE".equals(product.getStatus())
            && !"RESERVED".equals(product.getStatus())) {

        JOptionPane.showMessageDialog(
                this,
                "Only available or reserved products "
                + "can be marked as sold.");
        return;
    }

    int choice = JOptionPane.showConfirmDialog(
            this,
            "Mark this product as SOLD?\n"
                    + product.getTitle()
                    + "\n\nThis action cannot be undone.",
            "Confirm Sale",
            JOptionPane.YES_NO_OPTION
    );

    if (choice != JOptionPane.YES_OPTION) {
        return;
    }

    setButtonsEnabled(false);

    SwingWorker<Boolean, Void> worker =
            new SwingWorker<>() {

        @Override
        protected Boolean doInBackground()
                throws Exception {

            return productService.markProductSold(
                    product.getProductId(),
                    currentUser.getUserId()
            );
        }

        @Override
        protected void done() {

            try {
                if (get()) {

                    JOptionPane.showMessageDialog(
                            MyListingsPanel.this,
                            "Product marked as SOLD successfully!");

                } else {

                    JOptionPane.showMessageDialog(
                            MyListingsPanel.this,
                            "Unable to mark product as sold. "
                            + "Its status may have changed.");
                }

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                showError("Operation interrupted.");

            } catch (ExecutionException ex) {
                showError("Unable to mark product as sold.");

            } finally {
                setButtonsEnabled(true);
                loadProducts();
            }
        }
    };

    worker.execute();
}

}
