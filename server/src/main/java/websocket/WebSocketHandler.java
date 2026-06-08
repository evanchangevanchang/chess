package websocket;


import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.websocket.*;
import model.AuthData;
import model.GameData;
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
        } catch (IOException | DataAccessException e) { // figure out a better way?
            e.printStackTrace();
        }
    }

    private void connect(UserGameCommand command, Session session) throws IOException{
        connections.add(session);
        AuthData result = getAuthData(command, session);
        if (result == null) {
            throw new IOException("Data Access failed ");
        }
        String msg = result.username() + " has connected";
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notification.setMessage(msg);
        connections.broadcast(session, notification);


    }
    private void makeMove(UserGameCommand userGameCommand, Session session) {
        try {
            GameDAO gameDAO = new SQLGameDAO();
//            gameDAO.updateGame(userGameCommand.getGameID(), );
        } catch(Exception e) {

        }
    }
    private void leave(UserGameCommand command, Session session) throws IOException, DataAccessException {
        // remove root client
        AuthData authData = getAuthData(command, session);
        connections.remove(session);
        // update game in database
        GameDAO gameDAO = new SQLGameDAO();
        int gameID = command.getGameID();
        GameData gameData = gameDAO.getGame(gameID);
        if (!gameData.whiteUsername().isEmpty() &&
                gameData.whiteUsername().equals(authData.username())) {
            gameDAO.updateGame(gameID, new GameData(gameID, null,
                    gameData.blackUsername(), gameData.gameName(), gameData.game()));
        } else if (!gameData.blackUsername().isEmpty() &&
                gameData.blackUsername().equals(authData.username())) {
            gameDAO.updateGame(gameID, new GameData(gameID, gameData.whiteUsername(),
                    null, gameData.gameName(), gameData.game()));
        }
        String msg = authData.username() + " has disconnected";
        // send notification to all other clients that root client has left
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notification.setMessage(msg);
        connections.broadcast(session, notification);
    }
    private void resign(UserGameCommand command, Session session) throws IOException, DataAccessException {
        AuthData authData = getAuthData(command, session);
        GameDAO gameDAO = new SQLGameDAO();
        int gameID = command.getGameID();
        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            throw new DataAccessException("Game Data not found");
        }
        if (gameData.game().resigned) {
            String msg = "already resigned";
            return; // figure out what to do here
        }
        // update game where resigned is set to true
        gameData.game().resigned = true;
        gameDAO.updateGame(command.getGameID(), gameData);
    }

    private AuthData getAuthData(UserGameCommand command, Session session) throws IOException{
        try {
        AuthDAO authDAO = new SQLAuthDAO();
         return authDAO.getAuth(command.getAuthToken());
        } catch (DataAccessException e) {
            var serverError = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            serverError.setMessage("failed to get Auth Data");
            session.getRemote().sendString(new Gson().toJson(serverError));
            throw new IOException("failed to get authData"); // throw an IOException?
        }
    }
}
