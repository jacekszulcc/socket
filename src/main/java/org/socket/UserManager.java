package org.socket;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages user registration, lookup, and data persistence to a JSON file.
 * Used to load existing users and store newly registered ones.
 */
public class UserManager {
    private static final String FILE_PATH = "users.json";
    private List<User> users;

    /**
     * Initializes the user manager and loads existing users from the JSON file.
     */
    public UserManager() {
        users = loadUsersFromFile();
    }

    /**
     * Loads the list of users from the JSON file.
     * Returns an empty list if the file does not exist or is empty.
     *
     * @return list of users loaded from file, or an empty list if none found or error occurs
     */
    private List<User> loadUsersFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }

        try {
            List<User> loaded = JsonUtil.loadListFromJsonFile(FILE_PATH, User.class);
            return loaded != null ? loaded : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Błąd odczytu user.json" + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Registers a new user if the username is not already taken.
     * The new user is saved to the users list and persisted to JSON.
     *
     * @param username the desired username
     * @param password the desired password
     * @param role     user role ("admin" or "user")
     * @return true if registration succeeded, false if the username already exists
     */
    public boolean registerUser(String username, String password, String role) {
        if (getUserByUsername(username) != null){
            return false;
        }
        User newUser = new User(username, password, role);
        users.add(newUser);
        saveUsersToFile();;
        return true;
    }

    /**
     * Searches for a user by username (case-insensitive).
     *
     * @param username the username to look up
     * @return the matching User object, or null if not found
     */
    public User getUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Saves the current list of users to the JSON file.
     */
    private void saveUsersToFile() {
        try {
            JsonUtil.saveToJsonFile(users, FILE_PATH);
        }
        catch (IOException e){
            System.err.println("Błąd zapisu users.json" + e.getMessage());
        }
    }

    /**
     * Logs in a user by checking provided credentials.
     *
     * @param username the username
     * @param password the password
     * @return the logged-in User object if credentials are correct, otherwise null
     */
    public User loginUser(String username, String password) {
        User user = getUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
}
