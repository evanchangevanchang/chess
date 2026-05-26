package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    private final static HashMap<String, UserData> USER_MAP = new HashMap<>();

    public void clearUser() {
        USER_MAP.clear();
    }

    public void createUser(String username, String password, String email) {
        UserData userData = new UserData(username, password, email);
        USER_MAP.put(username, userData);
    }

    public UserData getUser(String username) {
        return USER_MAP.get(username);
    }


    public boolean verifyPassword(String username, String attemptPassword) {
        return USER_MAP.get(username).password().equals(attemptPassword);
    }
}
