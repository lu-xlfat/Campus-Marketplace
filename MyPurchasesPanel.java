
package campusmarketplace.ui;

import campusmarketplace.model.Product;
import campusmarketplace.model.User;
import campusmarketplace.service.ReservationService;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MyPurchasesPanel extends JPanel {

    private final User currentUser;

    private final ReservationService reservationService =
            new ReservationService();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{
                        "Product ID",
                        "Title",
                        "Category",
                        "Price (BDT)",
                        "Status"
                    }, 0) {

                @Override
                public boolean isCellEditable(
                        int row, int column) {
                    return false;
                }
            };

    private final JTable purchasesTable =
            new JTable(tableModel);

    private final JButton refreshButton =
            new JButton("Refresh");

    private final JLabel statusLabel =
            new JLabel("Loading purchases...");

    private boolean busy = false;

    public MyPurchasesPanel(User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "Login required.");
        }

        this.currentUser = user;

        setLayout(new BorderLayout(10, 10));

        createInterface();
        loadPurchases();
    }

    private void createInterface() {

        JLabel heading =
                new JLabel("My Purchases");

        heading.setFont(
                new Font("SansSerif", Font.BOLD, 22));

        add(heading, BorderLayout.NORTH);

        purchasesTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        purchasesTable.setRowHeight(28);
        purchasesTable.setAutoCreateRowSorter(true);
        
        // Hide the internal Product ID from students
        purchasesTable.removeColumn(
             purchasesTable.getColumn("Product ID"));


        add(new JScrollPane(purchasesTable),
                BorderLayout.CENTER);

        JPanel bottomPanel =
                new JPanel(new BorderLayout());

        JPanel buttonPanel =
                new JPanel(new FlowLayout(FlowLayout.LEFT));

        buttonPanel.add(refreshButton);

        bottomPanel.add(
                buttonPanel, BorderLayout.NORTH);

        bottomPanel.add(
                statusLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(
                e -> loadPurchases());
    }

    private void setBusy(boolean value) {

        busy = value;
        refreshButton.setEnabled(!value);
    }

    public void loadPurchases() {

        if (busy) {
            return;
        }

        setBusy(true);
        statusLabel.setText("Loading purchases...");

        SwingWorker<List<Product>, Void> worker =
                new SwingWorker<>() {

            @Override
            protected List<Product> doInBackground()
                    throws Exception {

                return reservationService.getMyPurchases(
                        currentUser.getUserId());
            }

            @Override
            protected void done() {

                try {

                    List<Product> purchases = get();

                    tableModel.setRowCount(0);

                    for (Product product : purchases) {

                        tableModel.addRow(new Object[]{
                            product.getProductId(),
                            product.getTitle(),
                            product.getCategory(),
                            product.getPrice(),
                            product.getStatus()
                        });
                    }

                    statusLabel.setText(
                            purchases.size()
                            + " purchased item(s) found.");

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    showError("Loading interrupted.");

                } catch (ExecutionException ex) {

                    showError(
                            "Unable to load purchased products.");

                } finally {

                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    private void showError(String message) {

        statusLabel.setText(message);

        JOptionPane.showMessageDialog(
                this,
                message,
                "Purchases Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
