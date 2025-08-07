package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.Collection;

public class BoardRenderer {
    public static void renderBoard(ChessGame game) {
        renderBoard(game, null);
    }

    public static void renderBoard(ChessGame game, ChessGame.TeamColor perspective) {
        renderBoard(game, perspective, null, null);
    }

    public static void renderBoard(ChessGame game,
                                   ChessGame.TeamColor perspective,
                                   ChessPosition selectedPosition,
                                   Collection<ChessMove> legalMoves) {
        ChessBoard board = game.getBoard();
        boolean flipBoard = perspective == ChessGame.TeamColor.BLACK;

        System.out.print("   ");
        for (char file = 'a'; file <= 'h'; file++) {
            System.out.print(" " + file + " ");
        }
        System.out.println();

        for (int i = 0; i < 8; i++) {
            int rank = flipBoard ? i + 1 : 8 - i;
            System.out.print(" " + rank + " ");

            for (int fileIndex = 1; fileIndex <= 8; fileIndex++) {
                int actualRow = rank;
                int actualCol = fileIndex;
                ChessPosition position = new ChessPosition(actualRow, actualCol);
                ChessPiece piece = board.getPiece(position);

                // Fix square coloring to alternate correctly
                boolean isLightSquare = (rank + fileIndex) % 2 == 1;
                String bgColor = isLightSquare
                        ? EscapeSequences.SET_BG_COLOR_WHITE
                        : EscapeSequences.SET_BG_COLOR_DARK_GREY;

                boolean isSelected = selectedPosition != null && position.equals(selectedPosition);
                boolean isLegal = legalMoves != null && legalMoves.stream()
                        .anyMatch(m -> m.getEndPosition().equals(position));

                if (isSelected) {
                    bgColor = EscapeSequences.SET_BG_COLOR_YELLOW;
                } else if (isLegal) {
                    bgColor = EscapeSequences.SET_BG_COLOR_GREEN;
                }

                String cell = piece == null
                        ? EscapeSequences.EMPTY
                        : getPieceSymbol(piece);

                System.out.print(bgColor + cell + EscapeSequences.RESET_BG_COLOR);
            }
            System.out.println(" " + rank);
        }

        System.out.print("   ");
        for (char file = 'a'; file <= 'h'; file++) {
            System.out.print(" " + file + " ");
        }
        System.out.println();

        System.out.println("Current turn: " + game.getTeamTurn());
    }

    private static String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? EscapeSequences.WHITE_KING
                    : EscapeSequences.BLACK_KING;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? EscapeSequences.WHITE_QUEEN
                    : EscapeSequences.BLACK_QUEEN;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? EscapeSequences.WHITE_BISHOP
                    : EscapeSequences.BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? EscapeSequences.WHITE_KNIGHT
                    : EscapeSequences.BLACK_KNIGHT;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? EscapeSequences.WHITE_ROOK
                    : EscapeSequences.BLACK_ROOK;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? EscapeSequences.WHITE_PAWN
                    : EscapeSequences.BLACK_PAWN;
        };
    }
}
