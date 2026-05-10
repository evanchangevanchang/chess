package chess;

import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor current_turn;
    private ChessBoard board;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.current_turn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return current_turn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.current_turn = team;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return current_turn == chessGame.current_turn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(current_turn, board);
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        if (board.getPiece(startPosition) == null) return null;
        Collection<ChessMove> moves = board.getPiece(startPosition).pieceMoves(board, startPosition);

        return moves;

    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece.PieceType promotionPiece = move.getPromotionPiece();
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());

        if (board.getPiece(startPosition) == null ||
                !(validMoves.contains(move)) ||
                board.getPiece(startPosition).getTeamColor() != current_turn) {
            throw new InvalidMoveException();
        }

        // copy piece from start pos to end pos

        // change piece type if necessary
        if (promotionPiece != null) {
            board.addPiece(endPosition, new ChessPiece(current_turn, promotionPiece));
        } else board.addPiece(endPosition, board.getPiece(startPosition));

        // remove old piece
        board.addPiece(startPosition, null);
        current_turn = (current_turn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // get king position
        ChessPosition king_position = null;
        for (int row=1;row<9;row++) {
            for(int col=1;col<9;col++) {
                ChessPosition current_position = new ChessPosition(row, col);
                ChessPiece current_piece = board.getPiece(current_position);
                if (current_piece != null
                        && current_piece.getTeamColor() == teamColor
                        && current_piece.getPieceType() == ChessPiece.PieceType.KING) {
                    king_position = current_position;
                    break;
                }
            }
        }
        // make sure king position is not in valid positions for opposing color
        for (int row=1;row<9;row++) {
            for(int col=1;col<9;col++) {
                ChessPosition current_position = new ChessPosition(row, col);
                ChessPiece current_piece = board.getPiece(current_position);
                if (current_piece != null && current_piece.getTeamColor() != teamColor) {
                    return kingCheck(current_piece, current_position, king_position);
                }
            }
        }
        return false;
    }

    // check if a piece threatens a king
    private boolean kingCheck(ChessPiece piece, ChessPosition piece_position, ChessPosition king_position) {
        if (king_position == null) return false;
        Collection<ChessMove> potentialMoves = piece.pieceMoves(board, piece_position);
        for (ChessMove move : potentialMoves) {
            if (move.getEndPosition().equals(king_position)) return true;
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
     * Sets this game's chessboard to a given board
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
        return this.board;
    }
}
