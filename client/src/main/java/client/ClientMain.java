package client;

import chess.*;

public class ClientMain {
    public static void main(String[] args) {
        try {
           new Client("localhost:8080").run();

        } catch (Throwable ex) {
            System.out.printf("Unable to start server: %s", ex.getMessage());
        }
    }
}
