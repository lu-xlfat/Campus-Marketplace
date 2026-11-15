
package campusmarketplace.ui;

import campusmarketplace.service.AuthService;
import campusmarketplace.model.User;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;

public class RegisterFrame extends JFrame {

    private final JTextField nameField =
            new JTextField(20);

    private final JTextField studentIdField =
            new JTextField(20);

    private final JTextField emailField =
            new JTextField(20);

    private final JTextField departmentField =
            new JTextField(20);

    private final JTextField academicSessionField =
            new JTextField(20);

    // Faded hint for optional Academic Session
    private final JLabel academicSessionHint =
            new JLabel("Optional | Example: 2024-25");

    private final JPasswordField passwordField =
            new JPasswordField(20);

    private final JPasswordField confirmPasswordField =
            new JPasswordField(20);

    private final JButton registerButton =
            new JButton("Create Account");

    private final JButton backButton =
            new JButton("Back to Login");

    private final AuthService authService =
            new AuthService();

    public RegisterFrame() {

        setTitle("Campus Marketplace - Student Registration");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(500, 600);
        setMinimumSize(new Dimension(480, 560));

        setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridBagLayout());

        form.setBorder(
                BorderFactory.createEmptyBorder(
                        20, 30, 20, 30));

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Registration heading
        JLabel title = new JLabel(
                "Campus Marketplace - Register",
                SwingConstants.CENTER
        );

        title.setFont(
                new Font("SansSerif", Font.BOLD, 20));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        form.add(title, gbc);

        gbc.gridwidth = 1;

        // Registration fields
        addRow(form, gbc, 1,
                "Full Name:", nameField);

        addRow(form, gbc, 2,
                "Student ID:", studentIdField);

        addRow(form, gbc, 3,
                "Email:", emailField);

        addRow(form, gbc, 4,
                "Department:", departmentField);

        addRow(form, gbc, 5,
                "Academic Session:", academicSessionField);

        // Faded hint below Academic Session
        academicSessionHint.setFont(
                new Font("SansSerif", Font.PLAIN, 11));

        academicSessionHint.setForeground(
                new Color(145, 150, 158));

        academicSessionField.setToolTipText(
                "Optional. Example: 2024-25");

        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.insets = new Insets(0, 7, 5, 7);

        form.add(academicSessionHint, gbc);

        // Restore normal spacing
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.anchor = GridBagConstraints.CENTER;

        // Password fields
        addRow(form, gbc, 7,
                "Password:", passwordField);

        addRow(form, gbc, 8,
                "Confirm Password:", confirmPasswordField);

        // Registration buttons
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.weightx = 1;

        form.add(registerButton, gbc);

        gbc.gridy = 10;
        form.add(backButton, gbc);

        // Button actions
        registerButton.addActionListener(
                e -> registerStudent());

        backButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        getRootPane().setDefaultButton(registerButton);

        add(form);
    }

    private void addRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            JComponent field) {

        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridwidth = 1;

        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;

        panel.add(field, gbc);
    }

    private void registerStudent() {

        // Academic Session is OPTIONAL
        final String academicSession =
                academicSessionField.getText().trim();

        // Validate only when a session is entered
        if (!academicSession.isEmpty()) {

            // Check 1: Valid format
            if (!academicSession.matches("\\d{4}-\\d{2}")) {

                JOptionPane.showMessageDialog(
                        this,
                        "Use format: 2024-25",
                        "Invalid Session",
                        JOptionPane.WARNING_MESSAGE
                );

                academicSessionField.requestFocusInWindow();
                return;
            }

            // Check 2: Consecutive years
            int startYear = Integer.parseInt(
                    academicSession.substring(0, 4));

            int endYear = Integer.parseInt(
                    academicSession.substring(5, 7));

            if (endYear != (startYear + 1) % 100) {

                JOptionPane.showMessageDialog(
                        this,
                        "Use consecutive years, e.g. 2024-25",
                        "Invalid Session",
                        JOptionPane.WARNING_MESSAGE
                );

                academicSessionField.requestFocusInWindow();
                return;
            }
        }

        // Read passwords
        char[] password =
                passwordField.getPassword();

        char[] confirmation =
                confirmPasswordField.getPassword();

        // Validate password confirmation
        if (!Arrays.equals(password, confirmation)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Passwords do not match.",
                    "Password Mismatch",
                    JOptionPane.WARNING_MESSAGE
            );

            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');

            return;
        }

        // Capture inputs before background processing
        final String fullName =
                nameField.getText();

        final String studentId =
                studentIdField.getText();

        final String email =
                emailField.getText();

        final String department =
                departmentField.getText();

        registerButton.setEnabled(false);
        backButton.setEnabled(false);

        SwingWorker<User, Void> worker =
                new SwingWorker<>() {

            @Override
            protected User doInBackground()
                    throws Exception {

                return authService.register(
                        fullName,
                        studentId,
                        email,
                        department,
                        academicSession,
                        password
                );
            }

            @Override
            protected void done() {

                registerButton.setEnabled(true);
                backButton.setEnabled(true);

                try {

                    User user = get();

                    JOptionPane.showMessageDialog(
                            RegisterFrame.this,
                            "Registration successful!\n"
                            + "Welcome, "
                            + user.getFullName() + "!",
                            "Account Created",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    passwordField.setText("");
                    confirmPasswordField.setText("");

                } catch (Exception ex) {

                    Throwable cause =
                            ex.getCause() != null
                            ? ex.getCause()
                            : ex;

                    String message = cause.getMessage();

                    if (cause instanceof SQLException sqlEx
                            && "23000".equals(
                                    sqlEx.getSQLState())) {

                        message =
                                "Email or Student ID already exists.";
                    }

                    if (message == null
                            || message.isBlank()) {

                        message =
                                "Registration failed. Please try again.";
                    }

                    JOptionPane.showMessageDialog(
                            RegisterFrame.this,
                            message,
                            "Registration Failed",
                            JOptionPane.ERROR_MESSAGE
                    );

                } finally {

                    // Clear temporary password arrays
                    Arrays.fill(password, '\0');
                    Arrays.fill(confirmation, '\0');
                }
            }
        };

        worker.execute();
    }
}
