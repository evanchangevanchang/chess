package dataaccess;
import java.sql.*;

import static java.sql.Types.NULL;

public class SQLDAO {
    public SQLDAO() throws DatabaseAccessException {
        try {
            DatabaseManager.createDatabase();
//            new SQLUserDAO();
//            new SQLAuthDAO();
//            new SQLGameDAO();

        } catch (DataAccessException e) {
            throw new DatabaseAccessException("clear failed:" + e.getMessage());
        }
    }


    public void configureDB(String[] statements) throws DatabaseAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            for (String statement : statements) {
                // if (statement == null) {continue;} // maybe
                try (var preparedStatement = connection.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (DataAccessException | SQLException e) {
            throw new DatabaseAccessException(String.format("configureDB failed: %s", e.getMessage()));

        }
    }


    public int executeUpdate(String statement, Object... params) throws DatabaseAccessException, SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String thing) preparedStatement.setString(i + 1, thing);
                    else if (param instanceof Integer thing) preparedStatement.setInt(i + 1, thing);
                    else if (param == null) preparedStatement.setNull(i + 1, Types.NULL);
                }
                preparedStatement.executeUpdate();

                ResultSet rs = preparedStatement.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException | DataAccessException e) {
            throw new DatabaseAccessException(String.format("executeUpdate failed: %s, %s", statement, e.getMessage()));
        }
    }
}
