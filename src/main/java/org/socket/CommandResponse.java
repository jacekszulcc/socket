package org.socket;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class CommandResponse {
    public String command;
    public String status;
    public String version;
    @SerializedName("created_at")
    public String createdAt;
    @SerializedName("uptime_seconds")
    public Long uptimeSeconds;
    public String error;
    public List<CommandInfo> availableCommands;
    public String messageList;


    public CommandResponse(String command) {
        this.command = command;
    }
}
