package client;

import chess.*;

public class ClientMain {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        try {
           new Client("http://localhost:8080").run();

        } catch (Throwable ex) {
            System.out.printf("Unable to start server: %s", ex.getMessage());
        }
    }
}
