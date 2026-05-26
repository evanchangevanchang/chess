package dataaccess;

import model.AuthData;

import java.sql.SQLException;
import java.util.UUID;


public class SQLAuthDAO extends SQLDAO implements AuthDAO {
    public SQLAuthDAO() throws DataAccessException {
        String[] statements = new String[]{
                """
               CREATE TABLE IF NOT EXISTS auth (
               authToken VARCHAR(255) NOT NULL,
               username VARCHAR(255) NOT NULL,
               PRIMARY KEY (authToken)
               );
               """
        };
        configureDB(statements);
    }

    @Override
    public void clearAuth()  {
        var statement = "DROP database auth";
        try {executeUpdate(statement); }
        catch (SQLException e) {
            throw new DatabaseAccessException("clearAuth failed");
        }
    }

    @Override
    public String createAuth(String username) {

        String newToken = UUID.randomUUID().toString();
        var statement = "INSERT INTO auth (username, authToken) VALUES (?, ?)";
        try {executeUpdate(statement, username, newToken);}
        catch (SQLException  e) {
            throw new DatabaseAccessException("failed to create");
        }
        return newToken;
    }

    @Override
    public AuthData getAuth(String authToken) throws DatabaseAccessException {
        String statement = "SELECT username, authToken, FROM auth WHERE authToken=?";
        try (var connection = DatabaseManager.getConnection()) {
            try (var ps = connection.prepareStatement(statement)) {
                ps.setString(1, authToken);
                var result = ps.executeQuery();
                if (result.next()) {
                    return new AuthData(result.getString("authToken"),
                            result.getString("username"));
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DatabaseAccessException("failed to get auth");
        }

        return null;
    }

    @Override
    public void deleteAuth(String authToken) {
        String statement = String.format("DELETE FROM auth WHERE authToken = %s", authToken);
        try {executeUpdate(statement); }
        catch (SQLException e) {
            throw new DatabaseAccessException("deleteAuth failed");
        }
    }
}
