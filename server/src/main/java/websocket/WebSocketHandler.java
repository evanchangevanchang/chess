package websocket;


import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
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
            case MAKE_MOVE -> makeMove(userGameCommand, wsMessageContext.session, userGameCommand.getMove());
            case LEAVE -> leave(userGameCommand, wsMessageContext.session);
            case RESIGN -> resign(userGameCommand, wsMessageContext.session);
        }
        } catch (Exception e) {
            String msg = e.getMessage();
            ServerMessage serverMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            serverMessage.setErrorMessage(msg);
            try {
                connections.send(wsMessageContext.session, serverMessage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void connect(UserGameCommand command, Session session) throws IOException, DataAccessException{
        GameDAO gameDAO = new SQLGameDAO();
        int gameID = command.getGameID();
        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            throw new IOException("no gameData");
        }
    try {
        connections.add(gameID, session);
    } catch (IOException e) { // return if already in connected
        throw new IOException("already connected");
    }
        AuthData result = getAuthData(command, session);
        if (result == null) {
            throw new IOException("Data Access failed ");
        }
        String username = result.username();
        String msg = username + " has connected";
        sendNotif(gameID, msg, session);

        var loadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        loadGame.setGame(gameData);
        connections.send(session, loadGame); // only connecting client loads
    }
    private void makeMove(UserGameCommand command, Session session, ChessMove move) throws IOException, DataAccessException {
        GameDAO gameDAO = new SQLGameDAO();
        int gameID = command.getGameID();
        GameData gameData = gameDAO.getGame(gameID);
        ChessGame game = gameData.game(); // also checks if gameData is null
        AuthData authData = getAuthData(command, session);

        // check if move is valid
        if (gameData.game().resigned) {
            throw new IOException("a player has already resigned");
        }
        ChessGame.TeamColor playerTeam = getPlayerTeam(authData, gameData);
        // check if player is an observer
        if (playerTeam == null) {
            throw new IOException("observer cannot play");
        } else if (playerTeam.equals(ChessGame.TeamColor.WHITE) && (game.getTeamTurn() != ChessGame.TeamColor.WHITE) ||
            playerTeam.equals(ChessGame.TeamColor.BLACK) && (game.getTeamTurn() != ChessGame.TeamColor.BLACK)) {
            throw new IOException("not your turn");
        }
        // update game
        try {
        gameData.game().makeMove(move);
        } catch (InvalidMoveException e) {
            throw new IOException("invalid move");
        }
        // broadcast load game to all clients
        sendLoad(gameID, gameData);

        // notification what move was made
        String username = authData.username();
        String moveNotif = username + "has made move: " + move;
        sendNotif(gameID, moveNotif, session);

        // check for check, checkmate etc. send a notification
        if (game.isInCheckmate(playerTeam)) {
            String checkNotif = username + " is in checkmate";
            sendNotif(gameID, checkNotif, null);
            game.resigned = true;
            return;
        }
        if (game.isInStalemate(playerTeam)) {
            String checkNotif = username + " is in stalemate";
            sendNotif(gameID, checkNotif, null);
            game.resigned = true;
            return;
        }
        if (game.isInCheck(playerTeam)) {
            String checkNotif = username + " is in check";
            sendNotif(gameID, checkNotif, null);
        }
    }
    private void leave(UserGameCommand command, Session session) throws IOException, DataAccessException {
        // remove root client
        AuthData authData = getAuthData(command, session);
        try {
            connections.remove(command.getGameID(), session);
        } catch (IOException e) {
            return; // return if already left
        }
        // update game in database
        int gameID = updateGame(command, authData);
        // send notification to all other clients that root client has left
        String msg = authData.username() + " has disconnected";
        sendNotif(gameID, msg, session);
    }

    // clean up leave game method
    private static int updateGame(UserGameCommand command, AuthData authData) throws DataAccessException {
        GameDAO gameDAO = new SQLGameDAO();
        int gameID = command.getGameID();
        GameData gameData = gameDAO.getGame(gameID);
        if (!(gameData.whiteUsername() == null) &&
                gameData.whiteUsername().equals(authData.username())) {
            gameDAO.updateGame(gameID, new GameData(gameID, null,
                    gameData.blackUsername(), gameData.gameName(), gameData.game()));
        } else if (!(gameData.blackUsername() == null) &&
                gameData.blackUsername().equals(authData.username())) {
            gameDAO.updateGame(gameID, new GameData(gameID, gameData.whiteUsername(),
                    null, gameData.gameName(), gameData.game()));
        }
        return gameID;
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
            throw new IOException("already resigned");
        }
        if (getPlayerTeam(authData, gameData) == null) {
            throw new IOException("observer cannot resign");
        }
        // update game where resigned is set to true
        gameData.game().resigned = true;
        gameDAO.updateGame(command.getGameID(), gameData);

        String msg = authData.username() +  " has resigned";
        sendNotif(gameID, msg, null);
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

    private ChessGame.TeamColor getPlayerTeam(AuthData authData, GameData gameData) {
        if (gameData.whiteUsername() != null && gameData.whiteUsername().equals(authData.username())) {
            return ChessGame.TeamColor.WHITE;
        } else if (gameData.blackUsername() != null && gameData.blackUsername().equals(authData.username())) {
            return ChessGame.TeamColor.BLACK;
        }
        return null;
    }

    private void sendNotif(int gameID, String msg, Session excludeSession) throws IOException {
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notification.setMessage(msg);
        connections.broadcast(gameID, excludeSession, notification);
    }
    private void sendLoad(int gameID, GameData gameData) throws IOException {
        var loadMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        loadMessage.setGame(gameData);
        connections.broadcast(gameID, null, loadMessage); // exclude session always null
    }
}
