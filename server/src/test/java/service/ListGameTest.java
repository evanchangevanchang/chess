package service;

import dataaccess.DataAccessException;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

public class ListGameTest extends Service {
    @Test
    public void listGameSuccess() throws DataAccessException  {
        this.clear();
        var gameService = new GameService();
        int gameID = gameService.createGame("meow");

        Assertions.assertNotNull(gameService.listGames());

        var gameData = gameDAO.getGame(gameID);

        Assertions.assertEquals( "meow", gameData.gameName());
    }

    @Test
    public void listGamesNone() throws DataAccessException {
        this.clear();
        var gameService = new GameService();

        Collection<GameData> emptyList= new ArrayList<>();

        Assertions.assertEquals(emptyList, gameService.listGames());
    }
}
