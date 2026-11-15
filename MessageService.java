
package campusmarketplace.service;

import campusmarketplace.model.ConversationPreview;

import campusmarketplace.dao.MessageDAO;
import campusmarketplace.model.Message;

import java.sql.SQLException;
import java.util.List;

public class MessageService {

    private final MessageDAO messageDAO = new MessageDAO();

    // 1. Send a message to another student
    public boolean sendMessage(
            int senderId,
            int receiverId,
            int productId,
            String messageText) throws SQLException {

        if (senderId <= 0 || receiverId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid sender or receiver.");
        }

        if (senderId == receiverId) {
            throw new IllegalArgumentException(
                    "You cannot send a message to yourself.");
        }

        if (productId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid product ID.");
        }

        if (messageText == null || messageText.isBlank()) {
            throw new IllegalArgumentException(
                    "Message cannot be empty.");
        }

        String cleanMessage = messageText.trim();

        if (cleanMessage.length() > 2000) {
            throw new IllegalArgumentException(
                    "Message cannot exceed 2000 characters.");
        }

        return messageDAO.sendMessage(
                senderId,
                receiverId,
                productId,
                cleanMessage
        );
    }

    // 2. Retrieve conversation history
    public List<Message> getConversation(
            int productId,
            int currentUserId,
            int otherUserId) throws SQLException {

        if (productId <= 0 || currentUserId <= 0
                || otherUserId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid conversation details.");
        }

        if (currentUserId == otherUserId) {
            throw new IllegalArgumentException(
                    "Conversation participants must be different.");
        }

        return messageDAO.getConversation(
                productId,
                currentUserId,
                otherUserId
        );
    }

    // 3. Retrieve Inbox conversations
    public List<Message> getInboxMessages(int userId)
            throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid student account.");
        }

        return messageDAO.getInboxMessages(userId);
    }
    
public List<ConversationPreview> getInboxPreviews(int userId)
        throws SQLException {

    if (userId <= 0) {
        throw new IllegalArgumentException(
                "Invalid student account.");
    }

    return messageDAO.getInboxPreviews(userId);
}

public String getStudentName(int userId)
        throws SQLException {

    if (userId <= 0) {
        throw new IllegalArgumentException(
                "Invalid student ID.");
    }

    return messageDAO.getStudentName(userId);
}

}
