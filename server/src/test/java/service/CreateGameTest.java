package service;

import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateGameTest extends Service {
    @Test
    public void createGameSuccess() throws DataAccessException {
        var gameService = new GameService();
        int gameID = gameService.createGame("test game");

        Assertions.assertNotNull(gameDAO.getGame(gameID));
    }

    @Test
    public void createGameFail() throws DataAccessException {
        var gameService = new GameService();
        Assertions.assertThrows(BadRequestException.class, () -> gameService.createGame(null));
    }

}
