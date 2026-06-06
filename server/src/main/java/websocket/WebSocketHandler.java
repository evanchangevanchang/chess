package websocket;


import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.websocket.*;
import model.AuthData;
import org.jetbrains.annotations.NotNull;
import websocket.commands.UserGameCommand;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();

    @Override
    public void handleClose(@NotNull WsCloseContext wsCloseContext) {

    }

    @Override
    public void handleConnect(@NotNull WsConnectContext wsConnectContext) {
        System.out.print("Websocket connected\n");
        wsConnectContext.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext wsMessageContext) {
        UserGameCommand userGameCommand = new Gson().fromJson(wsMessageContext.message(), UserGameCommand.class);
        try {
        switch (userGameCommand.getCommandType()) {
            case CONNECT -> connect(userGameCommand, wsMessageContext.session);
            case MAKE_MOVE -> makeMove(userGameCommand, wsMessageContext.session);
            case LEAVE -> leave(userGameCommand, wsMessageContext.session);
            case RESIGN -> resign(userGameCommand, wsMessageContext.session);
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connect(UserGameCommand userGameCommand, Session session) throws IOException{
        connections.add(session);
        try {
            AuthDAO authDAO = new SQLAuthDAO();
            AuthData result = authDAO.getAuth(userGameCommand.getAuthToken());
            String msg = result.username() + " has connected";
            var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.setMessage(msg);
            connections.broadcast(session, notification);
            }
        catch (DataAccessException e) {
            throw new IOException("Data Access failed" + e.getMessage());
        }
    }
    private void makeMove(UserGameCommand userGameCommand, Session session) {
        try {
            GameDAO gameDAO = new SQLGameDAO();
            gameDAO.updateGame(userGameCommand.getGameID(), );
        } catch(Exception e) {

        }
    }
    private void leave(UserGameCommand userGameCommand, Session session) {

    }
    private void resign(UserGameCommand userGameCommand, Session session) {

    }
}
