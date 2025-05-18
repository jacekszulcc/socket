package org.socket;

import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
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
     * Processes a command and returns a response object.
     * @param command command sent by the client
     * @return response object containing results of the command
     */
    private static CommandResponse handleCommand(String command) {
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
                  new CommandInfo("stop", "Zatrzymuje serwer i klienta")
                );
            }
            case "stop" -> response.status = "Zamykanie serwera i klienta...";
            default -> response.error = "Nieznana komenda: " + command;
        }

        return response;
    }
}
