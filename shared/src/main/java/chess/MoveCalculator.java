package chess;

import java.util.ArrayList;
import java.util.Collection;

public class MoveCalculator {
    public static Collection<ChessMove> generateMoves(ChessPiece piece, ChessPosition position, ChessBoard board) {
        switch (piece.getPieceType()) {
            case KING:
                return calcKingMoves(piece, position, board);
            case QUEEN:
                return calcQueenMoves(piece, position, board);
            case BISHOP:
                return calcBishopMoves(piece, position, board);
            case KNIGHT:
                return calcKnightMoves(piece, position, board);
            case ROOK:
                return calcRookMoves(piece, position, board);
            case PAWN:
                return calcPawnMoves(piece, position, board);
            default:
                return new ArrayList<>();
        }
    }

    private static final int[][] DIAGONALS = {
        { 1,  1}, { 1, -1}, {-1,  1}, {-1, -1}
    };

    private static final int[][] ORTHOGONALS = {
        { 1,  0}, {-1,  0}, { 0,  1}, { 0, -1}
    };

    private static Collection<ChessMove> slideMoves(ChessPiece piece, ChessPosition pos, ChessBoard board, int[][] directions) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessGame.TeamColor myColor = piece.getTeamColor();
        int r0 = pos.getRow(), c0 = pos.getColumn();
        for (int[] d : directions) {
            int r = r0 + d[0], c = c0 + d[1];
            while (isInBounds(r, c)) {
                ChessPosition to = new ChessPosition(r, c);
                ChessPiece occ = board.getPiece(to);
                if (occ == null) {
                    moves.add(new ChessMove(pos, to, null));
                } else {
                    if (occ.getTeamColor() != myColor) {
                        moves.add(new ChessMove(pos, to, null));
                    }
                    break;
                }
                r += d[0];
                c += d[1];
            }
        }
        return moves;
    }

    private static boolean isInBounds(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    private static Collection<ChessMove> calcKingMoves(ChessPiece piece, ChessPosition position, ChessBoard board) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessGame.TeamColor pieceColor = piece.getTeamColor();
        int currentRow = position.getRow(), currentCol = position.getColumn();
        int[] rowOffsets = { -1, -1, -1,  0, 0, 1, 1, 1 };
        int[] colOffsets = { -1,  0,  1, -1, 1, -1, 0, 1 };
        for (int i = 0; i < 8; i++) {
            int newRow = currentRow + rowOffsets[i], newCol = currentCol + colOffsets[i];
            if (isInBounds(newRow, newCol)) {
                ChessPosition to = new ChessPosition(newRow, newCol);
                ChessPiece occ = board.getPiece(to);
                if (occ == null || occ.getTeamColor() != pieceColor) {
                    moves.add(new ChessMove(position, to, null));
                }
            }
        }
        return moves;
    }

    private static Collection<ChessMove> calcBishopMoves(ChessPiece piece, ChessPosition position, ChessBoard board) {
        return slideMoves(piece, position, board, DIAGONALS);
    }

    private static Collection<ChessMove> calcKnightMoves(ChessPiece piece, ChessPosition position, ChessBoard board) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessGame.TeamColor pieceColor = piece.getTeamColor();
        int currentRow = position.getRow(), currentCol = position.getColumn();
        int[] rowOffsets = { -2, -1, 1, 2, 2, 1, -1, -2 };
        int[] colOffsets = {  1,  2, 2, 1, -1, -2, -2, -1 };
        for (int i = 0; i < 8; i++) {
            int newRow = currentRow + rowOffsets[i], newCol = currentCol + colOffsets[i];
            if (isInBounds(newRow, newCol)) {
                ChessPosition to = new ChessPosition(newRow, newCol);
                ChessPiece occ = board.getPiece(to);
                if (occ == null || occ.getTeamColor() != pieceColor) {
                    moves.add(new ChessMove(position, to, null));
                }
            }
        }
        return moves;
    }

    private static Collection<ChessMove> calcRookMoves(ChessPiece piece, ChessPosition position, ChessBoard board) {
        return slideMoves(piece, position, board, ORTHOGONALS);
    }

    private static Collection<ChessMove> calcQueenMoves(ChessPiece piece, ChessPosition position, ChessBoard board) {
        Collection<ChessMove> moves = new ArrayList<>();
        moves.addAll(slideMoves(piece, position, board, DIAGONALS));
        moves.addAll(slideMoves(piece, position, board, ORTHOGONALS));
        return moves;
    }

    private static Collection<ChessMove> calcPawnMoves(ChessPiece piece, ChessPosition position, ChessBoard board) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessGame.TeamColor color = piece.getTeamColor();
        int row = position.getRow(), col = position.getColumn();
        int forward = (color == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (color == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promoRow = (color == ChessGame.TeamColor.WHITE) ? 8 : 1;
        ChessPiece.PieceType[] promoTypes = {
            ChessPiece.PieceType.QUEEN,
            ChessPiece.PieceType.ROOK,
            ChessPiece.PieceType.BISHOP,
            ChessPiece.PieceType.KNIGHT
        };
        int r1 = row + forward;
        if (isInBounds(r1, col) && board.getPiece(new ChessPosition(r1, col)) == null) {
            if (r1 == promoRow) {
                for (ChessPiece.PieceType pt : promoTypes) {
                    moves.add(new ChessMove(position, new ChessPosition(r1, col), pt));
                }
            } else {
                moves.add(new ChessMove(position, new ChessPosition(r1, col), null));
                int r2 = row + 2 * forward;
                if (row == startRow && isInBounds(r2, col) && board.getPiece(new ChessPosition(r2, col)) == null) {
                    moves.add(new ChessMove(position, new ChessPosition(r2, col), null));
                }
            }
        }
        for (int dc : new int[]{ -1, 1 }) {
            int c1 = col + dc;
            if (!isInBounds(r1, c1)) continue;
            ChessPosition diag = new ChessPosition(r1, c1);
            ChessPiece occ = board.getPiece(diag);
            if (occ != null && occ.getTeamColor() != color) {
                if (r1 == promoRow) {
                    for (ChessPiece.PieceType pt : promoTypes) {
                        moves.add(new ChessMove(position, diag, pt));
                    }
                } else {
                    moves.add(new ChessMove(position, diag, null));
                }
            }
        }
        return moves;
    }
}
