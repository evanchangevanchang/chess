package service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ListGameTest extends Service {
    @Test
    public void listGameSuccess() {
        var gameService = new GameService();
        int gameID = gameService.createGame("meow");

        Assertions.assertNotNull(gameService.listGames());

        var gameData = gameDAO.getGame(gameID);

        Assertions.assertEquals( "meow", gameData.gameName());
    }
}
