package dataaccess;

import model.AuthData;

public interface AuthDAO {

    public void clearAuth() throws DataAccessException;
    public String createAuth(String username) throws DataAccessException;
    public void deleteAuth(String authToken) throws DataAccessException;
}
