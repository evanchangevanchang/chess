package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.SQLException;

public class SQLGameDAO extends SQLDAO implements GameDAO{
    public SQLGameDAO() throws DataAccessException {
        String[] statements = new String[]{
                """
               CREATE TABLE IF NOT EXISTS game (
               gameID INT AUTO_INCREMENT PRIMARY KEY,
               whiteUsername VARCHAR(255),
               blackUsername VARCHAR(255),
               gameName VARCHAR(255) NOT NULL,
               game JSON NOT NULL
               );
               """
        };
        configureDB(statements);
    }

    @Override
    public void clearGame()  {
        var statement = "TRUNCATE TABLE game";
        try {executeUpdate(statement); }
        catch (SQLException e) {
            throw new DatabaseAccessException("clearGame failed");
        }
    }

    private int getCurrentID(){
        String statement = "SELECT MAX(gameID) from game";
        try (var connection = DatabaseManager.getConnection()) {
            var ps = connection.prepareStatement(statement);
            var result = ps.executeQuery();

            if (result.next()) {
                return result.getInt(1) + 1;
            }
            return 1;
        }

        catch (SQLException | DataAccessException e) {
            throw new DatabaseAccessException("get gameID failed");
        }
    }

    @Override
    public int createGame(String gameName) throws DatabaseAccessException {
        if (gameName == null) {
            throw new BadRequestException("game name cannot be empty (DAO level)");
        }
        int gameID = getCurrentID();
        String statement = """
            INSERT INTO game (gameID, whiteUsername, blackUsername,
            gameName, game) VALUES (?, ?, ?, ?, ?)
        """;
        ChessGame chessGame = new ChessGame();
        var chessJson = new Gson().toJson(chessGame);
        try {
        executeUpdate(statement, gameID, null, null, gameName, chessJson);
        return gameID;
        } catch (SQLException e) {
            throw new DatabaseAccessException("createGame failed");
        }
    }

    @Override
    public void updateGame(int gameID, GameData gameData) throws DataAccessException {
        if (gameData == null) {
            throw new DatabaseAccessException("no gameData to update found");
        }
        String deleteStatement = String.format("DELETE FROM game WHERE gameID = %d", gameID);
        String addStatement = """
            INSERT INTO game (gameID, whiteUsername, blackUsername,
            gameName, game) VALUES (?, ?, ?, ?, ?)
        """;
        try {
            executeUpdate(deleteStatement);
            var chessJson = new Gson().toJson(gameData.game());
            executeUpdate(addStatement, gameID, gameData.whiteUsername(),
                    gameData.blackUsername(), gameData.gameName(), chessJson);

        } catch (SQLException e) {
            throw new DatabaseAccessException("updateGame failed");
        }

    }

    @Override
    public GameData getGame(int gameID) throws DatabaseAccessException {
        String statement = "SELECT gameID, whiteUsername, blackUsername," +
                "gameName, game FROM game WHERE gameID=?";
        try (var connection = DatabaseManager.getConnection()) {
            try (var ps = connection.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                var result = ps.executeQuery();
                if (result.next()) {
                    ChessGame chessGame = new Gson().fromJson(result.getString("game"), ChessGame.class);
                    return new GameData(gameID, result.getString("whiteUsername"),
                            result.getString("blackUsername"), result.getString("gameName"),
                            chessGame);
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DatabaseAccessException("failed to get Game");
        }
        return null;
    }
}
