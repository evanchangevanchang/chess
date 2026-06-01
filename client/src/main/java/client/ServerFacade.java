package client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.GameData;
import request.CreateGameRequest;
import request.LoginRequest;
import request.LogoutRequest;
import request.RegisterRequest;
import result.CreateGameResult;
import result.ListGameResult;
import result.LoginResult;
import result.RegisterResult;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverURL;

    public ServerFacade(String url) {
        serverURL = url;
    }
    public RegisterResult register(RegisterRequest registerRequest) {
        var request = buildRequest("POST", "/user", registerRequest, null);
        var response = sendRequest(request);
        return handleResponse(response, RegisterResult.class);
    }
    public LoginResult login(LoginRequest loginRequest) {
        var request = buildRequest("POST", "/session", loginRequest, null);
        var response = sendRequest(request);
        return handleResponse(response, LoginResult.class);
    }
    public void logout(LogoutRequest logoutRequest) {
        var request = buildRequest("DELETE", "/session", logoutRequest, logoutRequest.authToken());
        var response = sendRequest(request);
        handleResponse(response, null);
    }
    public Collection<GameData> listGames(String authToken) {
        var request = buildRequest("GET", "/game", null, authToken);
        var response = sendRequest(request);
        var type = new TypeToken<Collection<GameData>>(){}.getType();
        return handleResponse(response, ListGameResult.class);
    }
    public void createGame(CreateGameRequest createGameRequest, String authToken) {
        var request = buildRequest("POST", "/game", createGameRequest, authToken);
        var response = sendRequest(request);
        handleResponse(response, CreateGameResult.class);
    }
    public void clear() {
        var request = buildRequest("DELETE", "/db", null, null);
        var response = sendRequest(request);
        handleResponse(response, null);
    }


    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverURL + path))
                .method(method, makeRequestBody(body));
        if (authToken != null) {
            request.setHeader("Authorization", authToken);
        }
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        return request.build();

    }
    private HttpRequest.BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return HttpRequest.BodyPublishers.ofString(new Gson().toJson(request));
        }
        return HttpRequest.BodyPublishers.noBody();
        }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new ResponseException("send request failed: " + e.getMessage());
        }
    }
    private <T> T handleResponse(HttpResponse<String> response, Type responseType) throws ResponseException {
        var status = response.statusCode();
        if (status != 200) {
            var body = response.body();
            if (body != null) {
                throw new ResponseException(body);
            }
            throw new ResponseException("failure: " + status);
        }
        if (responseType != null) {
            return new Gson().fromJson(response.body(), responseType); // deserialize
        }
        return null;
    }
}
