package org.socket;

import java.util.List;

/**
 * Manages inbox operations like displaying messages.
 */
public class InboxManager {

    /**
     * Processes the 'inbox' command.
     * Allows users to view their own inbox and admins to view others' inboxes.
     *
     * @param requester the user issuing the command
     * @param args      command arguments (e.g., "inbox", "inbox username")
     * @param userManager instance for user data access
     * @return JSON-formatted inbox or error message
     */
    public String processInboxCommand(User requester, String[] args, UserManager userManager) {
        if (args.length == 1) {
            return formatInbox(requester);
        } else if (args.length == 2) {
            String targetUsername = args[1];
            if (!JsonUtil.isAdmin(requester) && !requester.getUsername().equalsIgnoreCase(targetUsername)) {
                return JsonUtil.jsonError("Access denied");
            }

            User target = userManager.getUserByUsername(targetUsername);
            if (target == null) {
                return JsonUtil.jsonError("User not found");
            }

            return formatInbox(target);
        } else {
            return JsonUtil.jsonError("Invalid inbox command.");
        }
    }

    /**
     * Formats the inbox of given user.
     *
     * @param user the user whose inbox should be shown
     * @return JSON-formatted message list
     */
    private String formatInbox(User user) {
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
        }

        return JsonUtil.jsonInfo("inbox", sb.toString());
    }
}

