package dataaccess;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
    @BeforeEach
    void clearUser() throws DataAccessException {

        userDAO.createUser("XxBBC_MattressxX", "yum", "email");

        userDAO.clearUser();


        String statement = """
                SELECT * FROM user
                """;
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                var rs = ps.executeQuery(statement);
                Assertions.assertFalse(rs.next());
            }
        } catch (SQLException e ) {
            throw new DataAccessException("clear error");
        }
    }
}
