package service;

import chess.ChessGame;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import model.GameData;
import request.JoinGameRequest;

import java.util.ArrayList;
import java.util.Collection;

public class GameService extends Service{
    public Collection<GameData> listGames() {
        Collection<GameData> games = new ArrayList<>();
        int gameID = 1;
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

    public void joinGame(JoinGameRequest joinGameRequest) throws DataAccessException {
        ChessGame.TeamColor playerColor = joinGameRequest.playerColor();
        int gameID = joinGameRequest.gameID();
        if (playerColor == null) {
            throw new BadRequestException("playerColor is null");
        }
        GameData gameData = this.getGame(gameID);
        if (gameData == null) {
            throw new DataAccessException("gameData not found");
        }
    }
}
