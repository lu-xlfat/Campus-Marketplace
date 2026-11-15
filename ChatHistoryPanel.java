
package campusmarketplace.ui;

import campusmarketplace.model.Message;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ChatHistoryPanel extends JScrollPane {

    private static final Color BACKGROUND =
            new Color(238, 243, 249);

    private static final Color MY_BUBBLE =
            new Color(214, 234, 255);

    private static final Color OTHER_BUBBLE =
            Color.WHITE;

    private final JPanel messagesPanel = new JPanel();

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public ChatHistoryPanel() {

        messagesPanel.setLayout(
                new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));

        messagesPanel.setBackground(BACKGROUND);

        messagesPanel.setBorder(
                new EmptyBorder(15, 15, 15, 15));

        setViewportView(messagesPanel);

        getViewport().setBackground(BACKGROUND);

        setBorder(BorderFactory.createEmptyBorder());

        setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        getVerticalScrollBar().setUnitIncrement(16);
    }

    public void showMessages(
            List<Message> messages,
            int currentUserId,
            String otherStudentName) {

        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() ->
                    showMessages(
                            messages,
                            currentUserId,
                            otherStudentName));
            return;
        }

        messagesPanel.removeAll();

        if (messages == null || messages.isEmpty()) {

            JLabel emptyLabel = new JLabel(
                    "No messages yet. Start the conversation!");

            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            messagesPanel.add(emptyLabel);

        } else {

            for (Message message : messages) {

                boolean isMine =
                        message.getSenderId() == currentUserId;

                JPanel row = new JPanel(new BorderLayout());
                row.setOpaque(false);

                row.setBorder(
                        new EmptyBorder(0, 0, 12, 0));

                row.setAlignmentX(Component.LEFT_ALIGNMENT);

                JPanel bubble = createBubble(
                        message,
                        isMine,
                        otherStudentName);

                row.add(
                        bubble,
                        isMine
                                ? BorderLayout.EAST
                                : BorderLayout.WEST);

                Dimension preferred = row.getPreferredSize();

                row.setMaximumSize(new Dimension(
                        Integer.MAX_VALUE,
                        preferred.height));

                messagesPanel.add(row);
            }
        }

        messagesPanel.revalidate();
        messagesPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private JPanel createBubble(
            Message message,
            boolean isMine,
            String otherStudentName) {

        Color bubbleColor =
                isMine ? MY_BUBBLE : OTHER_BUBBLE;

        JPanel bubble = new RoundedBubble(bubbleColor);

        bubble.setLayout(
                new BoxLayout(bubble, BoxLayout.Y_AXIS));

        bubble.setBorder(
                new EmptyBorder(10, 13, 10, 13));

        String senderName = isMine
                ? "You"
                : (otherStudentName == null
                        || otherStudentName.isBlank()
                        ? "Student"
                        : otherStudentName);

        JLabel senderLabel = new JLabel(senderName);

        senderLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 12));

        senderLabel.setForeground(
                new Color(48, 101, 160));

        senderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bubble.add(senderLabel);
        bubble.add(Box.createVerticalStrut(5));

        JTextArea messageText =
                new JTextArea(message.getMessageText());

        messageText.setFont(
                new Font("Segoe UI", Font.PLAIN, 14));

        messageText.setForeground(
                new Color(35, 45, 60));

        messageText.setLineWrap(true);
        messageText.setWrapStyleWord(true);
        messageText.setEditable(false);
        messageText.setOpaque(false);
        messageText.setBorder(null);

        // Limit bubble width and calculate wrapped height
        messageText.setSize(300, Short.MAX_VALUE);

        Dimension textSize = messageText.getPreferredSize();

        messageText.setPreferredSize(
                new Dimension(
                        300,
                        Math.max(20, textSize.height)));

        messageText.setAlignmentX(Component.LEFT_ALIGNMENT);

        bubble.add(messageText);
        bubble.add(Box.createVerticalStrut(6));

        String time = message.getSentTime() == null
                ? ""
                : message.getSentTime().format(TIME_FORMAT);

        JLabel timeLabel = new JLabel(time);

        timeLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 10));

        timeLabel.setForeground(
                new Color(110, 125, 145));

        timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        bubble.add(timeLabel);

        return bubble;
    }

    private static class RoundedBubble extends JPanel {

        private final Color bubbleColor;

        public RoundedBubble(Color bubbleColor) {
            this.bubbleColor = bubbleColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(bubbleColor);

            g2.fillRoundRect(
                    0, 0,
                    getWidth() - 1,
                    getHeight() - 1,
                    18, 18);

            g2.dispose();

            super.paintComponent(g);
        }
    }
}
