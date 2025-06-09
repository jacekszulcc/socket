package org.socket;

import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * A socket server class that handles commands sent by clients.
 * Supports commands: uptime, info, help, stop.
 */
public class Server {

    private static final int PORT = 4999;
    private static final String VERSION = "1.1.0";
    private static final Instant START_TIME = Instant.now();
    private static final String CREATED_AT = DateTimeFormatter.ISO_INSTANT.format(START_TIME);

    private static final UserManager userManager = new UserManager();
    private static final MessageManager messageManager = new MessageManager(userManager);

    private static final Gson gson = new Gson();

    /**
     * Main method that starts the server and listens for client connections.
     */
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println(JsonUtil.jsonInfo("status", "Serwer nasłuchuje na porcie " + PORT));
            try (Socket socket = serverSocket.accept()) {
                System.out.println(JsonUtil.jsonInfo("status", "Połączono z klientem"));
                handleClient(socket);
            }
        } catch (IOException e) {
            System.err.println(JsonUtil.jsonError("Błąd uruchamiania serwera: " + e.getMessage()));
        }

        System.out.println(JsonUtil.jsonInfo("status", "Serwer zakończył działanie"));
    }

    /**
     * Handles a single client connection.
     * @param socket active socket connection with the client
     * @throws IOException if an I/O error occurs
     */
    private static void handleClient(Socket socket) throws IOException {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            boolean running = true;

            while (running) {
                try {
                    String input = reader.readLine();
                    if (input == null || input.isBlank()) {
                        System.out.println("Klient zakończył połączenie lub przesłał pustą wiadomość.");
                        break;
                    }

                    CommandResponse response = handleCommand(input.trim().toLowerCase());
                    writer.println(gson.toJson(response));

                    if ("stop".equalsIgnoreCase(response.command)) {
                        running = false;
                    }
                }
                catch (Exception e){
                    writer.println(JsonUtil.jsonError("Błąd przetwarzania komendy: " + e.getMessage()));
                    break;
                }
            }
        }
        catch (IOException e){
            System.err.println("Błąd podczas komunikacji z klientem: " + e.getMessage());
        }
    }

    /**
     * Processes a raw command string sent by the client and returns a response object
     * .
     * @param input full command line sent by the client (e.g. "send ania Hello")
     * @return response containing the result of command execution
     */
    private static CommandResponse handleCommand(String input) {
        String[] args = input.trim().split("\\s+");
        String command = args[0].toLowerCase();

        CommandResponse response = new CommandResponse(command);

        switch (command) {
            case "uptime" -> {
                Duration uptime = Duration.between(START_TIME, Instant.now());
                response.uptimeSeconds = uptime.getSeconds();
            }
            case "info" -> {
                response.version = VERSION;
                response.createdAt = CREATED_AT;
            }
            case "help" -> {
                response.availableCommands = List.of(
                  new CommandInfo("uptime", "Czas działania serwera"),
                  new CommandInfo("info", "Wersja i data utworzenia"),
                  new CommandInfo("help", "Lista dostępnych komend"),
                  new CommandInfo("stop", "Zatrzymuje serwer i klienta"),
                  new CommandInfo("send", "Wyślij wiadomość: send <użytkownik> <treść>")
                );
            }
            case "stop" -> response.status = "Zamykanie serwera i klienta...";

            case "send" -> {
                // replace with session -based logged user when login is implemented
                User tempUser = userManager.getUserByUsername("jacek");
                return processSendCommand(tempUser, args, messageManager);
            }

            case "inbox" -> {
                // replace with session -based logged user when login is implemented
                User tempUser = userManager.getUserByUsername("jacek");
                return processInboxCommand(tempUser, userManager);
            }

            default -> response.error = "Nieznana komenda: " + command;
        }

        return response;
    }

    /**
     * Handles the 'send' command with allows a logged-in user to send a message.
     *
     * Usage: send <recipient> <message>
     *
     * @param sender         the logged-in user
     * @param args           the command arguments (parsed from input string)
     * @return response object with status or error message
     */
    private static CommandResponse processSendCommand(User sender, String[] args, MessageManager messageManager) {
        CommandResponse response = new CommandResponse("send");

        if (sender == null ) {
            response.error = "Musisz być zalogowany, aby wysłać wiadomości.";
            return response;
        }

        if (args.length < 3) {
            response.error = "Użycie: send <odbiorca> <wiadomość>";
            return response;
        }

        String recipient = args[1];
        String content = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        String result = messageManager.sendMessage(sender, recipient, content);


        response.status = switch (result) {
            case "ok" -> "Wiadomość wysłana";
            case "user_not_found" -> "Nie znaleziono użytkownika.";
            case "inbox_full" -> "Szkrzynka odbiorcy jest pełna.";
            case "too_long" -> "Wiadomość jest zbyt długa (max 255 znaków).";
            default -> "Nieznany błąd.";
        };

        return response;
    }

    /**
     * Processes the "inbox" command for the currently logged-in user.
     *
     * This command retrieves all unread messages from the user's inbox,
     * marks them as read, and returns a formatted response.
     *
     * @param user the currently logged-in user
     * @param userManager the UserManager instance responsible for user data
     * @return a CommandResponse containing the list of messages or a notification if none
     */
    private static CommandResponse processInboxCommand(User user, UserManager userManager){
        CommandResponse response = new CommandResponse("inbox");

        if (user == null) {
            response.error = "Musisz być zalogowany, aby sprawdzić skrzynkę.";
            return response;
        }

        List<Message> inbox = user.getInbox();
        if (inbox == null || inbox.isEmpty()) {
            response.status = "Brak nowych wiadomości.";
            return response;
        }

        List<Message> unreadMessages = inbox.stream()
                .filter(m -> !m.isRead())
                .toList();

        if (unreadMessages.isEmpty()) {
            response.status = "Brak nowych wiadomości.";
            return response;
        }

        StringBuilder sb = new StringBuilder();
        for (Message msg : unreadMessages) {
            sb.append("Od: ").append(msg.getFrom()).append("\n");
            sb.append("Treść: ").append(msg.getContent()).append("\n");
            sb.append("---\n");

            msg.setRead(true);
        }

        userManager.saveAll();

        response.messageList = sb.toString().trim();

        return response;
    }

}
