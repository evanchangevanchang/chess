package dataaccess;

import model.GameData;

public interface GameDAO {
    public void clearGame() throws DataAccessException;
    public int createGame(String gameName) throws DataAccessException;
    public void updateGame(int gameID, GameData gameData) throws DataAccessException;
    public GameData getGame(int gameID) throws DataAccessException;
}
