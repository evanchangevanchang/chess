package service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClearTest {

    @Test
    public void clearTest() {
        GameService gameService = new GameService();
        gameService.createGame("hello everyone");
        gameService.clear();
        Assertions.assertNull(gameService.getGame(1));
    }
}
