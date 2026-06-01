package client;

import chess.ChessGame;
import model.GameData;
import request.*;
import result.ListGameResult;
import result.LoginResult;
import result.RegisterResult;

import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Client extends ChessClient {
    private final ServerFacade server;
    private String authToken;
    private boolean inGame;
    public Client(String serverURL) {
        server = new ServerFacade(serverURL);
        authToken = null;
        inGame = false;
    }
    public void run() {
        // prompt here
        System.out.print("CHESS!!!!!!\n" + help());
        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            if (inGame) {
                displayGame();
            }
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
            if (authToken == null) {
            return switch (command) { //not logged in
                case "register", "r" -> register(params);
                case "login", "l" -> login(params);
                case "quit", "q" -> "quit";
                case "clear", "d" -> clear();
                default -> help();
            };

            } else { // logged in
                return switch (command) {
                    case "logout", "o" -> logout();
                    case "quit", "q" -> "quit";
                    case "create", "c" -> createGame(params);
                    case "list", "i" -> listGame();
                    case "join", "j" -> joinGame(params);
                    case "clear", "d" -> clear();
                    default -> help();
                };
            }

        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    private String register(String... params) {
        if (params.length == 3) {
            RegisterResult result = server.register(new RegisterRequest(params[0], params[1], params[2]));
            authToken = result.authToken();
            return String.format("""
                    success!
                    username: %s
                    """, result.username());
        }
        return help();
    }
    private String login(String... params) {
        if (params.length == 2) {
            LoginResult result = server.login(new LoginRequest(params[0], params[1]));
            authToken = result.authToken();
            return String.format("""
                    success!
                    username: %s
                    """, result.username());
        }
        return help();
    }
    private String logout() {
        if (authToken == null) {
            throw new ResponseException("no authToken found");
        }
        server.logout(new LogoutRequest(authToken));
        authToken = null;
        return "logout success!";
    }
    private String createGame(String... params) {
        if (params.length == 1) {
        server.createGame(new CreateGameRequest(params[0]), authToken); // change to not void to access gameID
        return "success!";
        }
        return help();
    }
    private String listGame() {
        ListGameResult listGameResult = server.listGames(authToken);
        Collection<GameData> games = listGameResult.games();
        if (games.isEmpty()) {
            return "no games found!";
        }
        String output = "";
        int gameNo = 1;
        for (GameData game : games) {
            output += "number: " + gameNo +
                "\ngame name: " + game.gameName() +
                "\nwhite username: " + game.whiteUsername() +
                "\nblack username: " + game.blackUsername() +
                    "\n";
            gameNo++;
        }
        return output;
    }
    private String joinGame(String... params) {
        if (params.length == 2) {
            int gameID = Integer.parseInt(params[1]);
            ChessGame.TeamColor player_color = (params[0].equals("white")) ? ChessGame.TeamColor.WHITE :
                    (params[0].equals("black")) ? ChessGame.TeamColor.BLACK : null;
            if (player_color == null) {
                return help();
            }
            server.joinGame(new JoinGameRequest(player_color, gameID), authToken);
            inGame = true;
            return "joined game successfully!";
        }
        return help();
    }

    private String clear() {
        server.clear();
        return "cleared";
    }
    private String help() {
        if (authToken == null) {
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
                    "join" or "j" <player color (white/black)> <game number>
                    "clear" or "d"
                    "quit" or "q"
                    """;
    }


}
