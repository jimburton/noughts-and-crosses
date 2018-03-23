package CI346.websockets.noughtsandcrosses;
/**
 * Entry point for the Noughts and Crosses game.
 */

import static spark.Spark.*;

public class NoughtsAndCrosses {
    public static void main(String[] args) {
        staticFileLocation("/public"); //index.html is served at localhost:4567 (default port)
        webSocket("/game", NACWebSocket.class);
        init(); // Needed if you don't define any HTTP routes after your WebSocket routes
    }
}