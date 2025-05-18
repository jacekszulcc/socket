package org.socket;

import com.google.gson.Gson;

public class JsonUtil {
    private static final Gson gson = new Gson();

    public static String jsonInfo(String key,String value) {
        return gson.toJson(new InfoMessage(key,value));
    }

    public static String jsonError(String message) {
        return gson.toJson(new InfoMessage("error", message));
    }

    private static class InfoMessage {
        String type;
        String message;

        public
        InfoMessage(String type,String message) {
            this.type = type;
            this.message = message;
        }
    }
}
