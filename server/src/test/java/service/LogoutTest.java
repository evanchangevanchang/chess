package service;

import dataaccess.DataAccessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import request.LogoutRequest;
import request.RegisterRequest;

public class LogoutTest extends Service {

    @Test
    public void logoutSuccessTest() {
        RegisterRequest registerRequest = new RegisterRequest(
                "PatrickGibbons69", "password", "email");
        UserService userService = new UserService();
        var result = userService.register(registerRequest);

        // logout to delete authData
        LogoutRequest logoutRequest = new LogoutRequest(result.authToken());
        Assertions.assertDoesNotThrow(() -> userService.logout(logoutRequest));

        // verify that authData has been deleted
        Assertions.assertNull(authDAO.getAuth(result.authToken()));
    }

    @Test
    public void logoutFailTest() {
        RegisterRequest registerRequest = new RegisterRequest(
                "PatrickGibbons69", "password", "email");
        UserService userService = new UserService();
        var result = userService.register(registerRequest);

        // check that error is thrown
        LogoutRequest logoutRequest = new LogoutRequest(null);
        Assertions.assertThrows( DataAccessException.class, () -> userService.logout(logoutRequest));

        //check that authData was not deleted
        Assertions.assertNotNull(result.authToken());

    }
}
