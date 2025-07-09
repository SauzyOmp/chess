package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import chess.ChessBoard;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor currentTurnColor;

    public ChessGame() {
        this.board = new ChessBoard();
        this.currentTurnColor = TeamColor.WHITE;

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurnColor;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTurnColor = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        ChessPiece startPiece = board.getPiece(startPosition);
        if (startPiece == null) {
            return validMoves;
        }

        TeamColor pieceColor = startPiece.getTeamColor();

        for (ChessMove candidate : startPiece.pieceMoves(board, startPosition)) {
            ChessBoard scratch = board.createScratchBoard();

            ChessPiece moved = scratch.getPiece(startPosition);
            scratch.addPiece(startPosition, null);
            scratch.addPiece(candidate.getEndPosition(), moved);

            ChessPosition kingPos = null;
            outer:
            for (int r = 1; r <= 8; r++) {
                for (int c = 1; c <= 8; c++) {
                    ChessPosition p = new ChessPosition(r, c);
                    ChessPiece cp = scratch.getPiece(p);
                    if (cp != null
                        && cp.getTeamColor() == pieceColor
                        && cp.getPieceType() == ChessPiece.PieceType.KING) {
                        kingPos = p;
                        break outer;
                    }
                }
            }
            if (kingPos == null) {
                continue;
            }

            boolean kingInCheck = false;
            outer2:
            for (int r = 1; r <= 8; r++) {
                for (int c = 1; c <= 8; c++) {
                    ChessPosition p = new ChessPosition(r, c);
                    ChessPiece cp = scratch.getPiece(p);
                    if (cp != null && cp.getTeamColor() != pieceColor) {
                        for (ChessMove opp : cp.pieceMoves(scratch, p)) {
                            if (opp.getEndPosition().equals(kingPos)) {
                                kingInCheck = true;
                                break outer2;
                            }
                        }
                    }
                }
            }

            if (!kingInCheck) {
                validMoves.add(candidate);
            }
        }

        return validMoves;
    }


/**
 * Makes a move in a chess game
 *
 * @param move chess move to perform
 * @throws InvalidMoveException if move is invalid
 */
public void makeMove(ChessMove move) throws InvalidMoveException {
    if (move == null) {
        throw new InvalidMoveException("Move cannot be null");
    }

    ChessPosition from = move.getStartPosition();
    ChessPosition to   = move.getEndPosition();
    ChessPiece piece   = board.getPiece(from);
    if (piece == null) {
        throw new InvalidMoveException("No piece at " + from);
    }
    if (piece.getTeamColor() != currentTurnColor) {
        throw new InvalidMoveException("It's " + currentTurnColor + "'s turn");
    }
    Collection<ChessMove> legal = validMoves(from);
    if (!legal.contains(move)) {
        throw new InvalidMoveException(
            "Illegal move from " + from + " to " + to
        );
    }
    ChessPiece.PieceType promo = move.getPromotionPiece();
    if (promo != null) {
        if (piece.getPieceType() != ChessPiece.PieceType.PAWN) {
            throw new InvalidMoveException("Only pawns can promote");
        }
        int destRow = to.getRow();
        boolean validRank = (piece.getTeamColor() == TeamColor.WHITE && destRow == 8)
                         || (piece.getTeamColor() == TeamColor.BLACK && destRow == 1);
        if (!validRank) {
            throw new InvalidMoveException("Pawn may only promote on the last rank");
        }
        board.addPiece(to, new ChessPiece(piece.getTeamColor(), promo));
    } else {
        board.addPiece(to, piece);
    }
    board.addPiece(from, null);

    currentTurnColor = (currentTurnColor == TeamColor.WHITE)
        ? TeamColor.BLACK
        : TeamColor.WHITE;
}




    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessGame)) return false;
        ChessGame other = (ChessGame) o;
        return Objects.equals(this.board, other.board)
            && this.currentTurnColor == other.currentTurnColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTurnColor);
    }

}
