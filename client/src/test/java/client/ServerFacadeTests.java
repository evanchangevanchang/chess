package client;

import org.junit.jupiter.api.*;
import request.CreateGameRequest;
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

}
