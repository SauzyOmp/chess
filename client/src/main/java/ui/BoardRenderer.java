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
        
        // Determine if we need to flip the board
        boolean flipBoard = (perspective == ChessGame.TeamColor.WHITE);
        
        System.out.print("   ");
        for (int col = 1; col <= 8; col++) {
            char colChar = flipBoard ? (char)('h' - col + 1) : (char)('a' + col - 1);
            System.out.print(" " + colChar + " ");
        }
        System.out.println();
        
        for (int displayRow = 1; displayRow <= 8; displayRow++) {
            int actualRow = flipBoard ? displayRow : (9 - displayRow);
            System.out.print(" " + displayRow + " ");
            for (int displayCol = 1; displayCol <= 8; displayCol++) {
                int actualCol = flipBoard ? (9 - displayCol) : displayCol;
                ChessPosition position = new ChessPosition(actualRow, actualCol);
                ChessPiece piece = board.getPiece(position);
                
                boolean isLightSquare = (displayRow + displayCol) % 2 == 0;
                String bgColor = isLightSquare ? EscapeSequences.SET_BG_COLOR_WHITE : EscapeSequences.SET_BG_COLOR_DARK_GREY;
                
                if (piece == null) {
                    System.out.print(bgColor + EscapeSequences.EMPTY + EscapeSequences.RESET_BG_COLOR);
                } else {
                    String pieceSymbol = getPieceSymbol(piece);
                    System.out.print(bgColor + pieceSymbol + EscapeSequences.RESET_BG_COLOR);
                }
            }
            System.out.println(" " + displayRow);
        }
        
        System.out.print("   ");
        for (int col = 1; col <= 8; col++) {
            char colChar = flipBoard ? (char)('h' - col + 1) : (char)('a' + col - 1);
            System.out.print(" " + colChar + " ");
        }
        System.out.println();
        
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