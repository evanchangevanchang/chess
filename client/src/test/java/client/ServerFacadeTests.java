package client;

import org.junit.jupiter.api.*;
import request.CreateGameRequest;
import request.LoginRequest;
import request.LogoutRequest;
import request.RegisterRequest;
import result.RegisterResult;
import server.Server;

import java.rmi.server.ExportException;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void clearTest() {
        serverFacade.clear();
        result.RegisterResult regResult = serverFacade.register(new RegisterRequest("mae", "p", "email"));
        String authToken = regResult.authToken();
        serverFacade.clear();
        Assertions.assertThrows(Exception.class, () ->
                serverFacade.listGames(authToken)); // no authToken found
    }

    @Test
    public void regFail() {
        serverFacade.clear();
        Assertions.assertThrows(Exception.class, () ->
        serverFacade.register(new RegisterRequest(null,null,null)));
    }
    @Test
    public void regSuccess() {
        serverFacade.clear();
        result.RegisterResult regResult = serverFacade.register(new RegisterRequest("mae", "p", "email"));
        Assertions.assertNotNull(regResult);
    }
    @Test
    public void logoutFail() {
        serverFacade.clear();
        serverFacade.register(new RegisterRequest("mae", "p", "email"));
        Assertions.assertThrows(Exception.class, () ->
        serverFacade.logout(new LogoutRequest(null)));
    }
    @Test
    public void logoutSuccess() {
        serverFacade.clear();
        result.RegisterResult regResult = serverFacade.register(new RegisterRequest("mae", "p", "email"));
        serverFacade.logout(new LogoutRequest(regResult.authToken()));
        Assertions.assertThrows(Exception.class, () ->
        serverFacade.listGames(regResult.authToken())); // cannot list games without authToken
    }
    @Test
    public void loginFail() {
        serverFacade.clear();
        result.RegisterResult regResult = serverFacade.register(new RegisterRequest("mae", "p", "email"));
        serverFacade.logout(new LogoutRequest(regResult.authToken()));
        Assertions.assertThrows(Exception.class, () ->
        serverFacade.login(new LoginRequest("mae", "fart")));
    }
    @Test
    public void loginSuccess() {
        serverFacade.clear();
        result.RegisterResult regResult = serverFacade.register(new RegisterRequest("mae", "p", "email"));
        serverFacade.logout(new LogoutRequest(regResult.authToken()));
        Assertions.assertDoesNotThrow(() ->
        serverFacade.login(new LoginRequest("mae", "p")));
    }
    @Test
    public void createSuccess() {
        serverFacade.clear();
        result.RegisterResult regResult = serverFacade.register(new RegisterRequest("mae", "p", "email"));
        serverFacade.createGame(new CreateGameRequest("mae_is_cool"), regResult.authToken());
        Assertions.assertNotNull(serverFacade.listGames(regResult.authToken()));
    }
    @Test
    public void createFail() {
        serverFacade.clear();
        result.RegisterResult regResult = serverFacade.register(new RegisterRequest("mae", "p", "email"));
        Assertions.assertThrows(Exception.class, () ->
        serverFacade.createGame(new CreateGameRequest(null), regResult.authToken()));
    }
    @Test
    public void listSuccess() {
        serverFacade.clear();
        result.RegisterResult regResult = serverFacade.register(new RegisterRequest("mae", "p", "email"));
        serverFacade.createGame(new CreateGameRequest("mae_suckz"), regResult.authToken());
        Assertions.assertNotNull(serverFacade.listGames(regResult.authToken()));
    }
    @Test
    public void listFail() {
        serverFacade.clear();
        serverFacade.register(new RegisterRequest("mae", "p", "email"));
        Assertions.assertThrows(Exception.class, () ->
                Assertions.assertNotNull(serverFacade.listGames(null)));
    }

}
