package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class SQLDataAccessTests extends SQLDAO {

    SQLAuthDAO authDAO;
    SQLUserDAO userDAO;
    SQLGameDAO gameDAO;

    public SQLDataAccessTests() throws DataAccessException {
        authDAO = new SQLAuthDAO();
        userDAO = new SQLUserDAO();
        gameDAO = new SQLGameDAO();
    }

    @Test
    void clearUser() throws DataAccessException {
        userDAO.createUser("XxBBC_MattressxX", "yum", "email");
        userDAO.clearUser();
        String statement = """
                SELECT * FROM user
                """;
        checkClear(statement);
    }
    @Test
    void clearGame() throws DataAccessException {
        gameDAO.createGame("jeep by kim petras");
        gameDAO.clearGame();
        String statement = """
                SELECT * FROM game
                """;
        checkClear(statement);
    }
    @Test
    void clearAuth() throws DataAccessException {
        authDAO.createAuth("CANNIBALISM! by Slayyyter");
        authDAO.clearAuth();
        String statement = """
                SELECT * FROM auth
                """;
        checkClear(statement);
    }

    @Test
    void createUserSuc() throws DataAccessException {
        userDAO.clearUser();
        userDAO.createUser("I", "LOVE", "CHOCOLATE");
        String statement = """
                SELECT * FROM user
                """;
        checkNotClear(statement);
    }
    @Test
    void createUserFail() {
        userDAO.clearUser();
        Assertions.assertThrows(DatabaseAccessException.class, () ->
                userDAO.createUser(null, null, "CHOCOLATE") );
    }
    @Test
    void createAuthSuc() throws DataAccessException {
        authDAO.clearAuth();
        authDAO.createAuth("PRETTY4U");
        String statement = """
                SELECT * FROM auth
                """;
        checkNotClear(statement);
    }
    @Test
    void createAuthFail() {
        authDAO.clearAuth();
        Assertions.assertThrows(DatabaseAccessException.class, () ->
        authDAO.createAuth(null) );
    }
    @Test
    void createGameSuc() throws DataAccessException {
        gameDAO.clearGame();
        gameDAO.createGame("meow");
        String statement = """
                SELECT * FROM game
                """;
        checkNotClear(statement);
    }
    @Test
    void createGameFail() {
        gameDAO.clearGame();
        Assertions.assertThrows(BadRequestException.class, () ->
        gameDAO.createGame(null)
        );
    }
    @Test
    void getGameSuc() {
        gameDAO.clearGame();
        int gameID = gameDAO.createGame("gay");
        GameData gameData = gameDAO.getGame(gameID);
        Assertions.assertNotNull(gameData);
    }
    @Test
    void getGameFail() {
        gameDAO.clearGame();
        GameData gameData = gameDAO.getGame(-1);
        Assertions.assertNull(gameData);
    }
    @Test
    void getAuthSuc() {
        authDAO.clearAuth();
        String authToken = authDAO.createAuth("ChuuCanDoIt");
         AuthData authData = authDAO.getAuth(authToken);
        Assertions.assertEquals("ChuuCanDoIt", authData.username());
    }
    @Test
    void getAuthFail() {
        authDAO.clearAuth();
        AuthData authData = authDAO.getAuth("steph curry");
        Assertions.assertNull(authData);
    }
    @Test
    void getUserSuc() {
        userDAO.clearUser();
        userDAO.createUser("ChuuCanDoIt", "MaeSucks", "emails");
        UserData userData = userDAO.getUser("ChuuCanDoIt");
        Assertions.assertEquals("ChuuCanDoIt", userData.username());
    }
    @Test
    void getUserFail() {
        userDAO.clearUser();
        UserData userData = userDAO.getUser("steph curry");
        Assertions.assertNull(userData);
    }
    @Test
    void verifyPasswordSuc() {
        userDAO.clearUser();
        userDAO.createUser("ChuuCanDoIt", "MaeSuckz", "emails");
        Assertions.assertTrue(userDAO.verifyPassword("ChuuCanDoIt","MaeSuckz"));
    }
    @Test
    void verifyPasswordFail() {
        userDAO.clearUser();
        userDAO.createUser("ChuuCanDoIt", "MaeSuckz", "emails");
        Assertions.assertFalse(userDAO.verifyPassword("ChuuCanDoIt","MaeDoesNotSuckz"));
    }

    void checkClear(String statement) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                var rs = ps.executeQuery(statement);
                Assertions.assertFalse(rs.next());
            }
        } catch (SQLException e ) {
            throw new DataAccessException("clear error");
        }
    }

    void checkNotClear(String statement) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                var rs = ps.executeQuery(statement);
                Assertions.assertTrue(rs.next());
            }
        } catch (SQLException e ) {
            throw new DataAccessException("clear error");
        }
    }
    // maybe remove
    void checkError(String statement) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                Assertions.assertThrows(DatabaseAccessException.class, () ->
                                ps.executeQuery(statement)
                        );
            }
        } catch (SQLException e ) {
            throw new DataAccessException("clear error");
        }
    }

}
