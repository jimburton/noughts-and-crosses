package CI346.websockets.noughtsandcrosses;
/**
 * Entry point for the Noughts and Crosses game.
 */

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import static spark.Spark.*;

public class NoughtsAndCrosses {
    public static void main(String[] args) {
        //Read in the config
        Config conf         = ConfigFactory.load();
        int port            = conf.getInt("web.port");
        String host         = conf.getString("web.host");
        String gamePath     = conf.getString("web.gamePath");
        String staticLoc    = conf.getString("web.staticFiles");
        long staticTO       = conf.getLong("web.staticTimeout");

        // Configure Spark
        port(port);
        staticFiles.location(staticLoc);
        staticFiles.expireTime(staticTO);

        webSocket(gamePath, NACWebSocket.class);
        init(); // Needed if you don't define any HTTP routes after your WebSocket routes
    }
}