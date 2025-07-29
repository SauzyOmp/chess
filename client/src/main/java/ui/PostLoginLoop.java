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
                    handleJoinAndDraw(scanner, facade, authToken, input);
                } else if (input.equals("logout") || input.equals("lo")) {
                    facade.logout(authToken);
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Logged out." + EscapeSequences.RESET_TEXT_COLOR);
                    return;
                } else {
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Unknown command. Type 'help' to see options." + EscapeSequences.RESET_TEXT_COLOR);
                }
            } catch (ResponseException e) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
            }
        }
    }

    private void printHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + EscapeSequences.SET_TEXT_BOLD + "Available commands:" + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + "help" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (h)" + EscapeSequences.SET_TEXT_COLOR_WHITE + " - Show this help message");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + "list games" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (lg)" + EscapeSequences.SET_TEXT_COLOR_WHITE + " - Show all available games");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + "create game" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (cg)" + EscapeSequences.SET_TEXT_COLOR_WHITE + " - Create a new game");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + "play <number>" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (p)" + EscapeSequences.SET_TEXT_COLOR_WHITE + " - Join a game as a player (you'll choose color)");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + "observe <number>" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (o)" + EscapeSequences.SET_TEXT_COLOR_WHITE + " - Join a game as an observer");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_RED + "logout" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (lo)" + EscapeSequences.SET_TEXT_COLOR_WHITE + " - Log out and return to main menu");
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
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Created game '" + name + "' (ID " + result.gameID() + ")" + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Use command " + EscapeSequences.SET_TEXT_COLOR_GREEN + "play " + result.gameID() + EscapeSequences.SET_TEXT_COLOR_YELLOW + " to join" + EscapeSequences.RESET_TEXT_COLOR);
    }

    private void handleJoinAndDraw(Scanner scanner, ServerFacade facade, String authToken, String input) throws ResponseException {
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
            String role;
            ChessGame.TeamColor perspective;
            
            if (isObserving) {
                role = "WHITE"; // Default for observers
                perspective = null; // Show board normally for observers
                System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Joining as observer..." + EscapeSequences.RESET_TEXT_COLOR);
            } else {
                // Ask for color choice
                System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + EscapeSequences.SET_TEXT_BOLD + "Choose your color:" + EscapeSequences.RESET_TEXT_COLOR);
                System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "1) " + EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.WHITE_KING + " White");
                System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "2) " + EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_KING + " Black");
                System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN + "Enter choice (1 or 2): " + EscapeSequences.RESET_TEXT_COLOR);
                
                String colorChoice = scanner.nextLine().trim();
                if (colorChoice.equals("1")) {
                    role = "WHITE";
                    perspective = ChessGame.TeamColor.WHITE;
                } else if (colorChoice.equals("2")) {
                    role = "BLACK";
                    perspective = ChessGame.TeamColor.BLACK;
                } else {
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid choice. Please enter 1 for White or 2 for Black." + EscapeSequences.RESET_TEXT_COLOR);
                    return;
                }
            }
            
            facade.joinGame(authToken, String.valueOf(selected.gameID()), role);
            String commandUsed = isObserving ? "observe " + (choice + 1) : "play " + (choice + 1);
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Joined game as " + role + " using command: " + EscapeSequences.SET_TEXT_COLOR_YELLOW + commandUsed + EscapeSequences.RESET_TEXT_COLOR);
            
            // Render the board with the correct perspective
            BoardRenderer.renderBoard(selected.game(), perspective);
            
        } catch (NumberFormatException e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: Invalid game number. Please enter a number." + EscapeSequences.RESET_TEXT_COLOR);
        }
    }
}
