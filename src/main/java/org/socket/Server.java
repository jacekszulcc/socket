package org.socket;

import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class Server {

    private static final int PORT = 4999;
    private static final String VERSION = "1.0.0";
    private static final Instant START_TIME = Instant.now();
    private static final String CREATED_AT = DateTimeFormatter.ISO_INSTANT.format(START_TIME);

    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println(jsonInfo("status", "Serwer nasłuchuje na porcie " + PORT));
            try (Socket socket = serverSocket.accept()) {
                System.out.println(jsonInfo("status", "Połączono z klientem"));
                handleClient(socket);
            }
        } catch (IOException e) {
            System.err.println(jsonError("Błąd uruchamiania serwera: " + e.getMessage()));
        }

        System.out.println(jsonInfo("status", "Serwer zakończył działanie"));
    }

    private static void handleClient(Socket socket) throws IOException {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            boolean running = true;

            while (running) {
                String input = reader.readLine();
                if (input == null || input.isBlank()) continue;

                String json = handleCommand(input.trim().toLowerCase());
                writer.println(json);

                CommandResponse response = gson.fromJson(json, CommandResponse.class);
                if ("stop".equals(response.command)) {
                    running = false;
                }
            }
        }
    }

    private static String handleCommand(String command) {
        CommandResponse response = new CommandResponse(command);

        switch (command) {
            case "uptime" -> {
                Duration uptime = Duration.between(START_TIME, Instant.now());
                response.uptime_seconds = uptime.getSeconds();
            }
            case "info" -> {
                response.version = VERSION;
                response.created_at = CREATED_AT;
            }
            case "help" -> {
                response.status = """
                    {
                        \"commands\": [
                            {\"command\": \"uptime\", \"description\": \"Czas działania serwera\"},
                            {\"command\": \"info\", \"description\": \"Wersja i data utworzenia\"},
                            {\"command\": \"help\", \"description\": \"Lista dostępnych komend\"},
                            {\"command\": \"stop\", \"description\": \"Zatrzymuje serwer i klienta\"}
                        ]
                    }
                    """.trim();
            }
            case "stop" -> response.status = "Zamykanie serwera i klienta...";
            default -> response.error = "Nieznana komenda: " + command;
        }

        return gson.toJson(response);
    }

    private static String jsonInfo(String key, String value) {
        return gson.toJson(new InfoMessage(key, value));
    }

    private static String jsonError(String message) {
        return gson.toJson(new InfoMessage("error", message));
    }

    private static class InfoMessage {
        String type;
        String message;

        public InfoMessage(String type, String message) {
            this.type = type;
            this.message = message;
        }
    }
}
