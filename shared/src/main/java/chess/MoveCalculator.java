package chess;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;

import chess.ChessMove;

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

    private static boolean isInBounds(int row, int col) {
    return row >= 1 && row <= 8 && col >= 1 && col <= 8;
}

    private static Collection<ChessMove> calcKingMoves(ChessPiece piece, ChessPosition position, ChessBoard board) {
        Collection<ChessMove> moves = new ArrayList<>();

        ChessGame.TeamColor pieceColor = piece.getTeamColor();

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

                if (pieceAtNew == null || pieceAtNew.getTeamColor() != pieceColor) {
                moves.add(new ChessMove(position, newPosition, null));
                }
            }
        }
    
    System.out.println(moves);
    return moves;
}


private static Collection<ChessMove> calcQueenMoves(ChessPiece piece,
    ChessPosition position,
    ChessBoard board) {
    Collection<ChessMove> moves = new ArrayList<>();

    int startRow = position.getRow();
    int startCol = position.getColumn();
    ChessGame.TeamColor myColor = piece.getTeamColor();

    // The 8 compass directions: N, NE, E, SE, S, SW, W, NW
    int[] dr = {  1,  1,  0, -1, -1, -1,  0,  1 };
    int[] dc = {  0,  1,  1,  1,  0, -1, -1, -1 };

    for (int dir = 0; dir < 8; dir++) {
        int r = startRow + dr[dir];
        int c = startCol + dc[dir];

        // keep stepping in this direction until we fall off or hit a piece
        while (isInBounds(r, c)) {
            ChessPosition to = new ChessPosition(r, c);
            ChessPiece occ = board.getPiece(to);

            if (occ == null) {
                // empty square → legal move
                moves.add(new ChessMove(position, to, null));
            } else {
                // occupied: if enemy, capture; then stop sliding
                if (occ.getTeamColor() != myColor) {
                    moves.add(new ChessMove(position, to, null));
                }
                break;
            }

            r += dr[dir];
            c += dc[dir];
        }
    }

    return moves;
}

    private static Collection<ChessMove> calcBishopMoves(ChessPiece piece, ChessPosition position, ChessBoard board) {
    Collection<ChessMove> moves = new ArrayList<>();
    ChessGame.TeamColor myColor = piece.getTeamColor();
    int startRow = position.getRow();
    int startCol = position.getColumn();

    int[] dr = {  1,  1, -1, -1 };
    int[] dc = {  1, -1,  1, -1 };

    for (int dir = 0; dir < 4; dir++) {
        int r = startRow + dr[dir];
        int c = startCol + dc[dir];
        while (isInBounds(r, c)) {
            ChessPosition to = new ChessPosition(r, c);
            ChessPiece occ = board.getPiece(to);
            if (occ == null) {
                moves.add(new ChessMove(position, to, null));
            } else {
                if (occ.getTeamColor() != myColor) {
                    moves.add(new ChessMove(position, to, null));
                }
                break;
            }
            r += dr[dir];
            c += dc[dir];
        }
    }

    return moves;
}

    private static Collection<ChessMove> calcKnightMoves(ChessPiece piece,  ChessPosition position, ChessBoard board) {
        Collection<ChessMove> moves = new ArrayList<>();

        ChessGame.TeamColor pieceColor = piece.getTeamColor();


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

                if (pieceAtNew == null || pieceAtNew.getTeamColor() != pieceColor) {
                moves.add(new ChessMove(position, newPosition, null));
                }
            }
        }

    return moves;
    }

    private static Collection<ChessMove> calcRookMoves(ChessPiece piece, ChessPosition position, ChessBoard board) {
    Collection<ChessMove> moves = new ArrayList<>();
    ChessGame.TeamColor myColor = piece.getTeamColor();
    int startRow = position.getRow();
    int startCol = position.getColumn();

    int[] dr = {  1, -1,  0,  0 };
    int[] dc = {  0,  0,  1, -1 };

    for (int dir = 0; dir < 4; dir++) {
        int r = startRow + dr[dir];
        int c = startCol + dc[dir];
        while (isInBounds(r, c)) {
            ChessPosition to = new ChessPosition(r, c);
            ChessPiece occ = board.getPiece(to);
            if (occ == null) {
                moves.add(new ChessMove(position, to, null));
            } else {
                if (occ.getTeamColor() != myColor) {
                    moves.add(new ChessMove(position, to, null));
                }
                break;
            }
            r += dr[dir];
            c += dc[dir];
        }
    }

    return moves;
}

    private static Collection<ChessMove> calcPawnMoves(ChessPiece piece, ChessPosition position, ChessBoard board) {
    Collection<ChessMove> moves = new ArrayList<>();
    ChessGame.TeamColor color = piece.getTeamColor();
    int row = position.getRow();
    int col = position.getColumn();

    int forward = (color == ChessGame.TeamColor.WHITE) ? 1 : -1;
    int startRow = (color == ChessGame.TeamColor.WHITE) ? 2 : 7;

    int oneRow = row + forward;
    if (isInBounds(oneRow, col) && board.getPiece(new ChessPosition(oneRow, col)) == null) {
        moves.add(new ChessMove(position, new ChessPosition(oneRow, col), null));

        int twoRow = row + 2*forward;
        if (row == startRow
         && isInBounds(twoRow, col)
         && board.getPiece(new ChessPosition(twoRow, col)) == null) {
            moves.add(new ChessMove(position, new ChessPosition(twoRow, col), null));
        }
    }

    for (int dc : new int[]{-1, +1}) {
        int c2 = col + dc;
        if (!isInBounds(oneRow, c2)) continue;
        ChessPosition diag = new ChessPosition(oneRow, c2);
        ChessPiece occ = board.getPiece(diag);
        if (occ != null && occ.getTeamColor() != color) {
            moves.add(new ChessMove(position, diag, null));
        }
    }

    return moves;
}

}
