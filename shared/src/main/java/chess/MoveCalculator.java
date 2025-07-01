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

    private static Collection<ChessMove> calcKingMoves( ChessPosition position, ChessBoard board) {
        Collection<ChessMove> moves = new ArrayDeque<>();

        int row = position.getRow();
        int col = position.getColumn();
        ChessPosition newPosition = new ChessPosition(row + 1, col + 1);

        ChessPiece pieceAtNew = board.getPiece(newPosition);
        if (pieceAtNew == null) {
            ChessMove newMove = new ChessMove(position, newPosition, null);
            moves.add(newMove);
        }


        return moves;
    }

    private static Collection<ChessMove> calcQueenMoves( ChessPosition position, ChessBoard board) {
        
    }

    private static Collection<ChessMove> calcBishopMoves( ChessPosition position, ChessBoard board) {
        
    }

    private static Collection<ChessMove> calcKnightMoves( ChessPosition position, ChessBoard board) {
        
    }

    private static Collection<ChessMove> calcRookMoves( ChessPosition position, ChessBoard board) {
        
    }

    private static Collection<ChessMove> calcPawnMoves( ChessPosition position, ChessBoard board) {
        
    }

}
