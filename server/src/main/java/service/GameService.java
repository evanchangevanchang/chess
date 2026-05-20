package service;

import chess.ChessGame;
import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import model.AuthData;
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

    public void joinGame(JoinGameRequest joinGameRequest, String authToken) throws DataAccessException {
        ChessGame.TeamColor playerColor = joinGameRequest.playerColor();
        int gameID = joinGameRequest.gameID();
        if (playerColor == null) {
            throw new BadRequestException("playerColor is null");
        }
        GameData gameData = this.getGame(gameID);
        if (gameData == null) {
            throw new BadRequestException("gameData not found");
        }
        AuthData authData = authDAO.getAuth(authToken);
        String username = authData.username();
        if (playerColor == ChessGame.TeamColor.WHITE && gameData.whiteUsername() == null) {
            // create new game data with different username
            GameData newGameData = new GameData(gameData.gameID(), username,
                    gameData.blackUsername(), gameData.gameName(), gameData.game());
            gameDAO.updateGame(gameData.gameID(), newGameData);
        } else if (playerColor == ChessGame.TeamColor.BLACK && gameData.blackUsername() == null) {
            GameData newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(),
                    username, gameData.gameName(), gameData.game());
            gameDAO.updateGame(gameData.gameID(), newGameData);
        } else {
            throw new AlreadyTakenException("color already taken");
        }
    }
}
