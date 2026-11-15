
package campusmarketplace.ui;

import campusmarketplace.model.User;
import campusmarketplace.service.AuthService;
import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public class LoginFrame extends JFrame {

    private final JTextField emailField = new JTextField(22);
    private final JPasswordField passwordField =
            new JPasswordField(22);

    private final JButton loginButton = new JButton("Login");
    private final JButton registerButton =
            new JButton("Create New Account");

    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("Campus Marketplace - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 320);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(
                25, 30, 25, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel(
                "Campus Marketplace", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        gbc.gridy = 4;
        panel.add(registerButton, gbc);

        loginButton.addActionListener(e -> loginStudent());

        registerButton.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });

        add(panel);
    }

    private void loginStudent() {
        String email = emailField.getText();
        char[] password = passwordField.getPassword();

        loginButton.setEnabled(false);

        SwingWorker<User, Void> worker = new SwingWorker<>() {

            @Override
            protected User doInBackground() throws Exception {
                return authService.login(email, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                Arrays.fill(password, '\0');
                passwordField.setText("");

                try {
                    
                   User user = get();

                    if (user == null) {
                    throw new IllegalStateException(
                     "Unable to load user account.");
                }

                MainFrame mainFrame = new MainFrame(user);
                mainFrame.setVisible(true);
                LoginFrame.this.dispose();
 

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            "Login was interrupted.");

                } catch (ExecutionException ex) {
                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            ex.getCause() instanceof IllegalArgumentException
                                    ? "Invalid email or password."
                                    : "Unable to log in. Please try again.",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }
}
