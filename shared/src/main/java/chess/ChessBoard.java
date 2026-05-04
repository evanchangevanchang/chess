package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    ChessPiece[][] squares = new ChessPiece[8][8];
    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        int i = 0;
        for (ChessPiece piece : PIECES) {
            this.addPiece(new ChessPosition(POSITIONS[i][0], POSITIONS[i][1]), piece);
            i++;
        }

    }
    private static final ChessPiece[] PIECES = {
            new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK),
            new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT),
            new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP),
            new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN),
            new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING),
            new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP),
            new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT),
            new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK),

            new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN),
            new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN),
            new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN),
            new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN),
            new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN),
            new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN),
            new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN),
            new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN),

            new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN),
            new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN),
            new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN),
            new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN),
            new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN),
            new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN),
            new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN),
            new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN),

            new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK),
            new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT),
            new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP),
            new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN),
            new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING),
            new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP),
            new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT),
            new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK)
    };
    private static final int[][] POSITIONS = {
            {1,1}, {1,2}, {1,3}, {1,4}, {1,5}, {1,6}, {1,7}, {1,8},
            {2,1}, {2,2}, {2,3}, {2,4}, {2,5}, {2,6}, {2,7}, {2,8},
            {7,1}, {7,2}, {7,3}, {7,4}, {7,5}, {7,6}, {7,7}, {7,8},
            {8,1}, {8,2}, {8,3}, {8,4}, {8,5}, {8,6}, {8,7}, {8,8}
    };

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }
}
