
package campusmarketplace.ui;

import campusmarketplace.model.User;
import campusmarketplace.service.ProfileService;

import java.awt.*;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class ProfilePanel extends JPanel {

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
    private final Runnable onProfileUpdated;

    private final ProfileService profileService =
            new ProfileService();

    private final JTextField nameField =
            new JTextField(24);

    private final JTextField studentIdField =
            new JTextField(24);

    private final JTextField emailField =
            new JTextField(24);

    private final JTextField departmentField =
            new JTextField(24);

    // Optional Academic Session
    private final JTextField academicSessionField =
            new JTextField(24);

    private final JButton saveButton =
            new JButton("Save Changes");

    private final JButton changePasswordButton =
            new JButton("Change Password");

    private final JLabel statusLabel =
            new JLabel("Student ID and Email are read-only.");

    public ProfilePanel(
            User user,
            Runnable onProfileUpdated) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "Login required.");
        }

        this.currentUser = user;
        this.onProfileUpdated = onProfileUpdated;

        setLayout(new BorderLayout());
        setBackground(BG);

        setBorder(
                new EmptyBorder(0, 10, 5, 10));

        createInterface();
        loadProfile();
    }

    // =====================================================
    // 1. CREATE PROFILE INTERFACE
    // =====================================================

    private void createInterface() {

        JPanel page = new JPanel();
        page.setOpaque(false);

        page.setLayout(
                new BoxLayout(page, BoxLayout.Y_AXIS));

        // Page heading
        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);

        headingPanel.setLayout(
                new BoxLayout(
                        headingPanel,
                        BoxLayout.Y_AXIS));

        headingPanel.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        JLabel heading =
                new JLabel("My Profile");

        heading.setFont(
                new Font("Segoe UI", Font.BOLD, 23));

        heading.setForeground(DARK);

        JLabel subtitle = new JLabel(
                "Manage your personal information "
                + "and account security.");

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        subtitle.setForeground(MUTED);

        headingPanel.add(heading);
        headingPanel.add(
                Box.createVerticalStrut(6));
        headingPanel.add(subtitle);

        page.add(headingPanel);
        page.add(Box.createVerticalStrut(10));

        // Personal Information card
        page.add(createPersonalCard());

        page.add(Box.createVerticalStrut(8));

        // Account Security card
        page.add(createSecurityCard());

        // Keep everything near the top
        add(page, BorderLayout.NORTH);
    }

    // =====================================================
    // 2. PERSONAL INFORMATION CARD
    // =====================================================

    private JPanel createPersonalCard() {

        JPanel card = new JPanel(
                new GridBagLayout());

        styleCard(card);

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 4, 0);

        JLabel title =
                new JLabel("Personal Information");

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 16));

        title.setForeground(DARK);

        card.add(title, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 10, 0);

        JLabel description = new JLabel(
                "Update your details. "
                + "Academic Session is optional.");

        description.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        description.setForeground(MUTED);

        card.add(description, gbc);

        // Style fields
        styleField(nameField, false);
        styleField(studentIdField, true);
        styleField(emailField, true);
        styleField(departmentField, false);
        styleField(academicSessionField, false);

        // Short hint for Academic Session
        academicSessionField.setToolTipText(
                "Optional | Example: 2024-25");

        // Form fields
        addFieldRow(
                card, 2, "Full Name", nameField);

        addFieldRow(
                card, 3, "Student ID", studentIdField);

        addFieldRow(
                card, 4, "Email Address", emailField);

        addFieldRow(
                card, 5, "Department", departmentField);

        addFieldRow(
                card, 6, "Academic Session",
                academicSessionField);

        // Save button
        styleButton(saveButton, true);

        JPanel actions = new JPanel(
                new BorderLayout(10, 0));

        actions.setOpaque(false);

        statusLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 11));

        statusLabel.setForeground(MUTED);

        actions.add(
                statusLabel,
                BorderLayout.CENTER);

        actions.add(
                saveButton,
                BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 0, 0);

        card.add(actions, gbc);

        saveButton.addActionListener(
                e -> saveProfile());

        return card;
    }

    // =====================================================
    // 3. ACCOUNT SECURITY CARD
    // =====================================================

    private JPanel createSecurityCard() {

        JPanel card = new JPanel(
                new BorderLayout(15, 0));

        styleCard(card);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);

        textPanel.setLayout(
                new BoxLayout(
                        textPanel,
                        BoxLayout.Y_AXIS));

        JLabel heading =
                new JLabel("Account Security");

        heading.setFont(
                new Font("Segoe UI", Font.BOLD, 15));

        heading.setForeground(DARK);

        JLabel description = new JLabel(
                "Keep your account secure.");

        description.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        description.setForeground(MUTED);

        textPanel.add(heading);
        textPanel.add(
                Box.createVerticalStrut(6));
        textPanel.add(description);

        card.add(
                textPanel,
                BorderLayout.CENTER);

        styleButton(changePasswordButton, false);

        card.add(
                changePasswordButton,
                BorderLayout.EAST);

        changePasswordButton.addActionListener(e -> {

            Window parent =
                    SwingUtilities.getWindowAncestor(this);

            ChangePasswordDialog dialog =
                    new ChangePasswordDialog(
                            parent,
                            currentUser);

            dialog.setVisible(true);
        });

        return card;
    }

    // =====================================================
    // 4. CARD STYLING
    // =====================================================

    private void styleCard(JPanel card) {

        card.setBackground(CARD_BG);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER, 1, true),
                        new EmptyBorder(10, 18, 10, 18)
                )
        );

        card.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        card.setMaximumSize(
                new Dimension(680, 390));
    }

    // =====================================================
    // 5. ADD FORM ROW
    // =====================================================

    private void addFieldRow(
            JPanel panel,
            int row,
            String labelText,
            JTextField field) {

        GridBagConstraints gbc =
                new GridBagConstraints();

        JLabel label =
                new JLabel(labelText);

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 12));

        label.setForeground(DARK);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 7, 16);

        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 7, 0);

        panel.add(field, gbc);
    }

    // =====================================================
    // 6. FIELD STYLING
    // =====================================================

    private void styleField(
            JTextField field,
            boolean readOnly) {

        field.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));

        field.setForeground(
                readOnly ? MUTED : DARK);

        field.setBackground(
                readOnly
                        ? new Color(237, 241, 245)
                        : Color.WHITE);

        field.setEditable(!readOnly);

        field.setPreferredSize(
                new Dimension(340, 32));

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(
                                readOnly
                                        ? new Color(211, 220, 230)
                                        : BORDER),
                        new EmptyBorder(5, 9, 5, 9)
                )
        );
    }

    // =====================================================
    // 7. BUTTON STYLING
    // =====================================================

    private void styleButton(
            JButton button,
            boolean primary) {

        button.setFont(
                new Font(
                        "Segoe UI",
                        primary ? Font.BOLD : Font.PLAIN,
                        12));

        button.setForeground(
                new Color(36, 76, 118));

        button.setBackground(
                primary ? SOFT_BLUE : CARD_BG);

        button.setFocusPainted(false);

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER),
                        new EmptyBorder(9, 15, 9, 15)
                )
        );
    }

    // =====================================================
    // 8. LOAD CURRENT PROFILE
    // =====================================================

    private void loadProfile() {

        nameField.setText(
                currentUser.getFullName());

        studentIdField.setText(
                currentUser.getStudentId());

        emailField.setText(
                currentUser.getEmail());

        departmentField.setText(
                currentUser.getDepartment());

        // NULL session displays as an empty field
        String session =
                currentUser.getAcademicSession();

        academicSessionField.setText(
                session == null ? "" : session);
    }

    // =====================================================
    // 9. CONTROL EDITING WHILE SAVING
    // =====================================================

    private void setBusy(boolean busy) {

        saveButton.setEnabled(!busy);
        changePasswordButton.setEnabled(!busy);

        nameField.setEditable(!busy);
        departmentField.setEditable(!busy);

        academicSessionField.setEditable(!busy);
    }

    // =====================================================
    // 10. SAVE PROFILE
    // =====================================================

    private void saveProfile() {

        final String newName =
                nameField.getText().trim();

        final String newDepartment =
                departmentField.getText().trim();

        final String newSession =
                academicSessionField.getText().trim();

        setBusy(true);

        statusLabel.setForeground(MUTED);
        statusLabel.setText("Saving changes...");

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                // Saves Name, Department and Session
                return profileService.updateProfile(
                        currentUser.getUserId(),
                        newName,
                        newDepartment,
                        newSession
                );
            }

            @Override
            protected void done() {

                try {

                    boolean updated = get();

                    if (updated) {

                        currentUser.setFullName(
                                newName);

                        currentUser.setDepartment(
                                newDepartment);

                        // Empty Session becomes NULL
                        String savedSession =
                                newSession.isEmpty()
                                        ? null
                                        : newSession;

                        currentUser.setAcademicSession(
                                savedSession);

                        academicSessionField.setText(
                                savedSession == null
                                        ? ""
                                        : savedSession);

                        // Refresh header/dashboard name
                        if (onProfileUpdated != null) {
                            onProfileUpdated.run();
                        }

                        statusLabel.setForeground(
                                new Color(45, 120, 80));

                        statusLabel.setText(
                                "Profile updated successfully!");

                        JOptionPane.showMessageDialog(
                                ProfilePanel.this,
                                "Your profile has been updated.",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                    } else {

                        showError(
                                "No changes were saved.");
                    }

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    showError(
                            "Operation interrupted.");

                } catch (ExecutionException ex) {

                    Throwable cause = ex.getCause();

                    if (cause instanceof
                            IllegalArgumentException) {

                        showError(cause.getMessage());

                    } else {

                        showError(
                                "Unable to update profile.");
                    }

                } finally {

                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    // =====================================================
    // 11. DISPLAY ERRORS
    // =====================================================

    private void showError(String message) {

        statusLabel.setForeground(
                new Color(170, 60, 60));

        statusLabel.setText(message);

        JOptionPane.showMessageDialog(
                this,
                message,
                "Profile Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
