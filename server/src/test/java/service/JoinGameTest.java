package service;

import chess.ChessGame;
import dataaccess.AlreadyTakenException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import request.JoinGameRequest;
import request.RegisterRequest;

public class JoinGameTest extends Service {
    @Test
    public void joinGameSuccess() {
        this.clear();
        var gameService = new GameService();
        gameService.createGame("meow");

        RegisterRequest registerRequest = new RegisterRequest(
                "PatrickGibbons69", "password", "email");
        UserService userService = new UserService();
        var result = userService.register(registerRequest);

        Assertions.assertNotNull(gameService.listGames());

        // verify no errors
        var joinGameRequest = new JoinGameRequest(ChessGame.TeamColor.WHITE, 1 );
        Assertions.assertDoesNotThrow(() -> gameService.joinGame(joinGameRequest, result.authToken()));

        //verify correct username
        Assertions.assertEquals("PatrickGibbons69", gameDAO.getGame(1).whiteUsername());
    }

    @Test
    public void joinGameFail() {
        this.clear();
        var gameService = new GameService();
        gameService.createGame("meow");

        RegisterRequest registerRequest = new RegisterRequest(
                "PatrickGibbons69", "password", "email");
        UserService userService = new UserService();
        var result = userService.register(registerRequest);

        Assertions.assertNotNull(gameService.listGames());

        // verify no errors
        var joinGameRequest = new JoinGameRequest(ChessGame.TeamColor.WHITE, 1 );
        Assertions.assertDoesNotThrow(() -> gameService.joinGame(joinGameRequest, result.authToken()));
        // attempt to join again
        Assertions.assertThrows(AlreadyTakenException.class, () -> gameService.joinGame(joinGameRequest, result.authToken()));

    }
}
