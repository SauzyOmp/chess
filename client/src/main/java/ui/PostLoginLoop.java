package ui;

import client.ServerFacade;
import exception.ResponseException;
import model.GameData;
import client.GameResult;
import client.GamesResult;
import chess.ChessGame;
import ui.EscapeSequences;

import java.util.Scanner;

public class PostLoginLoop {
    public void run(Scanner scanner, ServerFacade facade, String authToken, String username) {
        while (true) {
            System.out.print("[" + username + "] > ");
            String input = scanner.nextLine().trim().toLowerCase();
            try {
                if (input.equals("help") || input.equals("h")) {
                    printHelp();
                } else if (input.equals("list games") || input.equals("lg")) {
                    handleListGames(facade, authToken);
                } else if (input.equals("create game") || input.equals("cg")) {
                    handleCreateGame(scanner, facade, authToken);
                } else if (input.startsWith("play ") || input.startsWith("p ") || input.startsWith("observe ") || input.startsWith("o ")) {
                    handleJoinAndDraw(scanner, facade, authToken, username, input);
                } else if (input.equals("logout") || input.equals("lo")) {
                    facade.logout(authToken);
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Logged out." + EscapeSequences.RESET_TEXT_COLOR);
                    return;
                } else {
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + 
                        "Unknown command. Type 'help' to see options." + EscapeSequences.RESET_TEXT_COLOR);
                }
            } catch (ResponseException e) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
            }
        }
    }

    private void printHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + EscapeSequences.SET_TEXT_BOLD + 
            "Available commands:" + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + 
            "help" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (h)" + EscapeSequences.SET_TEXT_COLOR_WHITE + 
            " - Show this help message");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + 
            "list games" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (lg)" + EscapeSequences.SET_TEXT_COLOR_WHITE + 
            " - Show all available games");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + 
            "create game" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (cg)" + EscapeSequences.SET_TEXT_COLOR_WHITE + 
            " - Create a new game");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + 
            "play <number>" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (p)" + EscapeSequences.SET_TEXT_COLOR_WHITE + 
            " - Join a game as a player (you'll choose color)");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + 
            "observe <number>" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (o)" + EscapeSequences.SET_TEXT_COLOR_WHITE + 
            " - Join a game as an observer");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_RED + 
            "logout" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (lo)" + EscapeSequences.SET_TEXT_COLOR_WHITE + 
            " - Log out and return to main menu");
    }

    private void handleListGames(ServerFacade facade, String authToken) throws ResponseException {
                    GamesResult games = facade.listGames(authToken);
            if (games.games().isEmpty()) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "No games available." + EscapeSequences.RESET_TEXT_COLOR);
            } else {
                int index = 1;
                for (GameData g : games.games()) {
                System.out.printf(EscapeSequences.SET_TEXT_COLOR_GREEN + "%d" + EscapeSequences.SET_TEXT_COLOR_WHITE + ") " + 
                    EscapeSequences.SET_TEXT_COLOR_BLUE + "%s" + EscapeSequences.SET_TEXT_COLOR_WHITE + " - " + 
                    EscapeSequences.SET_TEXT_COLOR_YELLOW + "white: %s" + EscapeSequences.SET_TEXT_COLOR_WHITE + ", " + 
                    EscapeSequences.SET_TEXT_COLOR_YELLOW + "black: %s" + EscapeSequences.RESET_TEXT_COLOR + "%n",
                    index++, g.gameName(), g.whiteUsername(), g.blackUsername());
            }
        }
    }

    private void handleCreateGame(Scanner scanner, ServerFacade facade, String authToken) throws ResponseException {
        System.out.print("Game name: ");
        String name = scanner.nextLine().trim();
        GameResult result = facade.createGame(authToken, name);
        
        GamesResult games = facade.listGames(authToken);
        int gamePosition = games.games().size();
        
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Created game '" + name + "' (ID " + 
            result.gameID() + ")" + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Use command " + 
            EscapeSequences.SET_TEXT_COLOR_GREEN + "play " + gamePosition + 
            EscapeSequences.SET_TEXT_COLOR_YELLOW + " to join" + 
            EscapeSequences.RESET_TEXT_COLOR);
    }

    private void handleJoinAndDraw(Scanner scanner, ServerFacade facade, String authToken, String username, String input) throws ResponseException {
        String[] parts = input.split("\\s+");
        if (parts.length < 2) {
            System.out.println("Error: Please specify a game number. Use 'list games' to see available games.");
            return;
        }
        
        try {
            int choice = Integer.parseInt(parts[1]) - 1;
            boolean isObserving = parts[0].equals("observe") || parts[0].equals("o");
            GamesResult games = facade.listGames(authToken);

            if (choice < 0 || choice >= games.games().size()) {
                System.out.println("Error: Invalid game number. Use 'list games' to see available games.");
                System.out.println("Available games: 1-" + games.games().size());
                return;
            }

            GameData selected = games.games().get(choice);
            String role = null;
            ChessGame.TeamColor playerColor = null;
            
            if (isObserving) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Observing game..." + EscapeSequences.RESET_TEXT_COLOR);
            } else {
                // Check if user is already playing in this game
                if (username.equals(selected.whiteUsername())) {
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + 
                        "You are already playing in this game as " + 
                        EscapeSequences.SET_TEXT_COLOR_WHITE + "WHITE" + 
                        EscapeSequences.SET_TEXT_COLOR_YELLOW + "." + EscapeSequences.RESET_TEXT_COLOR);
                    playerColor = ChessGame.TeamColor.WHITE;
                } else if (username.equals(selected.blackUsername())) {
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + 
                        "You are already playing in this game as " + 
                        EscapeSequences.SET_TEXT_COLOR_BLACK + "BLACK" + 
                        EscapeSequences.SET_TEXT_COLOR_YELLOW + "." + EscapeSequences.RESET_TEXT_COLOR);
                    playerColor = ChessGame.TeamColor.BLACK;
                } else {
                    // User is not in the game yet, let them choose a color
                    // Check if positions are already taken
                    if (selected.whiteUsername() != null && selected.blackUsername() != null) {
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + 
                            "This game is full. Both positions are already taken." + EscapeSequences.RESET_TEXT_COLOR);
                        return;
                    }
                    
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + 
                        EscapeSequences.SET_TEXT_BOLD + "Choose your color:" + EscapeSequences.RESET_TEXT_COLOR);
                    
                    if (selected.whiteUsername() == null) {
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "w) " + 
                            EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_KING + " White");
                    }
                    if (selected.blackUsername() == null) {
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "b) " + 
                            EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_KING + " Black");
                    }
                    
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN + "Enter choice (w or b): " + 
                        EscapeSequences.RESET_TEXT_COLOR);
                    
                    String colorChoice = scanner.nextLine().trim().toLowerCase();
                    if (colorChoice.equals("w") && selected.whiteUsername() == null) {
                        role = "WHITE";
                        playerColor = ChessGame.TeamColor.WHITE;
                    } else if (colorChoice.equals("b") && selected.blackUsername() == null) {
                        role = "BLACK";
                        playerColor = ChessGame.TeamColor.BLACK;
                    } else {
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + 
                            "Invalid choice or position already taken. Please choose an available position." + 
                            EscapeSequences.RESET_TEXT_COLOR);
                        return;
                    }
                    
                    facade.joinGame(authToken, String.valueOf(selected.gameID()), role);
                    String commandUsed = "play " + (choice + 1);
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Joined game as " + role + 
                        " using command: " + EscapeSequences.SET_TEXT_COLOR_YELLOW + commandUsed + 
                        EscapeSequences.RESET_TEXT_COLOR);
                }
            }
            
            String serverUrl = "http://localhost:8080";
            GameplayUI gameplayUI = new GameplayUI(authToken, selected.gameID(), username, playerColor, serverUrl);
            gameplayUI.run();
            
        } catch (NumberFormatException e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + 
                "Error: Invalid game number. Please enter a number." + EscapeSequences.RESET_TEXT_COLOR);
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + 
                "Error starting gameplay: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
        }
    }
}
