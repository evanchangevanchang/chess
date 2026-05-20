package service;

import dataaccess.DataAccessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import request.LoginRequest;
import request.LogoutRequest;
import request.RegisterRequest;
import result.LoginResult;

public class LoginTest extends Service{
    @Test
    public void loginSuccess() {
        this.clear();
        RegisterRequest registerRequest = new RegisterRequest(
                "PatrickGibbons69", "password", "email");
        UserService userService = new UserService();
        var result = userService.register(registerRequest);

        // logout to delete authData
        LogoutRequest logoutRequest = new LogoutRequest(result.authToken());
        Assertions.assertDoesNotThrow(() -> userService.logout(logoutRequest));

        LoginRequest loginRequest = new LoginRequest(
                "PatrickGibbons69", "password");

        // check that no errors are thrown
        LoginResult loginResult = Assertions.assertDoesNotThrow(() -> userService.login(loginRequest));
        //check that authData was created
        Assertions.assertNotNull(loginResult.authToken());
    }

    @Test
    public void loginFail() {
        this.clear();
        RegisterRequest registerRequest = new RegisterRequest(
                "PatrickGibbons69", "password", "email");
        LoginRequest loginRequest = new LoginRequest(
                "PatrickGibbons69", "incorrect_password");
        UserService userService = new UserService();
        userService.register(registerRequest);

        // check that no errors are thrown
        Assertions.assertThrows(DataAccessException.class, () -> userService.login(loginRequest));
    }

}
