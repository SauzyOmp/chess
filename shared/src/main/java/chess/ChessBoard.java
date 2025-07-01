package chess;

public class ChessBoard {
    private final ChessPiece[][] board = new ChessPiece[8][8];

    public ChessBoard() { }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        int r = position.getRow() - 1;
        int c = position.getColumn() - 1;
        board[r][c] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        int r = position.getRow() - 1;
        int c = position.getColumn() - 1;
        if (r < 0 || r >= 8 || c < 0 || c >= 8) return null;
        return board[r][c];
    }

    /**
     * Sets the board to the default starting board
     */
    public void resetBoard() {
        throw new RuntimeException("Not implemented");
    }
}
