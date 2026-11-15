
package campusmarketplace.dao;

import campusmarketplace.model.Message;

import campusmarketplace.model.ConversationPreview;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MessageDAO {

    // 1. Send and save a message
    public boolean sendMessage(
            int senderId,
            int receiverId,
            int productId,
            String messageText) throws SQLException {

        if (senderId <= 0 || receiverId <= 0
                || productId <= 0 || senderId == receiverId) {
            throw new IllegalArgumentException(
                    "Invalid message participants or product.");
        }

        if (messageText == null
                || messageText.isBlank()
                || messageText.trim().length() > 2000) {
            throw new IllegalArgumentException(
                    "Message must contain 1 to 2000 characters.");
        }

        String sql = """
            INSERT INTO messages
                (sender_id, receiver_id, product_id, message_text)
            SELECT ?, ?, p.product_id, ?
            FROM products p
            WHERE p.product_id = ?
              AND p.status <> 'REMOVED'
              AND (
                  p.seller_id = ?
                  OR (
                      p.seller_id = ?
                      AND EXISTS (
                          SELECT 1 FROM messages m
                          WHERE m.product_id = p.product_id
                            AND m.sender_id = ?
                            AND m.receiver_id = ?
                      )
                  )
              )
            """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, messageText.trim());
            stmt.setInt(4, productId);

            // Buyer is sending to the product seller
            stmt.setInt(5, receiverId);

            // Seller is replying to an existing buyer
            stmt.setInt(6, senderId);
            stmt.setInt(7, receiverId);
            stmt.setInt(8, senderId);

            return stmt.executeUpdate() == 1;
        }
    }

    // 2. Retrieve conversation between two students
    public List<Message> getConversation(
            int productId,
            int currentUserId,
            int otherUserId) throws SQLException {

        if (productId <= 0 || currentUserId <= 0
                || otherUserId <= 0
                || currentUserId == otherUserId) {
            throw new IllegalArgumentException(
                    "Invalid conversation.");
        }

        List<Message> messages = new ArrayList<>();

        String sql = """
            SELECT m.*
            FROM messages m
            JOIN products p
              ON m.product_id = p.product_id
            WHERE m.product_id = ?
              AND p.seller_id IN (?, ?)
              AND (
                  (m.sender_id = ? AND m.receiver_id = ?)
                  OR
                  (m.sender_id = ? AND m.receiver_id = ?)
              )
            ORDER BY m.sent_time ASC, m.message_id ASC
            """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            stmt.setInt(2, currentUserId);
            stmt.setInt(3, otherUserId);

            stmt.setInt(4, currentUserId);
            stmt.setInt(5, otherUserId);

            stmt.setInt(6, otherUserId);
            stmt.setInt(7, currentUserId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    messages.add(mapMessage(rs));
                }
            }
        }

        return messages;
    }

    // 3. Retrieve latest message of every conversation
    public List<Message> getInboxMessages(int userId)
            throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid user account.");
        }

        Map<String, Message> latestMessages =
                new LinkedHashMap<>();

        String sql = """
            SELECT m.*
            FROM messages m
            JOIN products p
              ON m.product_id = p.product_id
            WHERE (m.sender_id = ? OR m.receiver_id = ?)
              AND p.seller_id IN (m.sender_id, m.receiver_id)
            ORDER BY m.sent_time DESC, m.message_id DESC
            """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Message message = mapMessage(rs);

                    int otherUserId =
                            message.getSenderId() == userId
                            ? message.getReceiverId()
                            : message.getSenderId();

                    String conversationKey =
                            message.getProductId()
                            + ":" + otherUserId;

                    latestMessages.putIfAbsent(
                            conversationKey, message);
                }
            }
        }

        return new ArrayList<>(latestMessages.values());
    }

    // Convert database row into Message object
    private Message mapMessage(ResultSet rs)
            throws SQLException {

        return new Message(
                rs.getInt("message_id"),
                rs.getInt("sender_id"),
                rs.getInt("receiver_id"),
                rs.getInt("product_id"),
                rs.getString("message_text"),
                rs.getTimestamp("sent_time")
                        .toLocalDateTime()
        );
    }

public List<ConversationPreview> getInboxPreviews(int userId)
        throws SQLException {

    if (userId <= 0) {
        throw new IllegalArgumentException(
                "Invalid user account.");
    }

    Map<String, ConversationPreview> previews =
            new LinkedHashMap<>();

    String sql = """
        SELECT
            m.message_id,
            m.sender_id,
            m.receiver_id,
            m.product_id,
            m.message_text,
            m.sent_time,
            p.title AS product_title,
            CASE
                WHEN m.sender_id = ?
                THEN receiver.full_name
                ELSE sender.full_name
            END AS other_user_name
        FROM messages m
        JOIN products p
            ON m.product_id = p.product_id
        JOIN users sender
            ON m.sender_id = sender.user_id
        JOIN users receiver
            ON m.receiver_id = receiver.user_id
        WHERE (m.sender_id = ? OR m.receiver_id = ?)
          AND p.seller_id IN
              (m.sender_id, m.receiver_id)
        ORDER BY m.sent_time DESC, m.message_id DESC
        """;

    try (Connection conn =
                 DatabaseConnection.getConnection();
         PreparedStatement stmt =
                 conn.prepareStatement(sql)) {

        stmt.setInt(1, userId);
        stmt.setInt(2, userId);
        stmt.setInt(3, userId);

        try (ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                int senderId = rs.getInt("sender_id");
                int receiverId = rs.getInt("receiver_id");
                int productId = rs.getInt("product_id");

                int otherUserId =
                        senderId == userId
                        ? receiverId
                        : senderId;

                String key =
                        productId + ":" + otherUserId;

                if (!previews.containsKey(key)) {

                    ConversationPreview preview =
                            new ConversationPreview(
                                    productId,
                                    rs.getString("product_title"),
                                    otherUserId,
                                    rs.getString("other_user_name"),
                                    rs.getString("message_text"),
                                    rs.getTimestamp("sent_time")
                                            .toLocalDateTime()
                            );

                    previews.put(key, preview);
                }
            }
        }
    }

    return new ArrayList<>(previews.values());
}

public String getStudentName(int userId)
        throws SQLException {

    String sql = """
        SELECT full_name
        FROM users
        WHERE user_id = ?
        """;

    try (Connection conn =
                 DatabaseConnection.getConnection();
         PreparedStatement stmt =
                 conn.prepareStatement(sql)) {

        stmt.setInt(1, userId);

        try (ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getString("full_name");
            }
        }
    }

    return null;
}

}
