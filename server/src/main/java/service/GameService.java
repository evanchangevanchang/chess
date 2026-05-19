package service;

import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;

public class GameService extends Service{
    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        Collection<GameData> games = new ArrayList<>();
        int gameID = 1;
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("authToken not found");
        }
        while (gameDAO.getGame(gameID) != null) {
            games.add(gameDAO.getGame(gameID));
            gameID++;
        }
        return games;
    }
    public GameData getGame(int gameID) {
        return gameDAO.getGame(gameID);
    }
    public int createGame(String gameName) {
        if (gameName == null) {
            throw new BadRequestException("game name cannot be empty");
        }
        return gameDAO.createGame(gameName);
    }
}
