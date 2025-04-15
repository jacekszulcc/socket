package org.socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 4999;

    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT)) {
            System.out.println(jsonInfo("status", "Połączono z serwerem"));

            handleCommunication(socket);

        } catch (IOException e) {
            System.err.println(jsonError("Błąd połączenia z serwerem: " + e.getMessage()));
        }

        System.out.println(jsonInfo("status", "Zakończono działanie klienta"));
    }

    private static void handleCommunication(Socket socket) throws IOException {
        try (
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner scanner = new Scanner(System.in)
        ) {
            boolean running = true;

            while (running) {
                System.out.println("\n=== MENU ===");
                System.out.println("1. uptime – czas działania serwera");
                System.out.println("2. info   – wersja i data utworzenia");
                System.out.println("3. help   – dostępne komendy");
                System.out.println("4. stop   – zakończ serwer i klienta");
                System.out.print("Wybierz opcję (1-4): ");

                String choice = scanner.nextLine().trim();
                String command;

                switch (choice) {
                    case "1" -> command = "uptime";
                    case "2" -> command = "info";
                    case "3" -> command = "help";
                    case "4" -> command = "stop";
                    default -> {
                        System.out.println("Nieprawidłowy wybór. Spróbuj ponownie.");
                        continue;
                    }
                }

                writer.println(command);

                String responseJson = reader.readLine();

                if (responseJson == null || responseJson.isBlank()) {
                    System.out.println("Odpowiedź z serwera jest pusta!");
                    continue;
                }

                CommandResponse response = gson.fromJson(responseJson, CommandResponse.class);

                if ("help".equalsIgnoreCase(response.command) && response.status != null) {
                    try {
                        Object statusObj = gson.fromJson(response.status, Object.class);
                        String prettyStatus = new GsonBuilder().setPrettyPrinting().create().toJson(statusObj);
                        System.out.println("\nDostępne komendy:");
                        System.out.println(prettyStatus);
                    } catch (Exception e) {
                        System.out.println("Błąd parsowania status JSON: " + e.getMessage());
                        System.out.println("Status jako tekst:");
                        System.out.println(response.status);
                    }
                } else {
                    System.out.println("\nOdpowiedź z serwera:");
                    System.out.println(gson.toJson(response));
                }

                if ("stop".equalsIgnoreCase(command)) {
                    running = false;
                }
            }
        }
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