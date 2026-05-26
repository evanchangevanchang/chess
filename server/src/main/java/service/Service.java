package service;

import dataaccess.*;

public class Service {
    UserDAO userDAO;
    AuthDAO authDAO;
    GameDAO gameDAO;
    public Service() {
        try {
             userDAO = new SQLUserDAO();
             authDAO = new SQLAuthDAO();
             gameDAO = new SQLGameDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException("failed to start DAOs");
        }

    }


    public void clear() throws DataAccessException {
        userDAO.clearUser();
        authDAO.clearAuth();
        gameDAO.clearGame();
    }
}
