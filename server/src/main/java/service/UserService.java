package service;


import dataaccess.*;
import model.UserData;
import request.LoginRequest;
import request.LogoutRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.RegisterResult;

public class UserService {
    public RegisterResult register(RegisterRequest registerRequest) throws AlreadyTakenException {
        String username = registerRequest.username();
        String password = registerRequest.password();
        String email = registerRequest.email();

        MemoryUserDAO userDAO = new MemoryUserDAO();

        //check for existing username
        UserData userData = userDAO.getUser(username);
        if (userData != null) {
            throw new AlreadyTakenException("username taken");
        }
        // create auth data
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        // create user data
        userDAO.createUser(username, password, email);
        String authToken = authDAO.createAuth(username);

        return new RegisterResult(username, authToken);
    };
//    public LoginResult login(LoginRequest loginRequest) {};
    public void logout(LogoutRequest logoutRequest) {};
}
