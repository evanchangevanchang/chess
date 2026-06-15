package client;

import chess.*;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;

import static chess.ChessGame.TeamColor.*;
import static ui.EscapeSequences.*;

public class ChessClient {
    private static final int BOARD_LENGTH = 8;
    private boolean direction;

    public ChessClient() {
        this.direction = true;
    }

    public void displayGame(ChessGame.TeamColor playerColor, ChessGame chessGame, ChessPosition highlightPos) {
        var out = new PrintStream(System.out);
        direction = playerColor == WHITE;
        ChessBoard board = chessGame.getBoard();
        Collection<ChessPosition> highlightSpots = new HashSet<>();
        if (highlightPos != null) {
            var moves = chessGame.validMoves(highlightPos);
            for (ChessMove move : moves) {
                var pos = move.getEndPosition();
                highlightSpots.add(new ChessPosition(pos.getRow(), pos.getColumn()));
            }
        }
        if (direction) {
            drawWhiteBoard(out, board, highlightSpots);
        } else {
            drawBlackBoard(out, board, highlightSpots);
        }
    }

    private void drawWhiteBoard(PrintStream out, ChessBoard chessBoard, Collection<ChessPosition> highlightSpots) {
        drawEdge(out);
        for (int row = BOARD_LENGTH; row > 0; row--) {
            out.print("\n");
            out.print(SET_BG_COLOR_BLACK);
            out.print(" " + row + " ");
            for (int col = 1; col <= BOARD_LENGTH; col++) {
                setBGColor(out, chessBoard, highlightSpots, row, col);
            }
            out.print(SET_BG_COLOR_BLACK);
            out.print(" " + row + " ");
            out.print(RESET_BG_COLOR);
        }
        drawEdge(out);
    }
    private void drawBlackBoard(PrintStream out, ChessBoard chessBoard, Collection<ChessPosition> highlightSpots) {
        drawEdge(out);
        for (int row = 1; row <= BOARD_LENGTH; row++) {
            out.print("\n");
            out.print(SET_BG_COLOR_BLACK);
            out.print(" " + row + " ");
            for (int col = BOARD_LENGTH; col > 0; col--) {
                setBGColor(out, chessBoard, highlightSpots, row, col);
            }
            out.print(SET_BG_COLOR_BLACK);
            out.print(" " + row + " ");
            out.print(RESET_BG_COLOR);
        }
        drawEdge(out);
    }

    private void setBGColor(PrintStream out, ChessBoard chessBoard, Collection<ChessPosition> highlightSpots, int row, int col) {
        if ((row + col) % 2 == 0) {
            if (highlightSpots.contains(new ChessPosition(row, col))) {
                out.print(SET_BG_COLOR_DARK_GREEN);
            } else {
                out.print(SET_BG_COLOR_DARK_GREY);
            }
        } else {
            if (highlightSpots.contains(new ChessPosition(row, col))) {
                out.print(SET_BG_COLOR_GREEN);
            } else {
                out.print(SET_BG_COLOR_LIGHT_GREY);
            }
        }
        drawPiece(out, chessBoard, row, col);
    }

    private void drawEdge(PrintStream out) {
        out.print("\n");
        out.print(SET_BG_COLOR_BLACK);
        for (int i = 0; i < edges.length; i++) {
            int j = (direction) ? i : (edges.length - 1) - i;
            out.print(edges[j]);
        }
        out.print(RESET_BG_COLOR);
    }

    private final String[] edges = {
            EMPTY, " a ", " b ", "  c ", " d ", "  e ", "  f "," g ","  h ", "   "
    };

    private void drawPiece(PrintStream out, ChessBoard chessBoard, int row, int col) {
        ChessPiece currentPiece = chessBoard.getPiece(new ChessPosition(row, col));
        if (currentPiece == null) {
            out.print(EMPTY);
        } else {
            switch (currentPiece.getPieceType()) {
                case PAWN:
                    if (currentPiece.getTeamColor() == WHITE) {
                        out.print(WHITE_PAWN);
                    } else {
                        out.print(BLACK_PAWN);
                    }
                    break;
                case KNIGHT:
                    if (currentPiece.getTeamColor() == WHITE) {
                        out.print(WHITE_KNIGHT);
                    } else {
                        out.print(BLACK_KNIGHT);
                    }
                    break;
                case ROOK:
                    if (currentPiece.getTeamColor() == WHITE) {
                        out.print(WHITE_ROOK);
                    } else {
                        out.print(BLACK_ROOK);
                    }
                    break;
                case BISHOP:
                    if (currentPiece.getTeamColor() == WHITE) {
                        out.print(WHITE_BISHOP);
                    } else {
                        out.print(BLACK_BISHOP);
                    }
                    break;
                case QUEEN:
                    if (currentPiece.getTeamColor() == WHITE) {
                        out.print(WHITE_QUEEN);
                    } else {
                        out.print(BLACK_QUEEN);
                    }
                    break;
                case KING:
                    if (currentPiece.getTeamColor() == WHITE) {
                        out.print(WHITE_KING);
                    } else {
                        out.print(BLACK_KING);
                    }
                    break;
                default:
                    out.print(EMPTY);
            }
        }
    }
}



