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

    private ChessGame.TeamColor pieceColor;
    private ChessPiece.PieceType type;
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
     * set piece type
     */
    public void setPieceType(ChessPiece.PieceType newType) {
        type = newType;
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
        return switch (piece.getPieceType()) {
            case BISHOP -> lineMoves(board, myPosition, BISHOP_DIRS);
            case ROOK -> lineMoves(board, myPosition, ROOK_DIRS);
            case QUEEN -> lineMoves(board, myPosition, QUEEN_DIRS);
            case KING -> kingMoves(board, myPosition);
            case KNIGHT -> knightMoves(board, myPosition);
            case PAWN -> pawnMoves(board, myPosition);
        };
    }

    /* finds possible moves in a straight line, for bishop, rook, and queen (combines both)
     */
    private List<ChessMove> lineMoves (ChessBoard board, ChessPosition myPosition, int[][] directions) {
        List<ChessMove> moves = new ArrayList<>();
        int startR = myPosition.getRow();
        int startC = myPosition.getColumn();

        for (int[] direction : directions) {
            int currentR = startR + direction[0];
            int currentC = startC + direction[1];

            while ((currentR >= 1 && currentR <= 8) && (currentC >= 1 && currentC <= 8)) {
                ChessPosition pos = new ChessPosition(currentR, currentC);
                ChessPiece checkSquare = board.getPiece(pos);

                if (checkSquare == null) {
                    moves.add(new ChessMove(myPosition, pos, null));
                    // add this position to valid
                    // move to next
                } else {
                    // landed on a piece
                    if (checkSquare.pieceColor != this.pieceColor) { // check if this is correct
                        moves.add(new ChessMove(myPosition, pos, null));
                    }
                    // check color of the piece,
                    // if opposite, spot is valid, stop
                    // if same, spot is not valid, stop (breaks either way
                    break;
                }
                currentR += direction[0];
                currentC += direction[1];
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
        return getChessMoves(board, myPosition, KNIGHT_DIRS);
    }

    private static final int[][] KNIGHT_DIRS = {
            {1,2}, {1,-2}, {-1,2}, {-1,-2},
            {2,1}, {2,-1}, {-2,1}, {-2,-1}
    };

    private List<ChessMove> pawnMoves (ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        int startR = myPosition.getRow();
        int startC = myPosition.getColumn();

        // ternary, positive direction if white
        int directionSign = (pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;

        // forward
        for (int[] direction : PAWN_F_DIRS) {
            // im so proud of this line
            if ((direction[0] == 2) && (startR != (4.5 - 2.5 * directionSign))) {continue;}
            int currentR = startR + directionSign * direction[0];
            // no current_c needed
            if ((currentR >= 1 && currentR <= 8)) {
                ChessPosition pos = new ChessPosition(currentR, startC);
                ChessPiece checkSquare = board.getPiece(pos);

                // handle promotion
                if (checkSquare == null) {
                    // break if moving 2 but blocked
                    if ((direction[0] == 2) && (board.getPiece(new ChessPosition(currentR - directionSign, startC)) != null)) {break;}
                    pawnMovement(myPosition, moves, currentR, pos);
                }

            }
        }
        // diagonal attacks
        for (int[] direction : PAWN_D_DIRS) {
            int currentR = startR + directionSign;
            int currentC = startC + direction[1];

            if ((currentR >= 1 && currentR <= 8) && (currentC >= 1 && currentC <= 8)) {
                ChessPosition pos = new ChessPosition(currentR, currentC);
                ChessPiece checkSquare = board.getPiece(pos);

                if (checkSquare != null) {
                    // landed on a piece
                    if (checkSquare.pieceColor != this.pieceColor) {
                        pawnMovement(myPosition, moves, currentR, pos);
                    }
                }
            }
        }


        return moves;
    }

    private void pawnMovement(ChessPosition myPosition, List<ChessMove> moves, int currentR, ChessPosition pos) {
        if (((pieceColor == ChessGame.TeamColor.WHITE) && (currentR == 8)) ||
                ((pieceColor == ChessGame.TeamColor.BLACK) && (currentR == 1))) {
            moves.add(new ChessMove(myPosition, pos, PieceType.BISHOP));
            moves.add(new ChessMove(myPosition, pos, PieceType.ROOK));
            moves.add(new ChessMove(myPosition, pos, PieceType.QUEEN));
            moves.add(new ChessMove(myPosition, pos, PieceType.KNIGHT));
        } else {

            moves.add(new ChessMove(myPosition, pos, null));
        }
    }

    private static final int[][] PAWN_F_DIRS = {
            {1,0}, {2,0}
    };
    // little redundant but i kept it this way bc im ocd
    private static final int[][] PAWN_D_DIRS = {
            {1,1}, {1,-1}
    };

    private List<ChessMove> kingMoves (ChessBoard board, ChessPosition myPosition) {
        return getChessMoves(board, myPosition, KING_DIRS);
    }

    private List<ChessMove> getChessMoves(ChessBoard board, ChessPosition myPosition, int[][] kingDirs) {
        List<ChessMove> moves = new ArrayList<>();
        int startR = myPosition.getRow();
        int startC = myPosition.getColumn();

        for (int[] direction : kingDirs) {
            int currentR = startR + direction[0];
            int currentC = startC + direction[1];

            if ((currentR >= 1 && currentR <= 8) && (currentC >= 1 && currentC <= 8)) {
                ChessPosition pos = new ChessPosition(currentR, currentC);
                ChessPiece checkSquare = board.getPiece(pos);

                if (checkSquare == null) {
                    moves.add(new ChessMove(myPosition, pos, null));
                    // add this position to valid
                } else {
                    // landed on a piece
                    if (checkSquare.pieceColor != this.pieceColor) {
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

