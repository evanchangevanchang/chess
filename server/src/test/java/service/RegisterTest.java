package service;

import dataaccess.BadRequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import request.RegisterRequest;
import result.RegisterResult;

public class RegisterTest extends Service {
    @Test
    public void registerSuccessTest() {
        RegisterRequest testRegReq = new RegisterRequest("Xx_BBC_MATTRESS_xX", "password", "email");
        UserService userService = new UserService();
        RegisterResult result = userService.register(testRegReq);
        //check that there is a result returned
        Assertions.assertNotNull(result);
        //check that authData was created
        Assertions.assertNotNull(authDAO.getAuth(result.authToken()));
    }

    @Test
    public void registerFailTest() {
        RegisterRequest testRegReq = new RegisterRequest("invalid", null, "email");
        UserService userService = new UserService();
        //check that result returned
        Assertions.assertThrows(BadRequestException.class, () -> userService.register(testRegReq));
    }
}





//public class ClearTest {
//
//
//    public void clearTest() {
//        GameService gameService = new GameService();
//        gameService.createGame("hello everyone");
//        gameService.clear();
//        Assertions.assertNull(gameService.getGame(1));
//    }
//}