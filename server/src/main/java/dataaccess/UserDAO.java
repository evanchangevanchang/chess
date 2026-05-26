package dataaccess;

import model.UserData;

public interface UserDAO {

    public void clearUser() throws DatabaseAccessException;
    public void createUser(String username, String password, String email) throws DatabaseAccessException;
    public UserData getUser(String username) throws DatabaseAccessException;
    public boolean verifyPassword(String username, String attemptPassword) throws DatabaseAccessException;
}
