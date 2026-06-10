package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;
import model.GameData;
import request.*;
import result.ListGameResult;
import result.LoginResult;
import result.RegisterResult;
import websocket.messages.ServerMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Client implements NotificationHandler {
    private final ServerFacade server;
    private String authToken;
    private boolean inGame; // display board if join or observe
    private ChessGame.TeamColor boardColor;
    private final ChessClient chessClient; // final?

    private ChessGame game; // starts as default board, updates every notification
    private int currentGameID; // save gameID to display chosen game

    private final WebSocketFacade ws;
    private ChessPosition highlightPos;

    public Client(String serverURL) {
        server = new ServerFacade("http://" + serverURL);
        authToken = null;
        inGame = false;
        chessClient = new ChessClient();
        game = new ChessGame(); // just for set up
        currentGameID = 1;
        ws = new WebSocketFacade("ws://" + serverURL, this);
        highlightPos = null;

    }
    public void run() {
        // prompt here
        System.out.print("CHESS!!!!!!\n" + help());
        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            if (inGame) {
                chessClient.displayGame(boardColor, game, highlightPos); // if game joined, display game
                highlightPos = null; // reset highlight piece after displaying once
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

            } else if (!inGame) { // logged in
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
            } else { // in game
                return switch (command) {
                    case "leave", "l" -> {
                        inGame = false;
                        yield ws.leave(authToken, currentGameID);
                    }
                    case "quit", "q" -> {
                        inGame = false;
                        yield "quit";
                    }
                    case "move", "m" -> {
                        ChessMove move = parseMove(params); // websocket handler checks for validity
                        yield (move != null) ?
                                ws.makeMove(authToken, currentGameID, move) : help();
                    }
                    case "clear", "d" -> clear();
                    case "highlight", "h" -> highlight(params);
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

            ws.connect(authToken, gameID);

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

    private String highlight(String... params) {
        if (params.length == 1) {
            String[] stringCoords = params[0].split("");
            int col = stringCoords[0].toCharArray()[0] - 'a' + 1;
            int row = stringCoords[1].toCharArray()[0] - '0';
            if (col < 1 || col > 8 ||
                    row < 1 || row > 8) {
                return "invalid coordinate";
            }
            var selectedPos = new ChessPosition(row, col);
            var selectedPiece = game.getBoard().getPiece(selectedPos);
            if (selectedPiece == null) {
                return "no piece selected";
            } else {
                highlightPos = selectedPos;
                return "selected: " + params[0];
            }
        }
        return "test string";
    }

    private ChessMove parseMove(String... params) {
        if (params.length == 2 || params.length == 3) {
            String[] startString = params[0].split("");
            String[] endString = params[1].split("");
            if (startString.length != 2 || endString.length != 2) {
                return null;
            }
            int startCol = startString[0].toCharArray()[0] - 'a' + 1;
            int startRow = startString[1].toCharArray()[0] - '0';
            int endCol = startString[0].toCharArray()[0] - 'a' + 1;
            int endRow = startString[1].toCharArray()[0] - '0';
            if (startCol < 1 || startCol > 8 ||
                    startRow < 1 || startRow > 8 ||
                    endCol < 1 || endCol > 8 ||
                    endRow < 1 || endRow > 8) {
                return null;
            }
            if (params.length == 3) { // allow 3 parameters if piece is a pawn, still gets validated later
                if (game.getBoard().getPiece(new ChessPosition(startRow, startCol)).getPieceType()
                        .equals(ChessPiece.PieceType.PAWN)) {
                    return switch (params[2]) {
                        case "queen", "q" -> new ChessMove(new ChessPosition(startRow, startCol),
                                new ChessPosition(endRow, endCol), ChessPiece.PieceType.QUEEN);
                        case "bishop", "b" -> new ChessMove(new ChessPosition(startRow, startCol),
                                new ChessPosition(endRow, endCol), ChessPiece.PieceType.BISHOP);
                        case "knight", "k" -> new ChessMove(new ChessPosition(startRow, startCol),
                                new ChessPosition(endRow, endCol), ChessPiece.PieceType.KNIGHT);
                        case "rook", "r" -> new ChessMove(new ChessPosition(startRow, startCol),
                                new ChessPosition(endRow, endCol), ChessPiece.PieceType.ROOK);
                        default -> null;
                    };
                }
            }
            return new ChessMove(new ChessPosition(startRow, startCol),
                    new ChessPosition(endRow, endCol), null);
        }
        return null;
    }

    private String clear() {
        server.clear();
        inGame = false;
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
        } if (!inGame) {
            return """
                    "logout" or "l"
                    "list" or "i"
                    "create" or "c" <game name>
                    "join" or "j" <player color (white/black)> <game number>
                    "observe" or "o"
                    "clear" or "d"
                    "help"
                    "quit" or "q"
                    """;
        } return """
                "leave" or "l"
                "move" or "m" <start position (e.g. e4)> <end position> <promotion piece>
                "highlight" or "h" <position of piece to highlight>
                "clear" or "d"
                "help"
                "quit" or "q"
                """;
    }


    @Override
    public void notify(ServerMessage serverMessage) {
        switch (serverMessage.getServerMessageType()) {
            case NOTIFICATION -> {
                System.out.println(SET_TEXT_COLOR_GREEN +  serverMessage.getMessage());
                printPrompt();
            }
            case ERROR -> {
                System.out.println(SET_TEXT_COLOR_RED +  serverMessage.getMessage());
                printPrompt();
            }
            case LOAD_GAME -> {
                // game is passed
                GameData loadedGameData = serverMessage.getGame();
                game = loadedGameData.game();
            }
        }


    }
}
