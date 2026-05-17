package dataaccess;

import model.UserData;

public interface UserDAO {

    public void clearUser() throws DataAccessException;
    public void createUser(String username, String password, String email) throws DataAccessException;
    public UserData getUser(String username) throws DataAccessException;
}
