package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import client.WebSocketFacade;
import exception.ResponseException;

import java.util.Collection;
import java.util.Scanner;

public class GameplayUI implements WebSocketFacade.GameHandler {
    private final Scanner scanner = new Scanner(System.in);
    private final String authToken;
    private final Integer gameID;
    private final String username;
    private final ChessGame.TeamColor playerColor;
    private WebSocketFacade webSocket;
    private ChessGame currentGame;
    private boolean gameActive = true;

    public GameplayUI(String authToken, Integer gameID, String username, ChessGame.TeamColor playerColor, String serverUrl) {
        this.authToken = authToken;
        this.gameID = gameID;
        this.username = username;
        this.playerColor = playerColor;
        
        try {
            this.webSocket = new WebSocketFacade(serverUrl.replace("http", "ws") + "/ws", this);
            this.webSocket.connect(authToken, gameID);
        } catch (Exception e) {
            System.out.println("Failed to connect to WebSocket: " + e.getMessage());
        }
    }

    public void run() {
        System.out.println("Welcome to the chess game!");
        if (playerColor != null) {
            System.out.println("You are playing as " + playerColor);
        } else {
            System.out.println("You are observing the game");
        }
        
        while (gameActive) {
            System.out.print("\nEnter a command (or 'help' for options): ");
            String input = scanner.nextLine().trim();
            
            try {
                processCommand(input);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void processCommand(String input) throws Exception {
        String[] parts = input.split("\\s+");
        String command = parts[0].toLowerCase();
        
        switch (command) {
            case "help" -> showHelp();
            case "redraw" -> redrawBoard();
            case "leave" -> leaveGame();
            case "move" -> makeMove(parts);
            case "resign" -> resignGame();
            case "highlight" -> highlightLegalMoves(parts);
            case "quit" -> leaveGame();
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void showHelp() {
        System.out.println("\nAvailable commands:");
        System.out.println("  help                    - Show this help message");
        System.out.println("  redraw                  - Redraw the chess board");
        System.out.println("  leave                   - Leave the game");
        System.out.println("  move <start> <end>      - Make a move (e.g., move e2 e4)");
        System.out.println("  resign                  - Resign the game");
        System.out.println("  highlight <position>    - Highlight legal moves for a piece (e.g., highlight e2)");
        System.out.println("  quit                    - Leave the game");
    }

    private void redrawBoard() {
        if (currentGame != null) {
            BoardRenderer.renderBoard(currentGame, playerColor);
        } else {
            System.out.println("No game data available to display.");
        }
    }

    private void leaveGame() {
        try {
            webSocket.leave(authToken, gameID);
            webSocket.close();
            gameActive = false;
            System.out.println("You have left the game.");
        } catch (Exception e) {
            System.out.println("Error leaving game: " + e.getMessage());
        }
    }

    private void makeMove(String[] parts) throws Exception {
        if (playerColor == null) {
            System.out.println("Observers cannot make moves.");
            return;
        }
        
        if (parts.length != 3) {
            System.out.println("Usage: move <start> <end> (e.g., move e2 e4)");
            return;
        }
        
        ChessPosition start = parsePosition(parts[1]);
        ChessPosition end = parsePosition(parts[2]);
        
        if (start == null || end == null) {
            System.out.println("Invalid position format. Use format like 'e2' or 'a1'.");
            return;
        }
        
        ChessMove move = new ChessMove(start, end, null);
        webSocket.makeMove(authToken, gameID, move);
    }

    private void resignGame() throws Exception {
        if (playerColor == null) {
            System.out.println("Observers cannot resign.");
            return;
        }
        
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if (confirmation.equals("yes") || confirmation.equals("y")) {
            webSocket.resign(authToken, gameID);
            System.out.println("You have resigned the game.");
        } else {
            System.out.println("Resignation cancelled.");
        }
    }

    private void highlightLegalMoves(String[] parts) {
        if (parts.length != 2) {
            System.out.println("Usage: highlight <position> (e.g., highlight e2)");
            return;
        }
        
        ChessPosition position = parsePosition(parts[1]);
        if (position == null) {
            System.out.println("Invalid position format. Use format like 'e2' or 'a1'.");
            return;
        }
        
        if (currentGame == null) {
            System.out.println("No game data available.");
            return;
        }
        
        Collection<ChessMove> legalMoves = currentGame.validMoves(position);
        if (legalMoves.isEmpty()) {
            System.out.println("No legal moves available for piece at " + parts[1]);
        } else {
            System.out.println("Legal moves for piece at " + parts[1] + ":");
            for (ChessMove move : legalMoves) {
                System.out.println("  " + move.getStartPosition() + " -> " + move.getEndPosition());
            }
        }
    }

    private ChessPosition parsePosition(String positionStr) {
        if (positionStr.length() != 2) {
            return null;
        }
        
        char colChar = positionStr.charAt(0);
        char rowChar = positionStr.charAt(1);
        
        if (colChar < 'a' || colChar > 'h' || rowChar < '1' || rowChar > '8') {
            return null;
        }
        
        int col = colChar - 'a' + 1;
        int row = rowChar - '0';
        
        return new ChessPosition(row, col);
    }

    @Override
    public void updateGame(ChessGame game) {
        this.currentGame = game;
        System.out.println("\nGame updated:");
        BoardRenderer.renderBoard(game, playerColor);
    }

    @Override
    public void printMessage(String message) {
        System.out.println("\n" + message);
    }
} 