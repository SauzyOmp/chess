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

    private boolean whiteKingMoved, blackKingMoved;
    private boolean whiteRookAMoved, whiteRookHMoved;
    private boolean blackRookAMoved, blackRookHMoved;
    private ChessMove lastMove;

    public ChessGame() {
    this.board = new ChessBoard();
    board.resetBoard();
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
    ChessPiece piece = board.getPiece(startPosition);
    if (piece == null) return validMoves;

    Collection<ChessMove> allMoves = new ArrayList<>(piece.pieceMoves(board, startPosition));
    TeamColor color = piece.getTeamColor();

    int startRow = (color == TeamColor.WHITE ? 1 : 8);
    if (piece.getPieceType() == ChessPiece.PieceType.KING
    && startPosition.getRow() == startRow
    && startPosition.getColumn() == 5) {
    if (canCastleKingside(color)) {
        allMoves.add(new ChessMove(startPosition, new ChessPosition(startRow, 7), null));
    }
    if (canCastleQueenside(color)) {
        allMoves.add(new ChessMove(startPosition, new ChessPosition(startRow, 3), null));
    }
    }



    // en passant
    if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
        ChessPosition ep = getEnPassantTarget();
        if (ep != null
            && Math.abs(ep.getColumn() - startPosition.getColumn()) == 1
            && ep.getRow() - startPosition.getRow() == (color == TeamColor.WHITE ? 1 : -1)) {
            allMoves.add(new ChessMove(startPosition, ep, null));
        }
    }

    // filter out any move that would leave the king in check
    for (ChessMove move : allMoves) {
        ChessBoard scratch = board.createScratchBoard();
        ChessPiece moved = scratch.getPiece(startPosition);
        scratch.addPiece(startPosition, null);
        if (move.getPromotionPiece() != null) {
            scratch.addPiece(move.getEndPosition(),
                new ChessPiece(color, move.getPromotionPiece()));
        } else {
            scratch.addPiece(move.getEndPosition(), moved);
        }

        ChessPosition kingPos = null;
        outer:
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition p = new ChessPosition(r, c);
                ChessPiece cp = scratch.getPiece(p);
                if (cp != null
                 && cp.getTeamColor() == color
                 && cp.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPos = p;
                    break outer;
                }
            }
        }
        if (kingPos == null) continue;

        boolean inCheck = false;
        outer2:
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition p = new ChessPosition(r, c);
                ChessPiece cp = scratch.getPiece(p);
                if (cp != null && cp.getTeamColor() != color) {
                    for (ChessMove opp : cp.pieceMoves(scratch, p)) {
                        if (opp.getEndPosition().equals(kingPos)) {
                            inCheck = true;
                            break outer2;
                        }
                    }
                }
            }
        }

        if (!inCheck) {
            validMoves.add(move);
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
    if (move == null) throw new InvalidMoveException("Move cannot be null");
    ChessPosition from = move.getStartPosition();
    ChessPosition to = move.getEndPosition();
    ChessPiece piece = board.getPiece(from);
    if (piece == null) throw new InvalidMoveException("No piece at " + from);
    if (piece.getTeamColor() != currentTurnColor) throw new InvalidMoveException("It's " + currentTurnColor + "'s turn");
    Collection<ChessMove> legal = validMoves(from);
    if (!legal.contains(move)) throw new InvalidMoveException("Illegal move from " + from + " to " + to);

    int fromRow = from.getRow(), fromCol = from.getColumn();
    int toRow = to.getRow(), toCol = to.getColumn();
    boolean isEnPassant = piece.getPieceType() == ChessPiece.PieceType.PAWN
        && getEnPassantTarget() != null
        && to.equals(getEnPassantTarget());
    boolean isCastle = piece.getPieceType() == ChessPiece.PieceType.KING
        && Math.abs(toCol - fromCol) == 2;

    ChessPiece.PieceType promo = move.getPromotionPiece();
    if (promo != null) {
        if (piece.getPieceType() != ChessPiece.PieceType.PAWN)
            throw new InvalidMoveException("Only pawns can promote");
        boolean validRank = (piece.getTeamColor() == TeamColor.WHITE && toRow == 8)
                         || (piece.getTeamColor() == TeamColor.BLACK && toRow == 1);
        if (!validRank) throw new InvalidMoveException("Pawn may only promote on the last rank");
        board.addPiece(to, new ChessPiece(piece.getTeamColor(), promo));
    } else {
        board.addPiece(to, piece);
    }
    board.addPiece(from, null);

    if (isEnPassant) {
        ChessPosition cap = lastMove.getEndPosition();
        board.addPiece(cap, null);
    }

    if (isCastle) {
        int row = fromRow;
        if (toCol > fromCol) {
            ChessPosition rf = new ChessPosition(row, 8);
            ChessPosition rt = new ChessPosition(row, 6);
            ChessPiece rook = board.getPiece(rf);
            board.addPiece(rt, rook);
            board.addPiece(rf, null);
        } else {
            ChessPosition rf = new ChessPosition(row, 1);
            ChessPosition rt = new ChessPosition(row, 4);
            ChessPiece rook = board.getPiece(rf);
            board.addPiece(rt, rook);
            board.addPiece(rf, null);
        }
    }

    if (piece.getPieceType() == ChessPiece.PieceType.KING) {
        if (piece.getTeamColor() == TeamColor.WHITE) whiteKingMoved = true;
        else blackKingMoved = true;
    }
    if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
        if (piece.getTeamColor() == TeamColor.WHITE) {
            if (from.equals(new ChessPosition(1, 1))) whiteRookAMoved = true;
            if (from.equals(new ChessPosition(1, 8))) whiteRookHMoved = true;
        } else {
            if (from.equals(new ChessPosition(8, 1))) blackRookAMoved = true;
            if (from.equals(new ChessPosition(8, 8))) blackRookHMoved = true;
        }
    }

    lastMove = move;
    currentTurnColor = (currentTurnColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
}






    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
    ChessPosition kingPos = null;
    for (int r = 1; r <= 8; r++) {
        for (int c = 1; c <= 8; c++) {
            ChessPosition p = new ChessPosition(r, c);
            ChessPiece cp = board.getPiece(p);
            if (cp != null && cp.getTeamColor() == teamColor && cp.getPieceType() == ChessPiece.PieceType.KING) {
                kingPos = p;
                break;
            }
        }
        if (kingPos != null) break;
    }
    if (kingPos == null) return false;
    for (int r = 1; r <= 8; r++) {
        for (int c = 1; c <= 8; c++) {
            ChessPosition p = new ChessPosition(r, c);
            ChessPiece cp = board.getPiece(p);
            if (cp != null && cp.getTeamColor() != teamColor) {
                for (ChessMove m : cp.pieceMoves(board, p)) {
                    if (m.getEndPosition().equals(kingPos)) return true;
                }
            }
        }
    }
    return false;
}

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
    if (!isInCheck(teamColor)) return false;
    for (int r = 1; r <= 8; r++) {
        for (int c = 1; c <= 8; c++) {
            ChessPosition p = new ChessPosition(r, c);
            ChessPiece cp = board.getPiece(p);
            if (cp != null && cp.getTeamColor() == teamColor) {
                if (!validMoves(p).isEmpty()) return false;
            }
        }
    }
    return true;
}

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
    if (isInCheck(teamColor)) return false;
    for (int r = 1; r <= 8; r++) {
        for (int c = 1; c <= 8; c++) {
            ChessPosition p = new ChessPosition(r, c);
            ChessPiece cp = board.getPiece(p);
            if (cp != null && cp.getTeamColor() == teamColor) {
                if (!validMoves(p).isEmpty()) return false;
            }
        }
    }
    return true;
}


    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard b) {
        board = b;
        whiteKingMoved = blackKingMoved = false;
        whiteRookAMoved = whiteRookHMoved = false;
        blackRookAMoved = blackRookHMoved = false;
        lastMove = null;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    private boolean isUnderAttack(ChessPosition pos, TeamColor byColor) {
    for (int r = 1; r <= 8; r++) {
        for (int c = 1; c <= 8; c++) {
            ChessPosition p = new ChessPosition(r, c);
            ChessPiece cp = board.getPiece(p);
            if (cp != null && cp.getTeamColor() == byColor) {
                for (ChessMove m : cp.pieceMoves(board, p)) {
                    if (m.getEndPosition().equals(pos)) return true;
                }
            }
        }
    }
    return false;
}

public boolean canCastleKingside(TeamColor c) {
    int row = (c == TeamColor.WHITE ? 1 : 8);
    TeamColor opp = (c == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    if ((c == TeamColor.WHITE && (whiteKingMoved || whiteRookHMoved)) ||
        (c == TeamColor.BLACK && (blackKingMoved || blackRookHMoved))) return false;
    if (board.getPiece(new ChessPosition(row, 6)) != null ||
        board.getPiece(new ChessPosition(row, 7)) != null) return false;
    if (isInCheck(c)) return false;
    if (isUnderAttack(new ChessPosition(row, 6), opp) ||
        isUnderAttack(new ChessPosition(row, 7), opp)) return false;
    return true;
}

public boolean canCastleQueenside(TeamColor c) {
    int row = (c == TeamColor.WHITE ? 1 : 8);
    TeamColor opp = (c == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    if ((c == TeamColor.WHITE && (whiteKingMoved || whiteRookAMoved)) ||
        (c == TeamColor.BLACK && (blackKingMoved || blackRookAMoved))) return false;
    if (board.getPiece(new ChessPosition(row, 2)) != null ||
        board.getPiece(new ChessPosition(row, 3)) != null ||
        board.getPiece(new ChessPosition(row, 4)) != null) return false;
    if (isInCheck(c)) return false;
    if (isUnderAttack(new ChessPosition(row, 4), opp) ||
        isUnderAttack(new ChessPosition(row, 3), opp)) return false;
    return true;
}


    public ChessPosition getEnPassantTarget() {
        if (lastMove == null) return null;
        ChessPiece p = board.getPiece(lastMove.getEndPosition());
        if (p == null || p.getPieceType() != ChessPiece.PieceType.PAWN) return null;
        int dr = lastMove.getEndPosition().getRow() - lastMove.getStartPosition().getRow();
        if (Math.abs(dr) != 2) return null;
        int midRow = (lastMove.getStartPosition().getRow() + lastMove.getEndPosition().getRow())/2;
        return new ChessPosition(midRow, lastMove.getEndPosition().getColumn());
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
