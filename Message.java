
package campusmarketplace.model;

import java.time.LocalDateTime;

public class Message {

    private final int messageId;
    private final int senderId;
    private final int receiverId;
    private final int productId;
    private final String messageText;
    private final LocalDateTime sentTime;

    public Message(int messageId,
                   int senderId,
                   int receiverId,
                   int productId,
                   String messageText,
                   LocalDateTime sentTime) {

        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.productId = productId;
        this.messageText = messageText;
        this.sentTime = sentTime;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public int getProductId() {
        return productId;
    }

    public String getMessageText() {
        return messageText;
    }

    public LocalDateTime getSentTime() {
        return sentTime;
    }
}
