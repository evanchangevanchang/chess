package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;

public class Service {
    MemoryUserDAO userDAO = new MemoryUserDAO();
    MemoryAuthDAO authDAO = new MemoryAuthDAO();
    MemoryGameDAO gameDAO = new MemoryGameDAO();

    public void clear() {
        userDAO.clearUser();
        authDAO.clearAuth();
        gameDAO.clearGame();
    }
}
