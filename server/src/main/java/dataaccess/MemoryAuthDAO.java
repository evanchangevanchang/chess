package dataaccess;

import model.AuthData;

import java.util.UUID;
import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO{
    private final static HashMap<String, AuthData> AUTH_MAP = new HashMap<>();

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
    public void clearAuth() {
        AUTH_MAP.clear();
    }
}
