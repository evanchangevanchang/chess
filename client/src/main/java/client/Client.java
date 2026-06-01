package client;

import request.RegisterRequest;
import result.RegisterResult;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Client { // implements NotificationHandler
    private final ServerFacade server;
    private boolean LOGGEDIN;
    public Client(String serverURL) {
        server = new ServerFacade(serverURL);
        LOGGEDIN = false;
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
                System.out.print(e.getMessage());
            }
        }
    }
    private void printPrompt() {
        System.out.print("\n" + RESET_BG_COLOR + ">>> " ); // color?
    }
    public String eval(String line) {
        try {
            String[] tokens = line.toLowerCase().split(" ");
            String command = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (command) {
                case "register", "r" -> register(params);
                case "quit", "q" -> "quit";
                default -> help();
            };

        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    private String register(String... params) {
        if (params.length == 3) {
            LOGGEDIN = true;
            RegisterResult result = server.register(new RegisterRequest(params[0], params[1], params[2]));
            return String.format("""
                    success!
                    username: %s
                    """, result.username());
        }
        return help();
    }
    private String help() {
        if (!LOGGEDIN) {
            return """
                    "register" or "r" <username> <password> <email>
                    "login" or "l" <username> <password>
                    "quit" or "q"
                    """;
        }
        return """
                    "logout" or "o"
                    "list" or "i"
                    "create" or "c" <game name>
                    "join" or "j" <player color> <gameID>
                    "clear" or "d"
                    "quit" or "q"
                    """;
    }


}
