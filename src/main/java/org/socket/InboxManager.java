package org.socket;

import java.util.List;

/**
 * Manages inbox operations like displaying messages.
 */
public class InboxManager {

    /**
     * Processes the 'inbox' command.
     * Allows users to view their own inbox and admins to view others' inboxes.
     * After viewing, all messages in the inbox are marked as read.
     *
     * Usage:
     * - inbox               -> view own inbox
     * - inbox <username>    -> admin only, view another user's inbox
     *
     * @param requester   the user who issued the command
     * @param args        command arguments (e.g., "inbox", "inbox username")
     * @param userManager manager for accessing and saving user data
     * @return JSON-formatted inbox content or error message
     */
    public String processInboxCommand(User requester, String[] args, UserManager userManager) {
        if (args.length == 1) {
            return formatInbox(requester, userManager);
        } else if (args.length == 2) {
            String targetUsername = args[1];
            if (!JsonUtil.isAdmin(requester) && !requester.getUsername().equalsIgnoreCase(targetUsername)) {
                return JsonUtil.jsonError("Access denied");
            }

            User target = userManager.getUserByUsername(targetUsername);
            if (target == null) {
                return JsonUtil.jsonError("User not found");
            }

            return formatInbox(target, userManager);
        } else {
            return JsonUtil.jsonError("Invalid inbox command.");
        }
    }

    /**
     * Formats the inbox content of a user and marks all messages as read.
     *
     * @param user        the user whose inbox is being displayed
     * @param userManager the user manager used to persist changes
     * @return JSON-formatted string of inbox content
     */
    private String formatInbox(User user, UserManager userManager) {
        List<Message> inbox = user.getInbox();

        if (inbox == null || inbox.isEmpty()) {
            return JsonUtil.jsonInfo("inbox", "No messages.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Inbox of ").append(user.getUsername()).append(":\n");

        for (Message msg : inbox) {
            sb.append("- From: ").append(msg.getFrom())
                    .append(" | Read: ").append(msg.isRead())
                    .append(" | Content: ").append(msg.getContent())
                    .append("\n");

            msg.setRead(true);
        }

        userManager.saveAll();

        return JsonUtil.jsonInfo("inbox", sb.toString());
    }
}

