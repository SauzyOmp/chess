package chess;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;

import chess.ChessMove;

public class MoveCalculator {
    public static Collection<ChessMove> generateMoves(ChessPiece piece, ChessPosition position, ChessBoard board) {
        switch (piece.getPieceType()) {
            case KING:
                return calcKingMoves(position, board);

            case QUEEN:
                return calcQueenMoves(position, board);
            
            case BISHOP:
                return calcBishopMoves(position, board);
            
            case KNIGHT:
                return calcKnightMoves(position, board);

            case ROOK:
                return calcRookMoves(position, board);

            case PAWN:
                return calcPawnMoves(position, board);
        
            default:
                return new ArrayList<>();
        }
    }

    private static boolean isInBounds(int row, int col) {
    return row >= 0 && row < 8 && col >= 0 && col < 8;
}

    private static Collection<ChessMove> calcKingMoves(ChessPosition position, ChessBoard board) {
        Collection<ChessMove> moves = new ArrayList<>();

        int currentRow = position.getRow();
        int currentCol = position.getColumn();

        int[] rowOffsets = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] colOffsets = {-1,  0,  1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            int newRow = currentRow + rowOffsets[i];
            int newCol = currentCol + colOffsets[i];

            if (isInBounds(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtNew = board.getPiece(newPosition);

                if (pieceAtNew == null) {
                    ChessMove newMove = new ChessMove(position, newPosition, null);
                    moves.add(newMove);
                }
            }
        }

    return moves;
}


    private static Collection<ChessMove> calcQueenMoves( ChessPosition position, ChessBoard board) {
        Collection<ChessMove> moves = new ArrayList<>();

        int currentRow = position.getRow();
        int currentCol = position.getColumn();

        int[] rowOffsets = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] colOffsets = {-1,  0,  1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            int newRow = currentRow + rowOffsets[i];
            int newCol = currentCol + colOffsets[i];

            if (isInBounds(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtNew = board.getPiece(newPosition);

                if (pieceAtNew == null) {
                    ChessMove newMove = new ChessMove(position, newPosition, null);
                    moves.add(newMove);
                }
            }
        }

    return moves;
    }

    private static Collection<ChessMove> calcBishopMoves( ChessPosition position, ChessBoard board) {
        Collection<ChessMove> moves = new ArrayList<>();

        int currentRow = position.getRow();
        int currentCol = position.getColumn();

        int[] rowOffsets = {-1, -1, 1, 1};
        int[] colOffsets = {-1,  1, -1, 1};

        for (int i = 0; i < 8; i++) {
            int newRow = currentRow + rowOffsets[i];
            int newCol = currentCol + colOffsets[i];

            if (isInBounds(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtNew = board.getPiece(newPosition);

                if (pieceAtNew == null) {
                    ChessMove newMove = new ChessMove(position, newPosition, null);
                    moves.add(newMove);
                }
            }
        }

    return moves;
    }

    private static Collection<ChessMove> calcKnightMoves( ChessPosition position, ChessBoard board) {
        Collection<ChessMove> moves = new ArrayList<>();

        int currentRow = position.getRow();
        int currentCol = position.getColumn();

        int[] rowOffsets = {-2, -1, 1, 2,  2,  1, -1, -2};
        int[] colOffsets = { 1,  2, 2, 1, -1, -2, -2, -1};

        for (int i = 0; i < 8; i++) {
            int newRow = currentRow + rowOffsets[i];
            int newCol = currentCol + colOffsets[i];

            if (isInBounds(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtNew = board.getPiece(newPosition);

                if (pieceAtNew == null) {
                    ChessMove newMove = new ChessMove(position, newPosition, null);
                    moves.add(newMove);
                }
            }
        }

    return moves;
    }

    private static Collection<ChessMove> calcRookMoves( ChessPosition position, ChessBoard board) {
        Collection<ChessMove> moves = new ArrayList<>();

        int currentRow = position.getRow();
        int currentCol = position.getColumn();

        int[] rowOffsets = {-1, 1, 0, 0};
        int[] colOffsets = {0, 0, -1, 1};

        for (int i = 0; i < 8; i++) {
            int newRow = currentRow + rowOffsets[i];
            int newCol = currentCol + colOffsets[i];

            if (isInBounds(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtNew = board.getPiece(newPosition);

                if (pieceAtNew == null) {
                    ChessMove newMove = new ChessMove(position, newPosition, null);
                    moves.add(newMove);
                }
            }
        }

    return moves;
    }

    private static Collection<ChessMove> calcPawnMoves( ChessPosition position, ChessBoard board) {
        Collection<ChessMove> moves = new ArrayList<>();

        int currentRow = position.getRow();
        int currentCol = position.getColumn();

        int[] rowOffsets = {1};
        int[] colOffsets = {0};

        for (int i = 0; i < 8; i++) {
            int newRow = currentRow + rowOffsets[i];
            int newCol = currentCol + colOffsets[i];

            if (isInBounds(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtNew = board.getPiece(newPosition);

                if (pieceAtNew == null) {
                    ChessMove newMove = new ChessMove(position, newPosition, null);
                    moves.add(newMove);
                }
            }
        }

    return moves;
    }

}
