package chess;

import chess.ChessPiece.PieceType;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    private final ChessPosition startPosition;
    private final ChessPosition endPosition;
    private final PieceType promotionPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
        ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChessMove that = (ChessMove) o;

        return startPosition.equals(that.startPosition) &&
            endPosition.equals(that.endPosition) &&
            ((promotionPiece == null && that.promotionPiece == null) || 
            (promotionPiece != null && promotionPiece.equals(that.promotionPiece)));
    }

    @Override
    public int hashCode() {
    int result = startPosition.hashCode();
    result = 31 * result + endPosition.hashCode();
    result = 31 * result + (promotionPiece != null ? promotionPiece.hashCode() : 0);
    return result;
}



    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }
}
