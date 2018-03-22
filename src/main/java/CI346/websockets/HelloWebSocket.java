package CI346.websockets;

import static spark.Spark.init;
import static spark.Spark.staticFileLocation;
import static spark.Spark.webSocket;

public class HelloWebSocket {
    public static void main(String[] args) {
        staticFileLocation("/public"); //index.html is served at localhost:4567 (default port)
        webSocket("/echo", EchoWebSocket.class);
        init(); // Needed if you don't define any HTTP routes after your WebSocket routes
    }
}