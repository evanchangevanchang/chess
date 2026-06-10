package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, Set<Session>> connections = new ConcurrentHashMap<>();

    public void add(int gameID, Session session) throws IOException {
        if (!connections.get(gameID).add(session)){
            throw new IOException("already connected");
        }
    }

    public void remove(int gameID, Session session) throws IOException {
        if (!connections.get(gameID).remove(session)) {
            throw new IOException("no session to remove");
        }
    }

    public void broadcast(int gameID, Session excludeSession, ServerMessage serverMessage) throws IOException {
        for (Session s : connections.get(gameID)) {
            if (s.isOpen()) {
                if (!s.equals(excludeSession)) {
                    s.getRemote().sendString(new Gson().toJson(serverMessage));
                }
            }
        }
    }

    // send a message to one session
    public void send(Session s, ServerMessage serverMessage) throws IOException {
        if (s.isOpen()) {
            s.getRemote().sendString(new Gson().toJson(serverMessage));
        }
    }
}
