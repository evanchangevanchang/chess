package service;

import dataaccess.DataAccessException;

public class AuthService extends Service{
    public void validateAuth(String authToken) throws DataAccessException {
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("authToken not found");
        }
    }
}
