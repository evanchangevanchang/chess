package service;


import dataaccess.*;
import model.AuthData;
import model.UserData;
import request.LoginRequest;
import request.LogoutRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.RegisterResult;


public class UserService extends Service {
    public RegisterResult register(RegisterRequest registerRequest) throws BadRequestException, AlreadyTakenException, DataAccessException {
        String username = registerRequest.username();
        String password = registerRequest.password();
        String email = registerRequest.email();

        //check for existing username
        UserData userData = userDAO.getUser(username);
        if (userData != null) {
            throw new AlreadyTakenException("username taken");
        }
        if (password == null) {
            throw new BadRequestException("empty password");
        }
        // create auth data
        String authToken = authDAO.createAuth(username);
        // create user data
        userDAO.createUser(username, password, email);

        return new RegisterResult(username, authToken);
    }
    public LoginResult login(LoginRequest loginRequest) throws DataAccessException, BadRequestException{
        String username = loginRequest.username();
        String password = loginRequest.password();
        if (password == null || username == null) {
            throw new BadRequestException("password cannot be empty");
        }
        // check if userData exists
        UserData userData = userDAO.getUser(username);

        if (userData == null) {
            throw new DataAccessException("userData not found");
        }

        // check if password is correct
        if (!userDAO.verifyPassword(username, password)) {
            throw new DataAccessException("incorrect password");
        }
        // create auth data
        String authToken = authDAO.createAuth(username);
        return new LoginResult(username, authToken);
    }

    public void logout(LogoutRequest logoutRequest) throws DataAccessException {
        String authToken = logoutRequest.authToken();
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("logout authData not found");
        }
        authDAO.deleteAuth(authToken);
    }
}
