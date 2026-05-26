package dataaccess;

import io.javalin.http.Context;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLUserDAO extends SQLDAO implements UserDAO{

    public SQLUserDAO() throws DatabaseAccessException {
        String[] statements = {
                """
               CREATE TABLE IF NOT EXISTS user (
               username VARCHAR(255) NOT NULL PRIMARY KEY,
               password VARCHAR(255) NOT NULL,
               email VARCHAR(255)
               );
               """
        };
        configureDB(statements);
    }

    @Override
    public void clearUser() {
        var statement = "DROP database user";
        try {executeUpdate(statement); }
        catch (SQLException e) {
            throw new DatabaseAccessException("clearUser failed");
        }
    }

    @Override
    public void createUser(String username, String password, String email)  {
        var statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        try {executeUpdate(statement, username, BCrypt.hashpw(password, BCrypt.gensalt()), email);}
        catch (SQLException e) {
            throw new DatabaseAccessException("failed to create");
        }
    }

    @Override
    public UserData getUser(String username) throws DatabaseAccessException {
        String statement = "SELECT username, password, email FROM user WHERE username=?";
        try (var connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setString(1, username);
                var result = preparedStatement.executeQuery();
                if (result.next()) {
                    return new UserData(result.getString("username"),
                            result.getString("password"),
                            result.getString("email"));
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DatabaseAccessException("failed to get user");
        }
        return null;
    }

    @Override
    public boolean verifyPassword(String username, String attemptPassword) {
        UserData userData = this.getUser(username);
        String hashedPassword = userData.password();
        return BCrypt.checkpw(attemptPassword, hashedPassword);
    }




}
