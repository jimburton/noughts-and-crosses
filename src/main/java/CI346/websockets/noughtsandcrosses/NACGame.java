package CI346.websockets.noughtsandcrosses;

import static spark.Spark.*;

public class NACGame {
    public static void main(String[] args) {
        staticFileLocation("/public"); //index.html is served at localhost:4567 (default port)
        webSocket("/game", NACWebSocket.class);
        init(); // Needed if you don't define any HTTP routes after your WebSocket routes
    }
}