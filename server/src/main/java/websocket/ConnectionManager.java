package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Session, Session> connections = new ConcurrentHashMap<>();

    public void add(Session session) throws IOException {
        Session existingSession = connections.putIfAbsent(session, session);
        if (existingSession != null ){
            throw new IOException("already connected");
        }
    }

    public void remove(Session session) throws IOException {
        if (connections.contains(session)) {
            connections.remove(session);
        } else {
            throw new IOException("no session to remove");
        }
    }

    public void broadcast(Session excludeSession, ServerMessage serverMessage) throws IOException {
        for (Session s : connections.values()) {
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
