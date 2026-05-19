package server;

import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import io.javalin.*;
import io.javalin.http.Context;
import request.CreateGameRequest;
import request.LoginRequest;
import request.LogoutRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.RegisterResult;
import service.AuthService;
import service.GameService;
import service.Service;
import service.UserService;

import java.util.Map;

public class Server {

    private final Javalin javalin;

    private final Gson serializer = new Gson();

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

        javalin.exception(Exception.class, this::exceptionHandler);
        javalin.post("/user", this::registerHandler);
        javalin.post("/session", this::loginHandler);
        javalin.delete("/session", this::logoutHandler);
        javalin.delete("/db", this::clearHandler);
        javalin.post("/game", this::createGameHandler);
        javalin.put("/game", this::joinGameHandler);
    }

    private void exceptionHandler(Exception e, Context context) {
        var body = new Gson().toJson(Map.of("message", String.format("Error: %s", e.getMessage()), "success", false));
        context.status(401);
        context.json(body);
    }

    private void registerHandler(Context context) {
        String userInfo = context.body();
        RegisterRequest registerRequest = serializer.fromJson(userInfo, RegisterRequest.class);
        UserService userService = new UserService();
        try {RegisterResult registerResult = userService.register(registerRequest);
            context.status(200);
            context.json(new Gson().toJson(registerResult));}
        catch(AlreadyTakenException e) {
            var body = new Gson().toJson(Map.of("message", String.format("Error: %s", e.getMessage()), "success", false));
            context.status(403);
            context.json(body);
        }
        catch(BadRequestException e) {
            var body = new Gson().toJson(Map.of("message", String.format("Error: %s", e.getMessage()), "success", false));
            context.status(400);
            context.json(body);
        }
    }

    private void loginHandler(Context context) throws DataAccessException {
        UserService userService = new UserService();
        LoginRequest loginRequest = serializer.fromJson(context.body(), LoginRequest.class);
        try {
            LoginResult loginResult = userService.login(loginRequest);
            context.status(200);
            context.json(new Gson().toJson(loginResult));
        } catch (BadRequestException e) {
            var body = new Gson().toJson(Map.of("message", String.format("Error: %s", e.getMessage()), "success", false));
            context.status(400);
            context.json(body);
        }

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
        try {
            int gameID = gameService.createGame(gameName);
            context.status(200);
            context.json(new Gson().toJson(Map.of("gameID", gameID)));
        }
        catch (BadRequestException e) {
            var body = new Gson().toJson(Map.of("message", String.format("Error: %s", e.getMessage()), "success", false));
            context.status(400);
            context.json(body);
        }
    }

    private void joinGameHandler(Context context) throws DataAccessException {

    }

    private void clearHandler(Context context) {
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
