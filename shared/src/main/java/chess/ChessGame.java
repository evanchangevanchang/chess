package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor currentTurn;
    private ChessBoard board;

    // castling cannot happen if any of the pieces involved have moved
    private boolean whiteKingMoved;
    private boolean blackKingMoved;

    private boolean whiteLeftRookMoved;
    private boolean whiteRightRookMoved;

    private boolean blackLeftRookMoved;
    private boolean blackRightRookMoved;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.currentTurn = TeamColor.WHITE;
        // all start false
        this.whiteKingMoved = this.blackKingMoved =
                this.whiteLeftRookMoved = this.whiteRightRookMoved =
                        this.blackLeftRookMoved = this.blackRightRookMoved = false;

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTurn = team;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return currentTurn == chessGame.currentTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTurn, board);
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
        Collection<ChessMove> validMoves = new ArrayList<>();
        for (ChessMove move : moves) {
            ChessBoard clone = new ChessBoard(board);
            ChessBoard save = board;
            board = clone;
            board.addPiece(move.getEndPosition(), board.getPiece(startPosition));

            ChessGame.TeamColor movingColor = board.getPiece(startPosition).getTeamColor();

            board.addPiece(move.getStartPosition(), null);

            if (!isInCheck(movingColor)) {
                validMoves.add(move);
            }

            board = save;
        }
        return validMoves;

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
                board.getPiece(startPosition).getTeamColor() != currentTurn) {
            throw new InvalidMoveException();
        }
        int startR = startPosition.getRow();
        int startC = startPosition.getColumn();

        if (startR == 1 && startC == 1) {whiteLeftRookMoved = false;}
        if (startR == 1 && startC == 8) {whiteRightRookMoved = false;}
        if (startR == 1 && startC == 5) {whiteKingMoved = false;}
        if (startR == 8 && startC == 1) {blackLeftRookMoved = false;}
        if (startR == 8 && startC == 8) {blackRightRookMoved = false;}
        if (startR == 8 && startC == 5) {blackKingMoved = false;}

        // change piece type if necessary
        if (promotionPiece != null) {
            board.addPiece(endPosition, new ChessPiece(currentTurn, promotionPiece));
        } else board.addPiece(endPosition, board.getPiece(startPosition));

        // remove old piece
        board.addPiece(startPosition, null);
        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // get king position
        ChessPosition kingPosition = null;
        for (int row=1;row<9;row++) {
            for(int col=1;col<9;col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = board.getPiece(currentPosition);
                if (currentPiece != null
                        && currentPiece.getTeamColor() == teamColor
                        && currentPiece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPosition = currentPosition;
                    break;
                }
            }
        }
        // make sure king position is not in valid positions for opposing color
        for (int row=1;row<9;row++) {
            for(int col=1;col<9;col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = board.getPiece(currentPosition);
                if (currentPiece != null && currentPiece.getTeamColor() != teamColor) {
                    if (kingCheck(currentPiece, currentPosition, kingPosition)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // check if a piece threatens a king
    private boolean kingCheck(ChessPiece piece, ChessPosition piecePosition, ChessPosition kingPosition) {
        if (kingPosition == null) return false;
        Collection<ChessMove> potentialMoves = piece.pieceMoves(board, piecePosition);
        for (ChessMove move : potentialMoves) {
            if (move.getEndPosition().equals(kingPosition)) return true;
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

            if (isInCheck(teamColor)) {
                for (int i = 1; i < 9; i++) {
                    for (int j = 1; j<9;j++) {
                        // check all teamColor pieces for moves that get them out of check, if none, checkmate
                        ChessPosition currentPos = new ChessPosition(i, j);
                        ChessPiece currentPiece = board.getPiece(currentPos);
                        if (currentPiece != null && currentPiece.getTeamColor() == teamColor) {
                            Collection<ChessMove> moves = validMoves(currentPos);
                            if (moves == null) continue;

                            for (ChessMove move : moves) {

                                ChessBoard clone = new ChessBoard(board);
                                ChessBoard save = board;
                                board = clone;
                                board.addPiece(move.getEndPosition(), board.getPiece(move.getStartPosition()));

                                board.addPiece(move.getStartPosition(), null);

                                if (!isInCheck(teamColor)) {
                                    board = save;
                                    return false;
                                }

                                board = save;
                            }

                        }
                    }
                }
                return true;
            }

        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            for (int i = 1; i < 9; i++) {
                for (int j = 1; j<9;j++) {
                    // check all teamColor pieces for moves that get them out of check, if none, checkmate
                    ChessPosition currentPos = new ChessPosition(i, j);
                    ChessPiece currentPiece = board.getPiece(currentPos);
                    if (currentPiece != null && currentPiece.getTeamColor() == teamColor) {
                        Collection<ChessMove> moves = validMoves(currentPos);
                        if (moves == null) continue;

                        for (ChessMove move : moves) {

                            ChessBoard clone = new ChessBoard(board);
                            ChessBoard save = board;
                            board = clone;
                            board.addPiece(move.getEndPosition(), board.getPiece(move.getStartPosition()));

                            board.addPiece(move.getStartPosition(), null);

                            if (!isInCheck(teamColor)) {
                                board = save;
                                return false;
                            }

                            board = save;
                        }

                    }
                }
            }
            return true;
        }

        return false;
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
