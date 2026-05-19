package dataaccess;

import model.AuthData;

import java.util.UUID;
import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO{
    private final static HashMap<String, AuthData> AUTH_MAP = new HashMap<>();

    private static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public void clearAuth() {
        AUTH_MAP.clear();
    }

    public String createAuth(String username) {
        String newToken = generateToken();
        AuthData newAuthData = new AuthData(newToken, username);
        AUTH_MAP.put(newToken, newAuthData);
        return newToken;
    }

    public AuthData getAuth(String authToken) {
        return AUTH_MAP.get(authToken);
    }

    public void deleteAuth(String authToken) throws DataAccessException{
        if (AUTH_MAP.get(authToken) == null) {
            throw new DataAccessException("AuthData could not be found");
        }
        AUTH_MAP.remove(authToken);
    }
}
