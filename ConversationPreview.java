
package campusmarketplace.model;

import java.time.LocalDateTime;

public class ConversationPreview {

    private final int productId;
    private final String productTitle;
    private final int otherUserId;
    private final String otherUserName;
    private final String latestMessage;
    private final LocalDateTime sentTime;

    public ConversationPreview(
            int productId,
            String productTitle,
            int otherUserId,
            String otherUserName,
            String latestMessage,
            LocalDateTime sentTime) {

        this.productId = productId;
        this.productTitle = productTitle;
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.latestMessage = latestMessage;
        this.sentTime = sentTime;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public int getOtherUserId() {
        return otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public LocalDateTime getSentTime() {
        return sentTime;
    }
}
