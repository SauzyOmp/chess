package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class BoardRenderer {
    public static void renderBoard(ChessGame game) {
        renderBoard(game, null);
    }
    
    public static void renderBoard(ChessGame game, ChessGame.TeamColor perspective) {
        ChessBoard board = game.getBoard();
        
        // Print column headers
        System.out.print("   ");
        for (int col = 1; col <= 8; col++) {
            System.out.print(" " + (char)('a' + col - 1) + " ");
        }
        System.out.println();
        
        // Print board rows
        for (int row = 8; row >= 1; row--) {
            System.out.print(" " + row + " ");
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                
                // Alternate background colors for checkered pattern
                boolean isLightSquare = (row + col) % 2 == 0;
                String bgColor = isLightSquare ? EscapeSequences.SET_BG_COLOR_WHITE : EscapeSequences.SET_BG_COLOR_DARK_GREY;
                
                if (piece == null) {
                    System.out.print(bgColor + EscapeSequences.EMPTY + EscapeSequences.RESET_BG_COLOR);
                } else {
                    String pieceSymbol = getPieceSymbol(piece);
                    System.out.print(bgColor + pieceSymbol + EscapeSequences.RESET_BG_COLOR);
                }
            }
            System.out.println(" " + row);
        }
        
        // Print column headers again
        System.out.print("   ");
        for (int col = 1; col <= 8; col++) {
            System.out.print(" " + (char)('a' + col - 1) + " ");
        }
        System.out.println();
        
        // Print current turn
        System.out.println("Current turn: " + game.getTeamTurn());
    }
    
    private static String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 
                EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 
                EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 
                EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 
                EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 
                EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 
                EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
        };
    }
} 