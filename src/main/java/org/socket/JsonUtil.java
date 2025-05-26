package org.socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Utility class for JSON serialization and deserialization using Gson.
 * Provides helper methods for saving objects to JSON files, loading lists from files,
 * and generating standardized JSON messages.
 */
public class JsonUtil {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * Creates a JSON-formatted message with a custom key and value.
     *
     * @param key   the message type key (e.g., "status", "info")
     * @param value the message value
     * @return a JSON string with the given key-value pair
     */
    public static String jsonInfo(String key,String value) {
        return gson.toJson(new InfoMessage(key,value));
    }

    /**
     * Creates a standardized JSON-formatted error message.
     *
     * @param message the error message to include
     * @return a JSON string with the "error" type and the provided message
     */
    public static String jsonError(String message) {
        return gson.toJson(new InfoMessage("error", message));
    }

    /**
     * Internal helper class used to format info/error messages as JSON.
     */
    private static class InfoMessage {
        String type;
        String message;

        public
        InfoMessage(String type,String message) {
            this.type = type;
            this.message = message;
        }
    }

    /**
     * Saves any Java object (e.g. list or map) to a JSON file.
     *
     * @param data     the object to serialize
     * @param filePath the path to the output file
     * @throws IOException if the file cannot be written
     */
    public static void saveToJsonFile(Object data, String filePath) throws IOException {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            gson.toJson(data, fileWriter);
        }
    }

    /**
     * Loads a list of objects from a JSON file.
     *
     * @param filePath the path to the JSON file
     * @param tClass   the class of the list's element type
     * @param <T>      the type of the list elements
     * @return a list of deserialized objects
     * @throws IOException if the file cannot be read or parsed
     */
    public static <T> List<T> loadListFromJsonFile(String filePath, Class<T> tClass) throws IOException{
        Type listType = TypeToken.getParameterized(List.class, tClass).getType();
        return gson.fromJson(new FileReader(filePath), listType);
    }

    /**
     * Checks if the given user has admin privileges
     *
     * @param user the user to check
     * @return true if user has role "admin", false otherwise
     */
    public static boolean isAdmin(User user) {
        return user != null && "admin".equalsIgnoreCase(user.getRole());
    }
}
