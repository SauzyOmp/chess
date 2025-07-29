package ui;

import client.ServerFacade;
import exception.ResponseException;
import model.GameData;
import service.CreateGameResult;
import service.GamesResult;

import java.util.Scanner;

public class PostLoginLoop {
    public void run(Scanner scanner, ServerFacade facade, String authToken) {
        while (true) {
            System.out.print("[Logged in] > ");
            String input = scanner.nextLine().trim().toLowerCase();
            try {
                if (input.equals("help")) {
                    printHelp();
                } else if (input.equals("list games")) {
                    handleListGames(facade, authToken);
                } else if (input.equals("create game")) {
                    handleCreateGame(scanner, facade, authToken);
                } else if (input.startsWith("play ") || input.startsWith("observe ")) {
                    handleJoinAndDraw(scanner, facade, authToken, input);
                } else if (input.equals("logout")) {
                    facade.logout(authToken);
                    System.out.println("Logged out.");
                    return;
                } else {
                    System.out.println("Unknown command. Type 'help' to see options.");
                }
            } catch (ResponseException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  help - Show this help message");
        System.out.println("  list games - Show all available games");
        System.out.println("  create game - Create a new game");
        System.out.println("  play <number> <WHITE|BLACK> - Join a game as a player (use number from 'list games')");
        System.out.println("  observe <number> - Join a game as an observer (use number from 'list games')");
        System.out.println("  logout - Log out and return to main menu");
    }

    private void handleListGames(ServerFacade facade, String authToken) throws ResponseException {
        GamesResult games = facade.listGames(authToken);
        if (games.games().isEmpty()) {
            System.out.println("No games available.");
        } else {
            int index = 1;
            for (GameData g : games.games()) {
                System.out.printf("%d) %s - white: %s, black: %s%n",
                    index++, g.game(), g.whiteUsername(), g.blackUsername());
            }
        }
    }

    private void handleCreateGame(Scanner scanner, ServerFacade facade, String authToken) throws ResponseException {
        System.out.print("Game name: ");
        String name = scanner.nextLine().trim();
        CreateGameResult result = facade.createGame(authToken, name);
        System.out.println("Created game '" + result.gameID() + "' (ID " + result.gameID() + ")");
    }

    private void handleJoinAndDraw(Scanner scanner, ServerFacade facade, String authToken, String input) throws ResponseException {
        String[] parts = input.split("\\s+");
        if (parts.length < 2) {
            System.out.println("Error: Please specify a game number. Use 'list games' to see available games.");
            return;
        }
        
        try {
            int choice = Integer.parseInt(parts[1]) - 1;
            String role = parts[0].equals("observe") ? "WHITE" : (parts.length >= 3 ? parts[2].toUpperCase() : "WHITE");
            GamesResult games = facade.listGames(authToken);
            
            if (choice < 0 || choice >= games.games().size()) {
                System.out.println("Error: Invalid game number. Use 'list games' to see available games.");
                System.out.println("Available games: 1-" + games.games().size());
                return;
            }
            
            GameData selected = games.games().get(choice);
            facade.joinGame(authToken, String.valueOf(selected.gameID()), role);
            System.out.println("Joined game as " + role);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid game number. Please enter a number.");
        }
    }
}
