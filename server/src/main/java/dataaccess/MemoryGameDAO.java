package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.HashMap;

public class MemoryGameDAO implements GameDAO{
    private final static HashMap<Integer, GameData> GAME_MAP = new HashMap<>();

    public void clearGame() {
        GAME_MAP.clear();
    }

    public int createGame(String gameName) {
        if (gameName == null) {
            throw new BadRequestException("game name cannot be empty (DAO level)");

        }
        int gameID = GAME_MAP.size() + 1;
        GameData gameData = new GameData(gameID, null, null, gameName, new ChessGame());
        GAME_MAP.put(gameID, gameData);
        return gameID;
    }

    // gameID and gameData are both parameters so that updateGame can also remove games
    public void updateGame(int gameID, GameData gameData) throws DataAccessException {
        if (GAME_MAP.get(gameID) == null) {
            throw new DataAccessException("gameData to update not found");
        }
        GAME_MAP.remove(gameID);
        if (gameData == null) {
            return;
        }
        GAME_MAP.put(gameID, gameData);
    }

    public GameData getGame(int gameID) {
        return GAME_MAP.get(gameID);
    }
}

