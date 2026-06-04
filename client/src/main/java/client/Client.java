package client;

import chess.ChessGame;
import model.GameData;
import request.*;
import result.ListGameResult;
import result.LoginResult;
import result.RegisterResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Client  {
    private final ServerFacade server;
    private String authToken;
    private boolean inGame;
    private ChessGame.TeamColor boardColor;
    private final ChessClient chessClient; // final?
    private ChessGame game; // figure out how to update game
    private int currentGameID; // save gameID to display chosen game

    public Client(String serverURL) {
        server = new ServerFacade(serverURL);
        authToken = null;
        inGame = false;
        chessClient = new ChessClient();
        game = new ChessGame(); // just for set up
        currentGameID = 1;
    }
    public void run() {
        // prompt here
        System.out.print("CHESS!!!!!!\n" + help());
        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            if (inGame) {
                chessClient.displayGame(boardColor, game); // if game joined, display game
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
                    case "logout", "l" -> logout();
                    case "quit", "q" -> "quit";
                    case "create", "c" -> createGame(params);
                    case "list", "i" -> listGame();
                    case "join", "j" -> joinGame(params);
                    case "observe", "o" -> observeGame(params);
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
            try {
            int gameID = Integer.parseInt(params[1]);
            ChessGame.TeamColor playerColor = (params[0].equals("white")) ? ChessGame.TeamColor.WHITE :
                    (params[0].equals("black")) ? ChessGame.TeamColor.BLACK : null;
            if (playerColor == null) {
                return help();
            }
            server.joinGame(new JoinGameRequest(playerColor, gameID), authToken);
            inGame = true;
            boardColor = playerColor;
            currentGameID = gameID;
            return "joined game successfully!";
            }
            catch (NumberFormatException e) {
                return "not a number";
            }
        }
        return help();
    }
    private String observeGame(String... params) {
        if (params.length == 1) {
            try {
                int gameID = Integer.parseInt(params[0]);
                ListGameResult listGameResult = server.listGames(authToken);
                ArrayList<GameData> games = listGameResult.games();
                var selectedGame = games.get(gameID - 1);
                if (selectedGame == null) {
                    return "no gameData";
                }
                inGame = true;
                boardColor = ChessGame.TeamColor.WHITE;
                currentGameID = gameID;
                return "observing game " + gameID;
            } catch (NumberFormatException e) {
                return "not a number";
            } catch (IndexOutOfBoundsException e) {
                return "game not found";
            }
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
                    "help"
                    "quit" or "q"
                    """;
        }
        return """
                    "logout" or "o"
                    "list" or "i"
                    "create" or "c" <game name>
                    "join" or "j" <player color (white/black)> <game number>
                    "observe" or "o"
                    "clear" or "d"
                    "help"
                    "quit" or "q"
                    """;
    }


}
