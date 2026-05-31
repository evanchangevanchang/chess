package client;

import com.sun.nio.sctp.NotificationHandler;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Client { // implements NotificationHandler
//    private final ServerFacade server;
    public Client() {

    }
    public void run() {
        // prompt here

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.print(SET_TEXT_COLOR_WHITE + result);
            } catch (Throwable e) {
                System.out.print(e.toString());
            }
        }
    }
    private void printPrompt() {
        System.out.print("\n" + RESET_BG_COLOR + ">>> " + SET_BG_COLOR_GREEN); // color?
    }
    public String eval(String line) {
        try {
            String[] tokens = line.toLowerCase().split(" ");
            String command = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (command) {
                case "register", "r" -> register(params);
                case "quit" -> "quit";
                default -> "help";
            };

        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    private String register(String... params) {
        return "placeholder";
    }


}
