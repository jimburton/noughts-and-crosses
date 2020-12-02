package CI346.websockets.noughtsandcrosses;
/**
 * Entry point for the Noughts and Crosses game.
 */

import spark.Spark;
import spark.servlet.SparkApplication;

import java.util.HashMap;

import static spark.Spark.*;

public class NoughtsAndCrosses {
    public static void main(String[] args) {
        staticFiles.location("/html");
        staticFiles.expireTime(600L);

        webSocket("/game", NACWebSocket.class);
        init(); // required if you don't define any routes after the websocket
    }
}