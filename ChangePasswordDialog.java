
package campusmarketplace.ui;

import campusmarketplace.model.User;
import campusmarketplace.service.ProfileService;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ChangePasswordDialog extends JDialog {

    private final User currentUser;

    private final ProfileService profileService =
            new ProfileService();

    private final JPasswordField currentPasswordField =
            new JPasswordField(22);

    private final JPasswordField newPasswordField =
            new JPasswordField(22);

    private final JPasswordField confirmPasswordField =
            new JPasswordField(22);

    private final JButton changeButton =
            new JButton("Change Password");

    private final JButton cancelButton =
            new JButton("Cancel");

    private final JLabel statusLabel =
            new JLabel("New password must have at least 8 characters.");

    private boolean busy = false;

    public ChangePasswordDialog(
            Window parent,
            User currentUser) {

        super(
                parent,
                "Change Password",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        if (currentUser == null) {
            throw new IllegalArgumentException(
                    "Login required.");
        }

        this.currentUser = currentUser;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(460, 365);
        setMinimumSize(new Dimension(420, 340));

        createInterface();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!busy) {
                    closeDialog();
                }
            }
        });

        setLocationRelativeTo(parent);
    }

    private void createInterface() {

        JPanel mainPanel =
                new JPanel(new BorderLayout(0, 18));

        mainPanel.setBackground(
                new Color(240, 240, 240));

        mainPanel.setBorder(
                new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);

        header.setLayout(
                new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Change Password");

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 21));

        title.setForeground(
                new Color(36, 55, 76));

        JLabel subtitle = new JLabel(
                "Enter your current and new passwords.");

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        subtitle.setForeground(
                new Color(99, 117, 136));

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);

        mainPanel.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, gbc, 0,
                "Current Password:",
                currentPasswordField);

        addRow(form, gbc, 1,
                "New Password:",
                newPasswordField);

        addRow(form, gbc, 2,
                "Confirm Password:",
                confirmPasswordField);

        mainPanel.add(form, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(
                new BorderLayout(0, 10));

        footer.setOpaque(false);

        statusLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 11));

        statusLabel.setForeground(
                new Color(99, 117, 136));

        footer.add(statusLabel, BorderLayout.NORTH);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0));

        buttons.setOpaque(false);

        changeButton.setFont(
                new Font("Segoe UI", Font.BOLD, 12));

        changeButton.setBackground(
                new Color(216, 231, 245));

        changeButton.setForeground(
                new Color(36, 76, 118));

        changeButton.setFocusPainted(false);

        buttons.add(cancelButton);
        buttons.add(changeButton);

        footer.add(buttons, BorderLayout.SOUTH);

        mainPanel.add(footer, BorderLayout.SOUTH);

        add(mainPanel);

        // Actions
        changeButton.addActionListener(
                e -> changePassword());

        cancelButton.addActionListener(
                e -> closeDialog());

        getRootPane().setDefaultButton(changeButton);
    }

    private void addRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String labelText,
            JPasswordField field) {

        JLabel label = new JLabel(labelText);

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 12));

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.insets = new Insets(8, 0, 8, 12);

        panel.add(label, gbc);

        field.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));

        field.setPreferredSize(
                new Dimension(240, 32));

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(8, 0, 8, 0);

        panel.add(field, gbc);
    }

    private void setBusy(boolean value) {

        busy = value;

        currentPasswordField.setEnabled(!value);
        newPasswordField.setEnabled(!value);
        confirmPasswordField.setEnabled(!value);

        changeButton.setEnabled(!value);
        cancelButton.setEnabled(!value);
    }

    private void changePassword() {

        if (busy) {
            return;
        }

        final char[] currentPassword =
                currentPasswordField.getPassword();

        final char[] newPassword =
                newPasswordField.getPassword();

        final char[] confirmPassword =
                confirmPasswordField.getPassword();

        setBusy(true);

        statusLabel.setForeground(
                new Color(99, 117, 136));

        statusLabel.setText("Updating password...");

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                return profileService.changePassword(
                        currentUser,
                        currentPassword,
                        newPassword,
                        confirmPassword
                );
            }

            @Override
            protected void done() {

                try {

                    boolean changed = get();

                    if (changed) {

                        JOptionPane.showMessageDialog(
                                ChangePasswordDialog.this,
                                "Password changed successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        dispose();

                    } else {

                        showError(
                                "Password could not be changed. "
                                + "Please try again.");
                    }

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    showError(
                            "Password change interrupted.");

                } catch (ExecutionException ex) {

                    Throwable cause = ex.getCause();

                    if (cause instanceof
                            IllegalArgumentException) {

                        showError(cause.getMessage());

                    } else {

                        showError(
                                "Unable to change password.");
                    }

                } finally {

                    // Clear password data after processing
                    Arrays.fill(currentPassword, '\0');
                    Arrays.fill(newPassword, '\0');
                    Arrays.fill(confirmPassword, '\0');

                    currentPasswordField.setText("");
                    newPasswordField.setText("");
                    confirmPasswordField.setText("");

                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    private void showError(String message) {

        statusLabel.setForeground(
                new Color(170, 60, 60));

        statusLabel.setText(message);

        JOptionPane.showMessageDialog(
                this,
                message,
                "Change Password Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void closeDialog() {

        if (busy) {
            return;
        }

        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");

        dispose();
    }
}
