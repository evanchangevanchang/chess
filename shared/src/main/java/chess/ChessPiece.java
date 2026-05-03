package chess;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
    this.pieceColor = pieceColor;
    this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if(piece.getPieceType() == PieceType.BISHOP) {
            // instead of returning this, add logic based on moves functions
            return lineMoves(board, myPosition, BISHOP_DIRS);
            // return List.of(new ChessMove(new ChessPosition(5,4), new ChessPosition(1, 8), null));
        }
        return List.of();
    }

    /* finds possible moves in a straight line, for bishop, rook, and queen (combines both)

     */
    private List<ChessMove> lineMoves (ChessBoard board, ChessPosition myPosition, int[][] directions) {
        List<ChessMove> moves = new ArrayList<>();
        int start_r = myPosition.getRow();
        int start_c = myPosition.getColumn();

        for (int[] direction : directions) {
            int current_r = start_r + direction[0];
            int current_c = start_c + direction[1];

            while ((current_r >= 1 && current_r <= 8) && (current_c >= 1 && current_c <= 8)) {
                ChessPosition pos = new ChessPosition(current_r, current_c);
                ChessPiece check_square = board.getPiece(pos);

                if (check_square == null) {
                    moves.add(new ChessMove(myPosition, pos, null));
                    // add this position to valid
                    // move to next
                } else {
                    // landed on a piece
                    if (check_square.pieceColor != this.pieceColor) { // check if this is correct
                        moves.add(new ChessMove(myPosition, pos, null));
                    }
                    // check color of the piece,
                    // if opposite, spot is valid, stop
                    // if same, spot is not valid, stop (breaks either way
                    break;
                }
                current_r += direction[0];
                current_c += direction[1];
            }
        }

        return moves;
    }
    // diagonal
    private static final int[][] BISHOP_DIRS = {
            {1,1}, {1,-1}, {-1,1}, {-1,-1}
    };

    // straight
    private static final int[][] ROOK_DIRS = {
            {1,0}, {-1,0}, {0,1}, {0,-1}
    };

    // both
    private static final int[][] QUEEN_DIRS = {
            {1,1}, {1,-1}, {-1,1}, {-1,-1},
            {1,0}, {-1,0}, {0,1}, {0,-1}
    };

//    private List<ChessMove> knightMoves (ChessBoard board, ChessPosition myPosition) {
//
//    };

//    private List<ChessMove> pawnMoves (ChessBoard board, ChessPosition myPosition,
}

