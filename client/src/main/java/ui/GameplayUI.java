package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import client.WebSocketFacade;
import exception.ResponseException;

import java.util.Collection;
import java.util.Scanner;
import chess.ChessPiece;

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
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Failed to connect to WebSocket: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    public void run() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Welcome to the chess game!" + EscapeSequences.RESET_TEXT_COLOR);
        if (playerColor != null) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "You are playing as " + playerColor + EscapeSequences.RESET_TEXT_COLOR);
        } else {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "You are observing the game" + EscapeSequences.RESET_TEXT_COLOR);
        }
        
        while (gameActive) {
            String prompt = "[" + username + " - Game " + gameID + "] > ";
            System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN + prompt + EscapeSequences.RESET_TEXT_COLOR);
            String input = scanner.nextLine().trim();
            
            try {
                processCommand(input);
            } catch (Exception e) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
            }
        }
    }

    private void processCommand(String input) throws Exception {
        String[] parts = input.split("\\s+");
        String command = parts[0].toLowerCase();
        
        switch (command) {
            case "help", "h" -> showHelp();
            case "redraw", "r" -> redrawBoard();
            case "leave", "l" -> leaveGame();
            case "move", "m" -> makeMove(parts);
            case "resign", "rs" -> resignGame();
            case "highlight", "hl" -> highlightLegalMoves(parts);
            case "quit", "q" -> leaveGame();
            default -> System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + 
                "Unknown command. Type 'help' for available commands." + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private void showHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + EscapeSequences.SET_TEXT_BOLD + 
            "Available commands:" + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + 
            "help" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (h)" + EscapeSequences.SET_TEXT_COLOR_WHITE + 
            " - Show this help message");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + 
            "redraw" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (r)" + EscapeSequences.SET_TEXT_COLOR_WHITE + 
            " - Redraw the chess board");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + 
            "move" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (m)" + EscapeSequences.SET_TEXT_COLOR_WHITE + 
            " - Make a move (interactive or: move <start> <end>)");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_GREEN + 
            "highlight <position>" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (hl)" + EscapeSequences.SET_TEXT_COLOR_WHITE + 
            " - Highlight legal moves for a piece (e.g., highlight e2)");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_RED + 
            "resign" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (rs)" + EscapeSequences.SET_TEXT_COLOR_WHITE + 
            " - Resign the game");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_RED + 
            "leave" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (l)" + EscapeSequences.SET_TEXT_COLOR_WHITE + 
            " - Leave the game");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + EscapeSequences.SET_TEXT_COLOR_RED + 
            "quit" + EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + " (q)" + EscapeSequences.SET_TEXT_COLOR_WHITE + 
            " - Leave the game");
    }

    private void redrawBoard() {
        if (currentGame != null) {
            BoardRenderer.renderBoard(currentGame, playerColor);
        } else {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "No game data available to display." + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private void leaveGame() {
        try {
            webSocket.leave(authToken, gameID);
            webSocket.close();
            gameActive = false;
            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "You have left the game." + EscapeSequences.RESET_TEXT_COLOR);
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error leaving game: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private void makeMove(String[] parts) throws Exception {
        if (playerColor == null) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Observers cannot make moves." + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }
        
        if (currentGame == null) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "No game data available." + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }
        
        // Check if it's the player's turn
        if (currentGame.getTeamTurn() != playerColor) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "It's not your turn. Current turn: " + currentGame.getTeamTurn() + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }
        
        ChessPosition start, end;
        
        if (parts.length == 3) {
            // Command format: move <start> <end>
            start = parsePosition(parts[1]);
            end = parsePosition(parts[2]);
        } else {
            // Interactive mode: prompt for positions
            System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN + "Enter start position (e.g., e2): " + EscapeSequences.RESET_TEXT_COLOR);
            String startInput = scanner.nextLine().trim();
            start = parsePosition(startInput);
            
            if (start == null) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid start position format. Use format like 'e2' or 'a1'." + EscapeSequences.RESET_TEXT_COLOR);
                return;
            }
            
            System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN + "Enter end position (e.g., e4): " + EscapeSequences.RESET_TEXT_COLOR);
            String endInput = scanner.nextLine().trim();
            end = parsePosition(endInput);
            
            if (end == null) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid end position format. Use format like 'e4' or 'a1'." + EscapeSequences.RESET_TEXT_COLOR);
                return;
            }
        }
        
        // Validate that the piece at start position belongs to the player
        ChessPiece piece = currentGame.getBoard().getPiece(start);
        if (piece == null) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "No piece at position " + formatPosition(start) + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }
        
        if (piece.getTeamColor() != playerColor) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "That piece doesn't belong to you. You are playing as " + playerColor + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }
        
        Collection<ChessMove> legalMoves = currentGame.validMoves(start);
        ChessMove move = null;
        
        for (ChessMove legalMove : legalMoves) {
            if (legalMove.getEndPosition().equals(end)) {
                move = legalMove;
                break;
            }
        }
        
        if (move == null) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid move. Use 'highlight " + formatPosition(start) + "' to see legal moves." + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }
        
        webSocket.makeMove(authToken, gameID, move);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Move submitted: " + formatPosition(start) + " to " + formatPosition(end) + EscapeSequences.RESET_TEXT_COLOR);
    }

    private void resignGame() throws Exception {
        if (playerColor == null) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Observers cannot resign." + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }
        
        System.out.print(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Are you sure you want to resign? (yes/no): " + EscapeSequences.RESET_TEXT_COLOR);
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if (confirmation.equals("yes") || confirmation.equals("y")) {
            webSocket.resign(authToken, gameID);
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "You have resigned the game." + EscapeSequences.RESET_TEXT_COLOR);
        } else {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Resignation cancelled." + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private void highlightLegalMoves(String[] parts) {
        if (parts.length != 2) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Usage: highlight <position> (e.g., highlight e2)" + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }
        
        ChessPosition position = parsePosition(parts[1]);
        if (position == null) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid position format. Use format like 'e2' or 'a1'." + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }
        
        if (currentGame == null) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "No game data available." + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }
        
        Collection<ChessMove> legalMoves = currentGame.validMoves(position);
        if (legalMoves.isEmpty()) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "No legal moves for " + parts[1] + EscapeSequences.RESET_TEXT_COLOR);
        } else {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Legal moves for " + parts[1] + ":" + EscapeSequences.RESET_TEXT_COLOR);
            BoardRenderer.renderBoard(currentGame, playerColor, position, legalMoves);
            
            // Show the moves in a simplified format
            for (ChessMove move : legalMoves) {
                String startPos = formatPosition(move.getStartPosition());
                String endPos = formatPosition(move.getEndPosition());
                System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + "  " + 
                    EscapeSequences.SET_TEXT_COLOR_GREEN + startPos + " -> " + endPos + EscapeSequences.RESET_TEXT_COLOR);
            }
        }
    }

    private String formatPosition(ChessPosition position) {
        char col = (char)('a' + position.getColumn() - 1);
        return col + String.valueOf(position.getRow());
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
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "\nGame updated:" + EscapeSequences.RESET_TEXT_COLOR);
        BoardRenderer.renderBoard(game, playerColor);
    }

    @Override
    public void printMessage(String message) {
        if (message.contains(" made move:")) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "\n" + message + EscapeSequences.RESET_TEXT_COLOR);
            if (currentGame != null) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Board updated:" + EscapeSequences.RESET_TEXT_COLOR);
                BoardRenderer.renderBoard(currentGame, playerColor);
            }
        } else {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "\n" + message + EscapeSequences.RESET_TEXT_COLOR);
        }
    }
} 