package chess;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Objects;

public class ChessGame {
    private ChessBoard board;
    private TeamColor currentTurnColor;
    private boolean whiteKingMoved, blackKingMoved;
    private boolean whiteRookAMoved, whiteRookHMoved;
    private boolean blackRookAMoved, blackRookHMoved;
    private ChessMove lastMove;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return whiteKingMoved == chessGame.whiteKingMoved &&
                blackKingMoved == chessGame.blackKingMoved &&
                whiteRookAMoved == chessGame.whiteRookAMoved &&
                whiteRookHMoved == chessGame.whiteRookHMoved &&
                blackRookAMoved == chessGame.blackRookAMoved &&
                blackRookHMoved == chessGame.blackRookHMoved &&
                Objects.equals(board, chessGame.board) &&
                currentTurnColor == chessGame.currentTurnColor &&
                Objects.equals(lastMove, chessGame.lastMove);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "board=" + board +
                ", currentTurnColor=" + currentTurnColor +
                ", whiteKingMoved=" + whiteKingMoved +
                ", blackKingMoved=" + blackKingMoved +
                ", whiteRookAMoved=" + whiteRookAMoved +
                ", whiteRookHMoved=" + whiteRookHMoved +
                ", blackRookAMoved=" + blackRookAMoved +
                ", blackRookHMoved=" + blackRookHMoved +
                ", lastMove=" + lastMove +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTurnColor, whiteKingMoved, blackKingMoved, whiteRookAMoved, whiteRookHMoved, blackRookAMoved, blackRookHMoved, lastMove);
    }

    public ChessGame() {
        this.board = new ChessBoard();
        board.resetBoard();
        this.currentTurnColor = TeamColor.WHITE;
    }

    public TeamColor getTeamTurn() {
        return currentTurnColor;
    }

    public void setTeamTurn(TeamColor team) {
        this.currentTurnColor = team;
    }

    public enum TeamColor { WHITE, BLACK }

    private static List<ChessPosition> getAllPositions() {
        List<ChessPosition> positions = new ArrayList<>();
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                positions.add(new ChessPosition(r, c));
            }
        }
        return positions;
    }

    private ChessPosition findKingPosition(ChessBoard board, TeamColor teamColor) {
        for (ChessPosition pos : getAllPositions()) {
            ChessPiece cp = board.getPiece(pos);
            if (cp != null && cp.getTeamColor() == teamColor && cp.getPieceType() == ChessPiece.PieceType.KING) {
                return pos;
            }
        }
        return null;
    }

    private boolean isUnderAttack(ChessBoard board, ChessPosition target, TeamColor attackerColor) {
        for (ChessPosition from : getAllPositions()) {
            ChessPiece cp = board.getPiece(from);
            if (cp != null && cp.getTeamColor() == attackerColor) {
                for (ChessMove m : cp.pieceMoves(board, from)) {
                    if (m.getEndPosition().equals(target)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasAnyValidMoves(ChessBoard board, TeamColor teamColor) {
        for (ChessPosition pos : getAllPositions()) {
            ChessPiece cp = board.getPiece(pos);
            if (cp != null && cp.getTeamColor() == teamColor && !validMoves(pos).isEmpty()) {
                return true;
            }
        }
        return false;
    }

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

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            ChessPosition ep = getEnPassantTarget();
            if (ep != null
                    && Math.abs(ep.getColumn() - startPosition.getColumn()) == 1
                    && ep.getRow() - startPosition.getRow() == (color == TeamColor.WHITE ? 1 : -1)) {
                allMoves.add(new ChessMove(startPosition, ep, null));
            }
        }

        TeamColor opponent = (color == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        for (ChessMove move : allMoves) {
            ChessBoard scratch = board.createScratchBoard();
            ChessPiece moved = scratch.getPiece(startPosition);
            scratch.addPiece(startPosition, null);
            if (move.getPromotionPiece() != null) {
                scratch.addPiece(move.getEndPosition(), new ChessPiece(color, move.getPromotionPiece()));
            } else {
                scratch.addPiece(move.getEndPosition(), moved);
            }
            ChessPosition kingPos = findKingPosition(scratch, color);
            if (kingPos != null && !isUnderAttack(scratch, kingPos, opponent)) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (move == null) throw new InvalidMoveException("Move cannot be null");
        ChessPosition from = move.getStartPosition();
        ChessPosition to = move.getEndPosition();
        ChessPiece piece = board.getPiece(from);
        if (piece == null) {
            throw new InvalidMoveException("No piece at " + from);
        }
        if (piece.getTeamColor() != currentTurnColor) {
            throw new InvalidMoveException("It's " + currentTurnColor + "'s turn");
        }
        Collection<ChessMove> legal = validMoves(from);
        if (!legal.contains(move)) {
            throw new InvalidMoveException("Illegal move from " + from + " to " + to);
        }

        int fromRow = from.getRow(), fromCol = from.getColumn();
        int toRow = to.getRow(), toCol = to.getColumn();
        boolean isEnPassant = piece.getPieceType() == ChessPiece.PieceType.PAWN
                && getEnPassantTarget() != null
                && to.equals(getEnPassantTarget());
        boolean isCastle = piece.getPieceType() == ChessPiece.PieceType.KING
                && Math.abs(toCol - fromCol) == 2;

        ChessPiece.PieceType promo = move.getPromotionPiece();
        if (promo != null) {
            if (piece.getPieceType() != ChessPiece.PieceType.PAWN) {
                throw new InvalidMoveException("Only pawns can promote");
            }
            boolean validRank = (piece.getTeamColor() == TeamColor.WHITE && toRow == 8)
                    || (piece.getTeamColor() == TeamColor.BLACK && toRow == 1);
            if (!validRank) {
                throw new InvalidMoveException("Pawn may only promote on the last rank");
            }
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
            ChessPosition rf, rt;
            if (toCol > fromCol) {
                rf = new ChessPosition(fromRow, 8);
                rt = new ChessPosition(fromRow, 6);
            } else {
                rf = new ChessPosition(fromRow, 1);
                rt = new ChessPosition(fromRow, 4);
            }
            ChessPiece rook = board.getPiece(rf);
            board.addPiece(rt, rook);
            board.addPiece(rf, null);
        }

        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (piece.getTeamColor() == TeamColor.WHITE) {
                whiteKingMoved = true;
            } else {
                blackKingMoved = true;
            }
        }
        if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            if (piece.getTeamColor() == TeamColor.WHITE) {
                if (from.equals(new ChessPosition(1, 1))) {
                    whiteRookAMoved = true;
                }
                if (from.equals(new ChessPosition(1, 8))) {
                    whiteRookHMoved = true;
                }
            } else {
                if (from.equals(new ChessPosition(8, 1))) {
                    blackRookAMoved = true;
                }
                if (from.equals(new ChessPosition(8, 8))) {
                    blackRookHMoved = true;
                }
            }
        }

        lastMove = move;
        currentTurnColor = (currentTurnColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = findKingPosition(board, teamColor);
        if (kingPos == null) {
            return false;
        }
        TeamColor opponent = (teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        return isUnderAttack(board, kingPos, opponent);
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && !hasAnyValidMoves(board, teamColor);
    }

    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && !hasAnyValidMoves(board, teamColor);
    }

    public void setBoard(ChessBoard b) {
        board = b;
        whiteKingMoved = blackKingMoved = false;
        whiteRookAMoved = whiteRookHMoved = false;
        blackRookAMoved = blackRookHMoved = false;
        lastMove = null;
    }

    public ChessBoard getBoard() {
        return board;
    }

    public boolean canCastleKingside(TeamColor c) {
        int row = (c == TeamColor.WHITE ? 1 : 8);
        TeamColor opp = (c == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        if ((c == TeamColor.WHITE && (whiteKingMoved || whiteRookHMoved)) ||
                (c == TeamColor.BLACK && (blackKingMoved || blackRookHMoved))) {
            return false;
        }
        if (board.getPiece(new ChessPosition(row, 6)) != null ||
                board.getPiece(new ChessPosition(row, 7)) != null) {
            return false;
        }
        if (isInCheck(c)) {
            return false;
        }
        if (isUnderAttack(board, new ChessPosition(row, 6), opp) ||
                isUnderAttack(board, new ChessPosition(row, 7), opp)) {
            return false;
        }
        return true;
    }

    public boolean canCastleQueenside(TeamColor c) {
        int row = (c == TeamColor.WHITE ? 1 : 8);
        TeamColor opp = (c == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        if ((c == TeamColor.WHITE && (whiteKingMoved || whiteRookAMoved)) ||
                (c == TeamColor.BLACK && (blackKingMoved || blackRookAMoved))) {
            return false;
        }
        if (board.getPiece(new ChessPosition(row, 2)) != null ||
                board.getPiece(new ChessPosition(row, 3)) != null ||
                board.getPiece(new ChessPosition(row, 4)) != null) {
            return false;
        }
        if (isInCheck(c)) {
            return false;
        }
        if (isUnderAttack(board, new ChessPosition(row, 4), opp) ||
                isUnderAttack(board, new ChessPosition(row, 3), opp)) {
            return false;
        }
        return true;
    }

    public ChessPosition getEnPassantTarget() {
        if (lastMove == null) {
            return null;
        }
        ChessPiece p = board.getPiece(lastMove.getEndPosition());
        if (p == null || p.getPieceType() != ChessPiece.PieceType.PAWN) {
            return null;
        }
        int dr = lastMove.getEndPosition().getRow() - lastMove.getStartPosition().getRow();
        if (Math.abs(dr) != 2) {
            return null;
        }
        int midRow = (lastMove.getStartPosition().getRow() + lastMove.getEndPosition().getRow()) / 2;
        return new ChessPosition(midRow, lastMove.getEndPosition().getColumn());
    }
}
