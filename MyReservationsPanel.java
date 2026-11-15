
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

public class MyReservationsPanel extends JPanel {

    private final User currentUser;

    private final ReservationService reservationService =
            new ReservationService();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{
                        "Product ID", "Title", "Category",
                        "Price (BDT)", "Status"
                    }, 0) {

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable reservationTable =
            new JTable(tableModel);

    private final JButton cancelButton =
            new JButton("Cancel Reservation");

    private final JButton refreshButton =
            new JButton("Refresh");

    private final JLabel resultLabel =
            new JLabel("Loading reservations...");

    private boolean busy = false;

    public MyReservationsPanel(User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "Login required.");
        }

        this.currentUser = user;

        setLayout(new BorderLayout(10, 10));

        JLabel heading =
                new JLabel("My Reservations");

        heading.setFont(
                new Font("SansSerif", Font.BOLD, 22));

        add(heading, BorderLayout.NORTH);

        reservationTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        reservationTable.setRowHeight(28);
        reservationTable.setAutoCreateRowSorter(true);
        
        // Hide internal Product ID from students
        reservationTable.removeColumn(
               reservationTable.getColumn("Product ID"));

        add(new JScrollPane(reservationTable),
                BorderLayout.CENTER);

        JPanel bottomPanel =
                new JPanel(new BorderLayout());

        JPanel buttons =
                new JPanel(new FlowLayout(FlowLayout.LEFT));

        buttons.add(cancelButton);
        buttons.add(refreshButton);

        bottomPanel.add(buttons, BorderLayout.NORTH);
        bottomPanel.add(resultLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(
                e -> cancelSelectedReservation());

        refreshButton.addActionListener(
                e -> loadReservations());

        loadReservations();
    }

    private void setBusy(boolean value) {
        busy = value;

        cancelButton.setEnabled(!value);
        refreshButton.setEnabled(!value);
    }

    public void loadReservations() {

        if (busy) {
            return;
        }

        setBusy(true);
        resultLabel.setText("Loading reservations...");

        SwingWorker<List<Product>, Void> worker =
                new SwingWorker<>() {

            @Override
            protected List<Product> doInBackground()
                    throws Exception {

                return reservationService.getMyReservations(
                        currentUser.getUserId());
            }

            @Override
            protected void done() {

                try {
                    List<Product> products = get();

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
                            products.size()
                            + " active reservation(s).");

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    showError("Loading interrupted.");

                } catch (ExecutionException ex) {

                    showError(
                            "Unable to load reservations.");

                } finally {
                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    private void cancelSelectedReservation() {

        if (busy) {
            return;
        }

        int selectedRow =
                reservationTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a reservation first.");

            return;
        }

        int modelRow =
                reservationTable.convertRowIndexToModel(
                        selectedRow);

        int productId =
                (Integer) tableModel.getValueAt(
                        modelRow, 0);

        String productTitle =
                (String) tableModel.getValueAt(
                        modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Cancel reservation for:\n"
                        + productTitle + "?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setBusy(true);

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                return reservationService.cancelReservation(
                        productId,
                        currentUser.getUserId());
            }

            @Override
            protected void done() {

                try {
                    boolean success = get();

                    if (success) {

                        JOptionPane.showMessageDialog(
                                MyReservationsPanel.this,
                                "Reservation cancelled successfully.");

                    } else {

                        JOptionPane.showMessageDialog(
                                MyReservationsPanel.this,
                                "Reservation could not be cancelled."
                                + "\nIts status may have changed.");
                    }

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();
                    showError("Cancellation interrupted.");

                } catch (ExecutionException ex) {

                    showError(
                            "Unable to cancel reservation.");

                } finally {
                    setBusy(false);
                    loadReservations();
                }
            }
        };

        worker.execute();
    }

    private void showError(String message) {

        resultLabel.setText(message);

        JOptionPane.showMessageDialog(
                this,
                message,
                "Reservation Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

}
