package chess;

import java.util.Collection;

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
                break;
        }
    }

    private static Collection<ChessMove> calcKingMoves( ChessPosition position, ChessBoard board) {
        Collection<ChessMove> moves = new Collection<ChessMove>();
        ChessPosition newPosition = new ChessPosition(position + 1, position + 1);
        if (position.getPiece = empty) {
            moves.add(newPosition)
        }
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
