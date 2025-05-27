package org.socket;

import java.util.List;

/**
 * Handles sending private messages between users.
 * Ensures inbox limits and content length rules are enforced.
 */
public class MessageManager {
    private final UserManager userManager;

    /**
     * Constructs a MessageManager with access to UserManager.
     */
    public MessageManager(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * Sends a message from one user to another
     *
     * @param fromUser   the sender (must be a logged-in User object)
     * @param toUsername the recipient's username
     * @param content    the message content (max 255 characters)
     * @return result string:
     *         "ok" - message delivered,
     *         "user_not_found" - recipient does not exist,
     *         "inbox_full" - recipient's inbox is full,
     *         "to_long" - message exceeds 255 characters
     */
    public String sendMessage(User fromUser, String toUsername, String content) {
        if (content.length() > 255) return "too_long";

        User recipient = userManager.getUserByUsername(toUsername);
        if (recipient == null) return "user_not_found";

        long unreadCount = countUnread(recipient.getInbox());
        if (unreadCount >= 5) return "inbox_full";

        Message msg = new Message(fromUser.getUsername(), toUsername, content);
        recipient.getInbox().add(msg);
        userManager.saveAll();

        return "ok";
    }

    /**
     * Counts the number of unread messages in a user's inbox
     *
     * @param inbox the recipient's inbox
     * @return the count of unread messages
     */
    private long countUnread(List<Message> inbox) {
        return inbox.stream()
                .filter( m -> !m.isRead())
                .count();
    }
}
