package org.socket;

public class Message {
    private String from;
    private String to;
    private String content;
    private boolean read;

    public Message(String from, String to, String content) {
        this.from = from;
        this.to = to;
        this.content = content.length() > 255 ? content.substring(0, 255) : content;
        this.read = false;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getContent() {
        return content;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
