
package campusmarketplace.ui;

import campusmarketplace.model.ConversationPreview;
import campusmarketplace.model.User;
import campusmarketplace.service.MessageService;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class InboxPanel extends JPanel {

    private final User currentUser;

    private final MessageService messageService =
            new MessageService();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{
                        "Product",
                        "Conversation With",
                        "Latest Message",
                        "Sent Time"
                    }, 0) {

                @Override
                public boolean isCellEditable(
                        int row, int column) {
                    return false;
                }
            };

    private final JTable inboxTable =
            new JTable(tableModel);

    private final JButton openButton =
            new JButton("Open Conversation");

    private final JButton refreshButton =
            new JButton("Refresh Inbox");

    private final JLabel statusLabel =
            new JLabel("Loading conversations...");

    private List<ConversationPreview> conversations =
            new ArrayList<>();

    private boolean busy = false;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, hh:mm a",
                    Locale.ENGLISH
            );

    public InboxPanel(User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "Login required.");
        }

        this.currentUser = user;

        setLayout(new BorderLayout(10, 10));

        createInterface();
        loadInbox();
    }

    private void createInterface() {

        JLabel heading = new JLabel("My Inbox");

        heading.setFont(
                new Font("SansSerif", Font.BOLD, 22));

        add(heading, BorderLayout.NORTH);

        inboxTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        inboxTable.setRowHeight(28);
        inboxTable.setAutoCreateRowSorter(true);

        add(new JScrollPane(inboxTable),
                BorderLayout.CENTER);

        JPanel bottomPanel =
                new JPanel(new BorderLayout());

        JPanel buttons =
                new JPanel(new FlowLayout(FlowLayout.LEFT));

        buttons.add(openButton);
        buttons.add(refreshButton);

        bottomPanel.add(buttons, BorderLayout.NORTH);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        openButton.addActionListener(
                e -> openSelectedConversation());

        refreshButton.addActionListener(
                e -> loadInbox());

        inboxTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (e.getClickCount() == 2
                        && SwingUtilities.isLeftMouseButton(e)) {

                    openSelectedConversation();
                }
            }
        });
    }

    private void setBusy(boolean value) {

        busy = value;

        openButton.setEnabled(!value);
        refreshButton.setEnabled(!value);
    }

    // Load Inbox with product titles and student names
    public void loadInbox() {

        if (busy) {
            return;
        }

        setBusy(true);
        statusLabel.setText("Loading conversations...");

        SwingWorker<List<ConversationPreview>, Void> worker =
                new SwingWorker<>() {

            @Override
            protected List<ConversationPreview> doInBackground()
                    throws Exception {

                return messageService.getInboxPreviews(
                        currentUser.getUserId());
            }

            @Override
            protected void done() {

                try {

                    List<ConversationPreview> previews = get();

                    conversations = previews;

                    tableModel.setRowCount(0);

                    for (ConversationPreview preview : previews) {

                        String messagePreview =
                                preview.getLatestMessage()
                                        .replace("\n", " ")
                                        .replace("\r", " ");

                        if (messagePreview.length() > 80) {

                            messagePreview =
                                    messagePreview.substring(0, 80)
                                    + "...";
                        }

                        String time = "";

                        if (preview.getSentTime() != null) {

                            time = preview.getSentTime()
                                    .format(TIME_FORMAT);
                        }

                        tableModel.addRow(new Object[]{
                            preview.getProductTitle(),
                            preview.getOtherUserName(),
                            messagePreview,
                            time
                        });
                    }

                    statusLabel.setText(
                            previews.size()
                            + " conversation(s) found.");

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();
                    showError("Inbox loading interrupted.");

                } catch (ExecutionException ex) {

                    showError(
                            "Unable to load Inbox.");

                } finally {

                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    // Open the selected conversation
    private void openSelectedConversation() {

        if (busy) {
            return;
        }

        int selectedRow = inboxTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a conversation first.");

            return;
        }

        int modelRow =
                inboxTable.convertRowIndexToModel(selectedRow);

        if (modelRow < 0
                || modelRow >= conversations.size()) {
            return;
        }

        ConversationPreview selectedConversation =
                conversations.get(modelRow);

        Window parent =
                SwingUtilities.getWindowAncestor(this);

        MessageDialog dialog = new MessageDialog(
                parent,
                currentUser,
                selectedConversation.getProductId(),
                selectedConversation.getOtherUserId(),
                selectedConversation.getProductTitle()
        );

        dialog.setVisible(true);

        // Refresh Inbox after the conversation closes
        loadInbox();
    }

    private void showError(String message) {

        statusLabel.setText(message);

        JOptionPane.showMessageDialog(
                this,
                message,
                "Inbox Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}

