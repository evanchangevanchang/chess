package service;

import dataaccess.DataAccessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClearTest {

    @Test
    public void clearSuccess() throws DataAccessException {
        GameService gameService = new GameService();
        gameService.createGame("hello everyone");
        gameService.clear();
        Assertions.assertNull(gameService.getGame(1));
    }
}
