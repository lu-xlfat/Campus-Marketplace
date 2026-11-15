
package campusmarketplace.ui;

import campusmarketplace.model.Message;
import campusmarketplace.model.User;
import campusmarketplace.service.MessageService;

import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

public class MessageDialog extends JDialog {

    private final User currentUser;
    private final int productId;
    private final int otherUserId;
    
    
    private String otherUserName;

    private final JLabel partnerLabel = new JLabel();

    private List<Message> lastLoadedMessages = List.of();


    private final MessageService messageService =
            new MessageService();

    
private final ChatHistoryPanel chatHistory =
        new ChatHistoryPanel();


    private final JTextArea messageInput =
            new JTextArea(3, 30);

    private final JButton sendButton =
            new JButton("Send Message");

    private final JButton refreshButton =
            new JButton("Refresh");

    private final JButton closeButton =
            new JButton("Close");

    private final JLabel statusLabel =
            new JLabel("Loading conversation...");

    private boolean busy = false;

    public MessageDialog(
            Window parent,
            User user,
            int productId,
            int otherUserId,
            String productTitle) {

        super(parent, "Product Conversation",
                Dialog.ModalityType.APPLICATION_MODAL);

        if (user == null) {
            throw new IllegalArgumentException(
                    "Login required.");
        }

        if (productId <= 0 || otherUserId <= 0
                || user.getUserId() == otherUserId) {
            throw new IllegalArgumentException(
                    "Invalid conversation details.");
        }

        this.currentUser = user;
        this.productId = productId;
        this.otherUserId = otherUserId;
        this.otherUserName = "Student #" + otherUserId;

        setTitle("Conversation - " + productTitle);

        setSize(650, 550);
        setMinimumSize(new Dimension(500, 420));
        setLocationRelativeTo(parent);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        createInterface(productTitle);
        loadOtherUserName();
        loadMessages();
    }

    private void createInterface(String productTitle) {

        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(
                new BorderLayout(10, 10));

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        15, 20, 15, 20));

        // Header
        JPanel header = new JPanel(
                new BorderLayout(5, 5));

        JLabel titleLabel =
                new JLabel("Product: " + productTitle);

        titleLabel.setFont(
                new Font("SansSerif", Font.BOLD, 20));

        partnerLabel.setText(
        "Conversation with: " + otherUserName);

        header.add(titleLabel, BorderLayout.NORTH);
        header.add(partnerLabel, BorderLayout.SOUTH);

        mainPanel.add(header, BorderLayout.NORTH);

       
            
        // Modern chat-bubble conversation history
        mainPanel.add(
        chatHistory,
        BorderLayout.CENTER
        );


        // Message input
        JPanel bottomPanel = new JPanel(
                new BorderLayout(5, 8));

        messageInput.setLineWrap(true);
        messageInput.setWrapStyleWord(true);
        
        
        messageInput.setFont(
        new Font("SansSerif", Font.PLAIN, 14));

        messageInput.setMargin(
        new Insets(8, 10, 8, 10));

        messageInput.setBackground(Color.WHITE);


        JScrollPane inputScroll =
                new JScrollPane(messageInput);

        bottomPanel.add(inputScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT));

        buttonPanel.add(refreshButton);
        buttonPanel.add(sendButton);
        buttonPanel.add(closeButton);

        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Button actions
        sendButton.addActionListener(
                e -> sendMessage());

        refreshButton.addActionListener(
                e -> loadMessages());

        closeButton.addActionListener(
                e -> dispose());
    }

    private void setBusy(boolean value) {

        busy = value;

        sendButton.setEnabled(!value);
        refreshButton.setEnabled(!value);
        messageInput.setEnabled(!value);
    }

    // Retrieve previous messages
    public void loadMessages() {

        if (busy) {
            return;
        }

        setBusy(true);

        statusLabel.setText(
                "Loading messages...");

        SwingWorker<List<Message>, Void> worker =
                new SwingWorker<>() {

            @Override
            protected List<Message> doInBackground()
                    throws Exception {

                return messageService.getConversation(
                        productId,
                        currentUser.getUserId(),
                        otherUserId
                );
            }

            @Override
            protected void done() {

                try {

                    List<Message> messages = get();
                    lastLoadedMessages = messages;

                    displayMessages(messages);

                    statusLabel.setText(
                            messages.size()
                            + " message(s) in conversation.");

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    showError(
                            "Message loading interrupted.");

                } catch (ExecutionException ex) {

                    showError(
                            "Unable to load conversation.");

                } finally {

                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    
    
    // Display messages using modern chat bubbles
    private void displayMessages(List<Message> messages) {

        chatHistory.showMessages(
            messages,
            currentUser.getUserId(),
            otherUserName
        );
    }


    // Send a new message
    private void sendMessage() {

        if (busy) {
            return;
        }

        String text = messageInput.getText().trim();

        if (text.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please type a message.");

            return;
        }

        if (text.length() > 2000) {

            JOptionPane.showMessageDialog(
                    this,
                    "Message cannot exceed 2000 characters.");

            return;
        }

        setBusy(true);

        statusLabel.setText("Sending message...");

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                return messageService.sendMessage(
                        currentUser.getUserId(),
                        otherUserId,
                        productId,
                        text
                );
            }

            @Override
            protected void done() {

                boolean sent = false;

                try {

                    sent = get();

                    if (sent) {

                        messageInput.setText("");

                        statusLabel.setText(
                                "Message sent successfully!");

                    } else {

                        statusLabel.setText(
                                "Message could not be sent.");

                        JOptionPane.showMessageDialog(
                                MessageDialog.this,
                                "Unable to send this message.\n"
                                + "Check the conversation "
                                + "and product status.");
                    }

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    showError("Sending interrupted.");

                } catch (ExecutionException ex) {

                    showError(
                            "Unable to send message.");

                } finally {

                    setBusy(false);
                }

                if (sent) {
                    loadMessages();
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
                "Messaging Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
    
    
private void loadOtherUserName() {

    SwingWorker<String, Void> worker =
            new SwingWorker<>() {

        @Override
        protected String doInBackground()
                throws Exception {

            return messageService.getStudentName(
                    otherUserId);
        }

        @Override
        protected void done() {

            try {
                String name = get();

                if (name != null && !name.isBlank()) {

                    otherUserName = name;

                    partnerLabel.setText(
                            "Conversation with: "
                            + otherUserName);

                    if (!lastLoadedMessages.isEmpty()) {
                        displayMessages(lastLoadedMessages);
                    }
                }

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();

            } catch (ExecutionException ex) {
                // Keep the Student #ID fallback.
            }
        }
    };

    worker.execute();
}

}
