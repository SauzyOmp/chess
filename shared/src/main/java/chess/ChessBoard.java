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
     * @return the piece at that square
     */
    public ChessPiece getPiece(ChessPosition position) {
        int r = position.getRow() - 1;
        int c = position.getColumn() - 1;
        if (r < 0 || r >= 8 || c < 0 || c >= 8) return null;
        return board[r][c];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessBoard)) return false;
        ChessBoard other = (ChessBoard) o;

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece a = this.getPiece(new ChessPosition(row, col));
                ChessPiece b = other.getPiece(new ChessPosition(row, col));
                if (a == null ? b != null : !a.equals(b)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece p = getPiece(new ChessPosition(row, col));
                result = 31 * result + (p == null ? 0 : p.hashCode());
            }
        }
        return result;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 8; row >= 1; row--) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece p = getPiece(new ChessPosition(row, col));
                if (p == null) {
                    sb.append(".");
                } else {
                    char symbol;
                    switch (p.getPieceType()) {
                        case KING:   symbol = 'K'; break;
                        case QUEEN:  symbol = 'Q'; break;
                        case ROOK:   symbol = 'R'; break;
                        case BISHOP: symbol = 'B'; break;
                        case KNIGHT: symbol = 'N'; break;
                        case PAWN:   symbol = 'P'; break;
                        default:     symbol = '?'; break;
                    }
                    if (p.getTeamColor() == ChessGame.TeamColor.BLACK) {
                        symbol = Character.toLowerCase(symbol);
                    }
                    sb.append(symbol);
                }
                if (col < 8) sb.append(' ');
            }
            if (row > 1) sb.append('\n');
        }
        return sb.toString();
    }


    /**
     * Sets the board to the default starting board
     */
    public void resetBoard() {
    for (int r = 1; r <= 8; r++) {
        for (int c = 1; c <= 8; c++) {
            int rr = r - 1, cc = c - 1;
            board[rr][cc] = null;
        }
    }
    for (int col = 1; col <= 8; col++) {
        addPiece(new ChessPosition(1, col),
            new ChessPiece(ChessGame.TeamColor.WHITE, backRank[col-1]));
        addPiece(new ChessPosition(2, col),
            new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
    }

    for (int col = 1; col <= 8; col++) {
        addPiece(new ChessPosition(7, col),
            new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        addPiece(new ChessPosition(8, col),
            new ChessPiece(ChessGame.TeamColor.BLACK, backRank[col-1]));
    }
}

        public ChessBoard createScratchBoard() {
        ChessBoard scratch = new ChessBoard();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece p = this.getPiece(new ChessPosition(row, col));
                if (p != null) {
                    scratch.addPiece(
                        new ChessPosition(row, col),
                        new ChessPiece(p.getTeamColor(), p.getPieceType())
                    );
                }
            }
        }
        return scratch;
    }

    ChessPiece.PieceType[] backRank = {
        ChessPiece.PieceType.ROOK,
        ChessPiece.PieceType.KNIGHT,
        ChessPiece.PieceType.BISHOP,
        ChessPiece.PieceType.QUEEN,
        ChessPiece.PieceType.KING,
        ChessPiece.PieceType.BISHOP,
        ChessPiece.PieceType.KNIGHT,
        ChessPiece.PieceType.ROOK
    };
}
