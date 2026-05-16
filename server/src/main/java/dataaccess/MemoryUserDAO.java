package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    private final static HashMap<String, UserData> USER_MAP = new HashMap<>();

    public void clearUser() {
        USER_MAP.clear();
    }
}
