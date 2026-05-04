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
        switch(piece.getPieceType()) {
            case BISHOP:
                return lineMoves(board, myPosition, BISHOP_DIRS);
            case ROOK:
                return lineMoves(board, myPosition, ROOK_DIRS);
            case QUEEN:
                return lineMoves(board, myPosition, QUEEN_DIRS);
            case KING:
                return kingMoves(board, myPosition);
            case KNIGHT:
                return knightMoves(board, myPosition);
            case PAWN:
                return pawnMoves(board, myPosition);

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
    private static final int[][] BISHOP_DIRS = {
            {1,1}, {1,-1}, {-1,1}, {-1,-1}
    };
    private static final int[][] ROOK_DIRS = {
            {1,0}, {-1,0}, {0,1}, {0,-1}
    };
    private static final int[][] QUEEN_DIRS = {
            {1,1}, {1,-1}, {-1,1}, {-1,-1},
            {1,0}, {-1,0}, {0,1}, {0,-1}
    };

    //probably can use the same Moves code as king but keeping separate just in case
    private List<ChessMove> knightMoves (ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        int start_r = myPosition.getRow();
        int start_c = myPosition.getColumn();

        for (int[] direction : KNIGHT_DIRS) {
            int current_r = start_r + direction[0];
            int current_c = start_c + direction[1];

            if ((current_r >= 1 && current_r <= 8) && (current_c >= 1 && current_c <= 8)) {
                ChessPosition pos = new ChessPosition(current_r, current_c);
                ChessPiece check_square = board.getPiece(pos);

                if (check_square == null) {
                    moves.add(new ChessMove(myPosition, pos, null));
                    // add this position to valid
                } else {
                    // landed on a piece
                    if (check_square.pieceColor != this.pieceColor) {
                        moves.add(new ChessMove(myPosition, pos, null));
                    }
                }
            }
        }

        return moves;
    };

    private static final int[][] KNIGHT_DIRS = {
            {1,2}, {1,-2}, {-1,2}, {-1,-2},
            {2,1}, {2,-1}, {-2,1}, {-2,-1}
    };

    private List<ChessMove> pawnMoves (ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        int start_r = myPosition.getRow();
        int start_c = myPosition.getColumn();

        // ternary, positive direction if white
        int direction_sign = (pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;

        // forward
        for (int[] direction : PAWN_F_DIRS) {
            // im so proud of this line
            if ((direction[0] == 2) && (start_r != (4.5 - 2.5 * direction_sign))) continue;
            int current_r = start_r + direction_sign * direction[0];
            int current_c = start_c;

            if ((current_r >= 1 && current_r <= 8)) {
                ChessPosition pos = new ChessPosition(current_r, current_c);
                ChessPiece check_square = board.getPiece(pos);

                // handle promotion
                if (check_square == null) {
                    // break if moving 2 but blocked
                    if ((direction[0] == 2) && (board.getPiece(new ChessPosition(current_r - direction_sign, current_c)) != null)) break;
                    if (((pieceColor == ChessGame.TeamColor.WHITE) && (current_r == 8)) ||
                            ((pieceColor == ChessGame.TeamColor.BLACK) && (current_r == 1))) {
                        moves.add(new ChessMove(myPosition, pos, PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, pos, PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, pos, PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, pos, PieceType.KNIGHT));
                    } else {

                        moves.add(new ChessMove(myPosition, pos, null));
                    }
                }

            }
        }
        // diagonal attacks
        for (int[] direction : PAWN_D_DIRS) {
            int current_r = start_r + direction_sign;
            int current_c = start_c + direction[1];

            if ((current_r >= 1 && current_r <= 8) && (current_c >= 1 && current_c <= 8)) {
                ChessPosition pos = new ChessPosition(current_r, current_c);
                ChessPiece check_square = board.getPiece(pos);

                if (check_square != null) {
                    // landed on a piece
                    if (check_square.pieceColor != this.pieceColor) {
                        if (((pieceColor == ChessGame.TeamColor.WHITE) && (current_r == 8)) ||
                                ((pieceColor == ChessGame.TeamColor.BLACK) && (current_r == 1))) {
                            moves.add(new ChessMove(myPosition, pos, PieceType.BISHOP));
                            moves.add(new ChessMove(myPosition, pos, PieceType.ROOK));
                            moves.add(new ChessMove(myPosition, pos, PieceType.QUEEN));
                            moves.add(new ChessMove(myPosition, pos, PieceType.KNIGHT));
                        }
                        else moves.add(new ChessMove(myPosition, pos, null));
                    }
                }
            }
        }


        return moves;
    }
    private static final int[][] PAWN_F_DIRS = {
            {1,0}, {2,0}
    };
    // little redundant but i kept it this way bc im ocd
    private static final int[][] PAWN_D_DIRS = {
            {1,1}, {1,-1}
    };

    private List<ChessMove> kingMoves (ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        int start_r = myPosition.getRow();
        int start_c = myPosition.getColumn();

        for (int[] direction : KING_DIRS) {
            int current_r = start_r + direction[0];
            int current_c = start_c + direction[1];

            if ((current_r >= 1 && current_r <= 8) && (current_c >= 1 && current_c <= 8)) {
                ChessPosition pos = new ChessPosition(current_r, current_c);
                ChessPiece check_square = board.getPiece(pos);

                if (check_square == null) {
                    moves.add(new ChessMove(myPosition, pos, null));
                    // add this position to valid
                } else {
                    // landed on a piece
                    if (check_square.pieceColor != this.pieceColor) {
                        moves.add(new ChessMove(myPosition, pos, null));
                    }
                    // check color of the piece,
                    // if opposite, spot is valid, stop
                    // if same, spot is not valid, stop (breaks either way
                }
            }
        }

        return moves;
    }
    private static final int[][] KING_DIRS = {
            {1,1}, {1,-1}, {-1,1}, {-1,-1},
            {1,0}, {-1,0}, {0,1}, {0,-1}
    };
}

