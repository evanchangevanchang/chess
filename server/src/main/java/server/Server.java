package server;

import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.DatabaseAccessException;
import io.javalin.*;
import io.javalin.http.Context;
import model.GameData;
import request.*;
import result.LoginResult;
import result.RegisterResult;
import service.AuthService;
import service.GameService;
import service.Service;
import service.UserService;
import websocket.WebSocketHandler;

import java.util.Collection;
import java.util.Map;

public class Server {

    private final Javalin javalin;

    private final Gson serializer = new Gson();
    private final WebSocketHandler webSocketHandler;

    public Server() {
        webSocketHandler = new WebSocketHandler();
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

        javalin.exception(DataAccessException.class, this::dataAccessExceptionHandler);
        javalin.exception(AlreadyTakenException.class, this::alreadyTakenExceptionHandler);
        javalin.exception(BadRequestException.class, this::badRequestExceptionHandler);
        javalin.exception(DatabaseAccessException.class, this::databaseAccessExceptionHandler);
        javalin.post("/user", this::registerHandler);
        javalin.post("/session", this::loginHandler);
        javalin.delete("/session", this::logoutHandler);
        javalin.delete("/db", this::clearHandler);
        javalin.post("/game", this::createGameHandler);
        javalin.put("/game", this::joinGameHandler);
        javalin.get("/game", this::listGameHandler);
        javalin.ws("/ws", ws -> {
            ws.onConnect(webSocketHandler);
            ws.onMessage(webSocketHandler);
            ws.onClose(webSocketHandler);
        });
    }

    private void dataAccessExceptionHandler(DataAccessException e, Context context) {
        var body = new Gson().toJson(Map.of("message", String.format("Error: %s", e.getMessage()), "success", false));
        context.status(401);
        context.json(body);
    }
    private void alreadyTakenExceptionHandler(AlreadyTakenException e, Context context) {
        var body = new Gson().toJson(Map.of("message", String.format("Error: %s", e.getMessage()), "success", false));
        context.status(403);
        context.json(body);
    }
    private void badRequestExceptionHandler(BadRequestException e, Context context) {
        var body = new Gson().toJson(Map.of("message", String.format("Error: %s", e.getMessage()), "success", false));
        context.status(400);
        context.json(body);
    }
    private void databaseAccessExceptionHandler(DatabaseAccessException e, Context context) {
        var body = new Gson().toJson(Map.of("message", String.format("Error: %s", e.getMessage()), "success", false));
        context.status(500);
        context.json(body);
    }

    private void registerHandler(Context context) throws DataAccessException {
        String userInfo = context.body();
        RegisterRequest registerRequest = serializer.fromJson(userInfo, RegisterRequest.class);
        UserService userService = new UserService();
        RegisterResult registerResult = userService.register(registerRequest);
        context.status(200);
        context.json(new Gson().toJson(registerResult));

    }

    private void loginHandler(Context context) throws DataAccessException {
        UserService userService = new UserService();
        LoginRequest loginRequest = serializer.fromJson(context.body(), LoginRequest.class);
        LoginResult loginResult = userService.login(loginRequest);
        context.status(200);
        context.json(new Gson().toJson(loginResult));
    }

    private void logoutHandler(Context context) throws DataAccessException {
        UserService userService = new UserService();
        String authToken = context.header("authorization");
        LogoutRequest logoutRequest = new LogoutRequest(authToken);
        userService.logout(logoutRequest);
        context.status(200);
        context.json(new Gson().toJson(Map.of("success", true)));
    }

    private void createGameHandler(Context context) throws DataAccessException {
        GameService gameService = new GameService();
        String authToken = context.header("authorization");
        AuthService authService = new AuthService();
        authService.validateAuth(authToken);

        CreateGameRequest createGameRequest = serializer.fromJson(context.body(), CreateGameRequest.class);
        String gameName = createGameRequest.gameName();
            int gameID = gameService.createGame(gameName);
            context.status(200);
            context.json(new Gson().toJson(Map.of("gameID", gameID)));
    }

    private void listGameHandler(Context context) throws DataAccessException {
        GameService gameService = new GameService();
        String authToken = context.header("authorization");
        AuthService authService = new AuthService();
        authService.validateAuth(authToken);

        Collection<GameData> games = gameService.listGames();
        context.status(200);
        context.json(new Gson().toJson(Map.of("games", games)));
    }

    private void joinGameHandler(Context context) throws DataAccessException {
        GameService gameService = new GameService();
        String authToken = context.header("authorization");
        AuthService authService = new AuthService();
        authService.validateAuth(authToken);

        JoinGameRequest joinGameRequest = serializer.fromJson(context.body(), JoinGameRequest.class);
        gameService.joinGame(joinGameRequest, authToken);
        context.status(200);
        context.json(new Gson().toJson(Map.of("success", true)));
    }

    private void clearHandler(Context context) throws DataAccessException{
        Service service = new Service();
        service.clear();
        context.status(200);
        context.json(new Gson().toJson(Map.of("success", true)));
    }


    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
