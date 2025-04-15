package org.socket;

public class CommandResponse {
    public String command;
    public String status;
    public String version;
    public String created_at;
    public Long uptime_seconds;
    public String error;

    public CommandResponse(String command) {
        this.command = command;
    }
}
